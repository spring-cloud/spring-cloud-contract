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
import groovy.lang.GString;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.springframework.cloud.contract.verifier.util.ContentType.UNKNOWN;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getClientContentType;
import static org.springframework.cloud.contract.verifier.util.MapConverter.transformValues;

/**
 * Common abstraction over WireMock Request / Response conversion implementations.
 *
 * @since 1.0.0
 */
abstract class BaseWireMockStubStrategy {

	private static final String WRAPPER = "UNQUOTE_ME";

	protected final TemplateProcessor processor;

	protected final ContractTemplate template;

	protected final Contract contract;

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

	/**
	 * For the given {@link ContentType} returns the String version of the body.
	 */
	String parseBody(Object value, ContentType contentType) {
		return parseBody(value.toString(), contentType);
	}

	/**
	 * Return body as String from file.
	 */
	String parseBody(FromFileProperty value, ContentType contentType) {
		return value.asString();
	}

	/**
	 * For the given {@link ContentType} returns the Boolean version of the body.
	 */
	String parseBody(Boolean value, ContentType contentType) {
		return value.toString();
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body.
	 */
	String parseBody(Map<?, ?> map, ContentType contentType) {
		Object transformedMap = MapConverter.getStubSideValues(map);
		transformedMap = transformMapIfRequestPresent(transformedMap);
		String json = toJson(transformedMap);
		// the space is important cause at the end of the json body you also have a }
		// you can't have 4 } next to each other
		String unquotedJson = json.replace('"' + WRAPPER, "").replace(WRAPPER + '"', " ");
		String unescapedJson = unquotedJson.replace("\\/", "/");
		return parseBody(unescapedJson, contentType);
	}

	private Object transformMapIfRequestPresent(Object transformedMap) {
		Object requestBody = contract.getRequest().getBody();
		if (requestBody == null) {
			return transformedMap;
		}
		String testSideBody = toJson(MapConverter.getTestSideValues(requestBody));
		DocumentContext context = JsonPath.parse(testSideBody);
		return processEntriesForTemplating(transformedMap, context);
	}

	private Object processEntriesForTemplating(Object transformedMap, DocumentContext context) {
		return transformValues(transformedMap, (val) -> {
			if (val instanceof String && processor.containsJsonPathTemplateEntry((String) val)) {
				String jsonPath = processor.jsonPathFromTemplateEntry((String) val);
				if (jsonPath == null) {
					return val;
				}
				Object value = context.read(jsonPath);
				if (value instanceof String) {
					return val;
				}
				return WRAPPER + val + WRAPPER;
			}
			else if (val instanceof String && processor.containsTemplateEntry((String) val)
					&& template.escapedBody().equals(val)) {
				return template.escapedBody();
			}
			return val;
		});
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body.
	 */
	String parseBody(List<?> list, ContentType contentType) {
		final List<Object> result = new ArrayList<>();
		list.forEach(l -> {
			if (l instanceof Map) {
				result.add(MapConverter.getStubSideValues(l));
			}
			else if (l instanceof List) {
				result.add(parseBody((List<?>) l, contentType));
			}
			else {
				result.add(parseBody(l, contentType));
			}
		});
		return parseBody(toJson(result), contentType);
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body.
	 */
	String parseBody(GString value, ContentType contentType) {
		Object processedValue = extractValue(value, contentType,
				(o) -> o instanceof DslProperty ? ((DslProperty<?>) o).getClientValue() : o);
		if (processedValue instanceof GString) {
			return parseBody(processedValue.toString(), contentType);
		}
		return parseBody(processedValue, contentType);
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body.
	 */
	String parseBody(String value, ContentType contentType) {
		return value;
	}

	private static String toJson(Object value) {
		try {
			if (value instanceof Map) {
				Object convertedMap = MapConverter.transformValues(value,
						(v) -> v instanceof GString ? ((GString) v).toString() : v);
				String jsonOutput = new ObjectMapper().writeValueAsString(convertedMap);
				return jsonOutput.replaceAll("\\\\\\\\\\\\", "\\\\");
			}
			return new ObjectMapper().writeValueAsString(value);
		}
		catch (JsonProcessingException e) {
			throw new IllegalArgumentException("The current object [" + value + "] could not be serialized");
		}
	}

	/**
	 * Attempts to guess the {@link ContentType} from body and headers. Returns
	 * {@link ContentType#UNKNOWN} if it fails to guess.
	 */
	protected ContentType tryToGetContentType(Object body, Headers headers) {
		ContentType contentType = ContentUtils.recognizeContentTypeFromHeader(headers);
		if (UNKNOWN == contentType) {
			if (body == null) {
				return UNKNOWN;
			}
			return getClientContentType(body);
		}
		return contentType;
	}

}
