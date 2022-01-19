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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.core.model.OptionalBody;
import au.com.dius.pact.core.model.Request;
import au.com.dius.pact.core.model.Response;
import au.com.dius.pact.core.model.generators.Generator;
import au.com.dius.pact.core.model.messaging.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import groovy.lang.GString;
import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.contract.spec.internal.ClientDslProperty;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ServerDslProperty;
import org.springframework.cloud.contract.verifier.util.ContentUtils;

/**
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
final class BodyConverter {

	private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.INSTANCE.getMapper();

	private BodyConverter() {

	}

	static DslPart toPactBody(DslProperty<?> dslProperty, Function<DslProperty<?>, Object> dslPropertyValueExtractor) {
		return traverse(dslProperty, null, dslPropertyValueExtractor);
	}

	private static DslPart traverse(Object value, DslPart parent,
			Function<DslProperty<?>, Object> dslPropertyValueExtractor) {
		boolean isRoot = parent == null;
		Object v = value;
		if (v instanceof DslProperty) {
			v = dslPropertyValueExtractor.apply((DslProperty<?>) v);
		}
		if (v instanceof GString) {
			v = ContentUtils.extractValue((GString) v, dslPropertyValueExtractor);
		}
		if (v instanceof String) {
			String stringValue = ((String) v).trim();
			if (StringUtils.startsWith(stringValue, "{") && StringUtils.endsWith(stringValue, "}")) {
				try {
					v = OBJECT_MAPPER.readValue(stringValue, Object.class);
				}
				catch (JsonProcessingException ex) { /*
														 * it wasn't a JSON string after
														 * all...
														 */
				}
			}
		}
		DslPart p = isRoot ? createRootDslPart(v) : parent;
		if (v instanceof Map) {
			processMap((Map) v, (PactDslJsonBody) p, dslPropertyValueExtractor);
		}
		else if (v instanceof Collection) {
			processCollection((Collection) v, (PactDslJsonArray) p, dslPropertyValueExtractor);
		}
		return p;
	}

	private static DslPart createRootDslPart(Object value) {
		return value instanceof Collection ? new PactDslJsonArray() : new PactDslJsonBody();
	}

	private static void processCollection(Collection values, PactDslJsonArray jsonArray,
			Function<DslProperty<?>, Object> dslPropertyValueExtractor) {
		values.forEach(v -> {

			if (v instanceof DslProperty) {
				v = dslPropertyValueExtractor.apply((DslProperty<?>) v);
			}
			if (v instanceof GString) {
				v = ContentUtils.extractValue((GString) v, dslPropertyValueExtractor);
			}
			if (v == null) {
				jsonArray.nullValue();
			}
			else if (v instanceof String) {
				jsonArray.string((String) v);
			}
			else if (v instanceof Number) {
				jsonArray.number((Number) v);
			}
			else if (v instanceof Map) {
				PactDslJsonBody current = jsonArray.object();
				traverse(v, current, dslPropertyValueExtractor);
				current.closeObject();
			}
			else if (v instanceof Collection) {
				PactDslJsonArray current = jsonArray.array();
				traverse(v, current, dslPropertyValueExtractor);
				current.closeArray();
			}
		});
	}

	private static void processMap(Map<String, Object> values, PactDslJsonBody jsonObject,
			Function<DslProperty<?>, Object> dslPropertyValueExtractor) {
		values.forEach((k, v) -> {
			if (v instanceof DslProperty) {
				v = dslPropertyValueExtractor.apply((DslProperty<?>) v);
			}
			if (v instanceof GString) {
				v = ContentUtils.extractValue((GString) v, dslPropertyValueExtractor);
			}
			if (v == null) {
				jsonObject.nullValue(k);
			}
			else if (v instanceof Boolean) {
				jsonObject.booleanValue(k, (Boolean) v);
			}
			else if (v instanceof String) {
				jsonObject.stringType(k, (String) v);
			}
			else if (v instanceof Number) {
				jsonObject.numberValue(k, (Number) v);
			}
			else if (v instanceof Map) {
				PactDslJsonBody current = jsonObject.object(k);
				traverse(v, current, dslPropertyValueExtractor);
				current.closeObject();
			}
			else if (v instanceof Collection) {
				PactDslJsonArray current = jsonObject.array(k);
				traverse(v, current, dslPropertyValueExtractor);
				current.closeArray();
			}
		});
	}

	static Object toSCCBody(Request request) {
		Object body = parseBody(request.getBody());
		if (request.getGenerators().isNotEmpty() && request.getGenerators().getCategories()
				.containsKey(au.com.dius.pact.core.model.generators.Category.BODY)) {
			applyGenerators(body,
					request.getGenerators().getCategories().get(au.com.dius.pact.core.model.generators.Category.BODY),
					currentValue -> pattern -> generatedValue -> new DslProperty<>(
							new ClientDslProperty(pattern, generatedValue), currentValue));
		}
		return body;
	}

	static Object toSCCBody(Response response) {
		Object body = parseBody(response.getBody());
		if (response.getGenerators().isNotEmpty() && response.getGenerators().getCategories()
				.containsKey(au.com.dius.pact.core.model.generators.Category.BODY)) {
			applyGenerators(body,
					response.getGenerators().getCategories().get(au.com.dius.pact.core.model.generators.Category.BODY),
					currentValue -> pattern -> generatedValue -> new DslProperty<>(currentValue,
							new ServerDslProperty(pattern, generatedValue)));
		}
		return body;
	}

	static Object toSCCBody(Message message) {
		Object body = parseBody(message.getContents());
		if (message.getGenerators().isNotEmpty() && message.getGenerators().getCategories()
				.containsKey(au.com.dius.pact.core.model.generators.Category.BODY)) {
			applyGenerators(body,
					message.getGenerators().getCategories().get(au.com.dius.pact.core.model.generators.Category.BODY),
					currentValue -> pattern -> generatedValue -> new DslProperty<>(
							new ClientDslProperty(pattern, generatedValue), currentValue));
		}
		return body;
	}

	private static Object parseBody(OptionalBody optionalBody) {
		if (optionalBody.isPresent()) {
			try {
				return OBJECT_MAPPER.readValue(optionalBody.getValue(), Object.class);
			}
			catch (IOException e) {
				throw new RuntimeException("Body could not be read", e);
			}
		}
		else {
			return optionalBody.getValue();
		}
	}

	private static void applyGenerators(Object body, Map<String, Generator> generatorsPerPath,
			Function<Pattern, Function<Object, Function<Object, DslProperty<Object>>>> dslPropertyProvider) {
		Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonProvider(OBJECT_MAPPER))
				.build();
		generatorsPerPath.forEach((path, generator) -> {
			Path compiledPath = PathCompiler.compile(path);
			EvaluationContext evaluationContext = compiledPath.evaluate(body, body, configuration, true);
			evaluationContext.updateOperations().forEach(pathRef -> {
				pathRef.convert(((currentValue, config) -> ValueGeneratorConverter.convert(generator,
						(pattern, generatedValue) -> dslPropertyProvider.apply(pattern).apply(generatedValue)
								.apply(currentValue))),
						configuration);
			});
		});
	}

}
