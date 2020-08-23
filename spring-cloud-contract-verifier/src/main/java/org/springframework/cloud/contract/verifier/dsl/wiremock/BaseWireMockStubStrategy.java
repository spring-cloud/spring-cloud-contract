/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.Reference;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.springframework.cloud.contract.verifier.util.ContentType.UNKNOWN;

/**
 * Common abstraction over WireMock Request / Response conversion implementations
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @since 1.0.0
 */
abstract class BaseWireMockStubStrategy {
	protected BaseWireMockStubStrategy(Contract contract) {
		this.processor = templateProcessor();
		this.template = contractTemplate();
		this.contract = contract;
	}

	private TemplateProcessor templateProcessor() {
		return new HandlebarsTemplateProcessor();
	}

	private ContractTemplate contractTemplate() {
		return new HandlebarsTemplateProcessor();
	}

	/**
	 * @return the stub side values from the object
	 */
	protected Object getStubSideValue(Object object) {
		return MapConverter.getStubSideValues(object);
	}

	/**.
	 * For the given {@link ContentType} returns the String version of the body
	 */
	public String parseBody(Object value, ContentType contentType) {
		return parseBody(value.toString(), contentType);
	}

	/**.
	 * Return body as String from file
	 */
	public String parseBody(FromFileProperty value, ContentType contentType) {
		return value.asString();
	}

	/**.
	 * For the given {@link ContentType} returns the Boolean version of the body
	 */
	public Boolean parseBody(Boolean value, ContentType contentType) {
		return value;
	}

	/**.
	 * For the given {@link ContentType} returns the String version of the body
	 */
	public String parseBody(Map map, ContentType contentType) {
		Object transformedMap = MapConverter.getStubSideValues(map);
		transformedMap = transformMapIfRequestPresent(transformedMap);
		String json = toJson(transformedMap);
		// the space is important cause at the end of the json body you also have a }
		// you can't have 4 } next to each other
		String unquotedJson = json.replace("\"" + WRAPPER, "").replace(WRAPPER + "\"", " ");
		String unescapedJson = unquotedJson.replace("\\/", "/");
		return parseBody(unescapedJson, contentType);
	}

	private Object transformMapIfRequestPresent(Object transformedMap) {
		Body requestBody = contract.getRequest().getBody();
		if (requestBody == null) {
			return transformedMap;
		}

		String testSideBody = toJson(MapConverter.getTestSideValues(requestBody));
		DocumentContext context = JsonPath.parse(testSideBody);
		return processEntriesForTemplating(transformedMap, context);
	}

	private Object processEntriesForTemplating(Object transformedMap, final DocumentContext context) {
		return MapConverter.transformValues(transformedMap, new Closure<Object>(this, this) {
			public Object doCall(Object it) {
				if (it instanceof String && processor.containsJsonPathTemplateEntry((String) it)) {
					String jsonPath = processor.jsonPathFromTemplateEntry((String) it);
					if (!StringGroovyMethods.asBoolean(jsonPath)) {
						return it;
					}

					Object value = context.read(jsonPath);
					if (value instanceof String) {
						return it;
					}

					return WRAPPER + it + WRAPPER;
				}
				else if (it instanceof String && processor.containsTemplateEntry((String) it) && template.escapedBody()
						.equals(it)) {
					return template.escapedBody();
				}

				return it;
			}

			public Object doCall() {
				return doCall(null);
			}

		});
	}

	/**.
	 * For the given {@link ContentType} returns the String version of the body
	 */
	public String parseBody(List list, final ContentType contentType) {
		final Reference<List> result = new Reference<List>(new ArrayList());
		DefaultGroovyMethods.each(list, new Closure<List<? extends Object>>(this, this) {
			public List<? extends Object> doCall(Object it) {
				if (it instanceof Map) {
					return setGroovyRef(result, DefaultGroovyMethods
							.plus(result.get(), MapConverter.getStubSideValues(it)));
				}
				else {
					return setGroovyRef(result, DefaultGroovyMethods.plus(result.get(), parseBody(it, contentType)));
				}

			}

			public List<? extends Object> doCall() {
				return doCall(null);
			}

		});
		return parseBody(toJson(result.get()), contentType);
	}

	/**.
	 * For the given {@link ContentType} returns the String version of the body
	 */
	public String parseBody(GString value, ContentType contentType) {
		Object processedValue = ContentUtils.extractValue(value, contentType, new Closure<Object>(this, this) {
			public Object doCall(Object o) {
				return o instanceof DslProperty ? ((DslProperty) o).getClientValue() : o;
			}

		});
		if (processedValue instanceof GString) {
			return parseBody(processedValue.toString(), contentType);
		}

		return parseBody(processedValue, contentType);
	}

	/**.
	 * For the given {@link ContentType} returns the String version of the body
	 */
	public String parseBody(String value, ContentType contentType) {
		return value;
	}

	private static String toJson(Object value) {
		if (value instanceof Map) {
			Map convertedMap = DefaultGroovyMethods
					.asType(MapConverter.transformValues(value, new Closure<Object>(null, null) {
						public Object doCall(Object it) {
							return it instanceof GString ? it.toString() : it;
						}

						public Object doCall() {
							return doCall(null);
						}

					}), Map.class);
			String jsonOutput = null;
			jsonOutput = convertValueToJson(convertedMap);
			return jsonOutput.replaceAll("\\\\\\\\\\\\", "\\\\");
		}
		String jsonOutput = convertValueToJson(value);
		return jsonOutput.replaceAll("\\\\\\\\\\\\", "\\\\");
	}

	/**
	 * Attempts to guess the {@link ContentType} from body and headers. Returns
	 * {@link ContentType#UNKNOWN} if it fails to guess.
	 */
	protected ContentType tryToGetContentType(Object body, Headers headers) {
		ContentType contentType = ContentUtils.recognizeContentTypeFromHeader(headers);
		if (UNKNOWN.equals(contentType)) {
			if (!DefaultGroovyMethods.asBoolean(body)) {
				return UNKNOWN;
			}

			return ContentUtils.getClientContentType(body);
		}

		return contentType;
	}

	private static final String WRAPPER = "UNQUOTE_ME";
	protected final TemplateProcessor processor;
	protected final ContractTemplate template;
	protected final Contract contract;
	private static Closure transform = new Closure<Object>(null, null) {
		public Object doCall(Object it) {
			return it instanceof DslProperty ? MapConverter
					.transformValues(((DslProperty) it).getClientValue(), transform) : it;
		}

		public Object doCall() {
			return doCall(null);
		}

	};

	private static <T> T setGroovyRef(Reference<T> ref, T newValue) {
		ref.set(newValue);
		return newValue;
	}

	private static String convertValueToJson(Object value) {
		try {
			return new ObjectMapper().writeValueAsString(value);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
