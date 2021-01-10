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

package org.springframework.cloud.contract.verifier.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.lang.GString;

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;

/**
 * Converts an object into either client or server side representation. Iterates over the
 * structure of an object (depending on whether it's an iterable or a primitive type
 * etc.), converts the {@link DslProperty} into their client / server representation and
 * returns the result
 *
 * @author Marcin Grzejszczak
 * @author Stessy Delcroix
 * @since 1.1.0
 */
public class MapConverter {

	private static final boolean STUB_SIDE = true;

	private static final boolean TEST_SIDE = false;

	/**
	 * Generic {@link Function} used to deserialize a json file.
	 */
	public static final Function<String, Object> JSON_PARSING_FUNCTION = (value) -> new JsonSlurper().parseText(value);

	/**
	 * Generic {@link Closure} used to deserialize a json file.
	 */
	public static final Closure<Object> JSON_PARSING_CLOSURE = new Closure<Object>(null) {
		public Object doCall(Object it) {
			return new JsonSlurper().parseText((String) it);
		}
	};

	/**
	 * Function used to return its input argument. {@link Function#identity()} cannot be
	 * used as the return type is Function&lt;T,T&gt;, whilst this function return type is
	 * Function&lt;T,R&gt;
	 */
	public static final Function<String, Object> IDENTITY = (value) -> value;

	private final TemplateProcessor templateProcessor;

	MapConverter() {
		this.templateProcessor = processor();
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor();
	}

	/**
	 * @return the object with client side values of
	 * {@link org.springframework.cloud.contract.spec.internal.DslProperty}
	 */
	public static Object transformToClientValues(Object value) {
		return transformValues(value, (v) -> v instanceof DslProperty ? ((DslProperty<?>) v).getClientValue() : v);
	}

	public static Object transformValues(Object value, Function<Object, ?> function) {
		return transformValues(value, function, JSON_PARSING_FUNCTION);
	}

	/**
	 * Iterates over the structure of the object and executes the function on each element
	 * of that structure.
	 * @return the transformed structure
	 */
	public static Object transformValues(Object value, Function<Object, ?> function,
			Function<String, Object> parsingFunction) {
		if (value instanceof String) {
			try {
				Object parsed = parsingFunction.apply((String) value);
				if (parsed instanceof Map) {
					return convert((Map) parsed, function, parsingFunction);
				}
				else if (parsed instanceof List) {
					return transformValues(parsed, function, parsingFunction);
				}
			}
			catch (Exception ignore) {
			}
			return extractValue(value, function);
		}
		else if (value instanceof Map) {
			return convert((Map) value, function, parsingFunction);
		}
		else if (value instanceof List) {
			return ((List) value).stream().map((v) -> transformValues(v, function, parsingFunction))
					.collect(Collectors.toList());
		}
		return transformValue(function, value, parsingFunction);
	}

	/**
	 * Transforms a value with the given function. Needs to be protected, otherwise method
	 * access exception will occur at runtime.
	 */
	protected static Object transformValue(Function<Object, ?> function, Object value,
			Function<String, Object> parsingFunction) {
		return extractValue(value, (val) -> {
			Object newValue = function.apply(val);
			if (newValue instanceof Map || newValue instanceof List || newValue instanceof String && val != null) {
				return transformValues(newValue, function, parsingFunction);
			}
			return newValue;
		});
	}

	private static Object extractValue(Object value, Function<Object, ?> function) {
		try {
			return function.apply(value);
		}
		catch (Exception ignore) {
			return value;
		}
	}

	private static Map<?, ?> convert(Map<?, ?> map, Function<Object, ?> function,
			Function<String, Object> parsingFunction) {
		Map<Object, Object> convertedMap = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			convertedMap.put(entry.getKey(), transformValues(entry.getValue(), function, parsingFunction));
		}
		return convertedMap;
	}

	public static Object getClientOrServerSideValues(Object json, boolean clientSide) {
		return getClientOrServerSideValues(json, clientSide, JSON_PARSING_FUNCTION);
	}

	/**
	 * If {@code clientSide} is {@code true} returns the client side value for the
	 * provided object.
	 */
	public static Object getClientOrServerSideValues(Object json, boolean clientSide,
			Function<String, Object> parsingFunction) {
		return transformValues(json, val -> {
			if (val instanceof DslProperty) {
				DslProperty<?> dslProperty = ((DslProperty<?>) val);
				return clientSide
						? getClientOrServerSideValues(dslProperty.getClientValue(), clientSide, parsingFunction)
						: getClientOrServerSideValues(dslProperty.getServerValue(), clientSide, parsingFunction);
			}
			else if (val instanceof GString) {
				ContentType type = new MapConverter().templateProcessor.containsJsonPathTemplateEntry(
						ContentUtils.extractValueForGString((GString) val, ContentUtils.GET_TEST_SIDE).toString())
								? ContentType.TEXT : null;
				return ContentUtils.extractValue((GString) val, type, (v) -> {
					if (v instanceof DslProperty) {
						return clientSide
								? getClientOrServerSideValues(((DslProperty<?>) v).getClientValue(), clientSide,
										parsingFunction)
								: getClientOrServerSideValues(((DslProperty<?>) v).getServerValue(), clientSide,
										parsingFunction);
					}
					return v;
				});
			}
			else if (val instanceof FromFileProperty) {
				return ((FromFileProperty) val).isByte() ? ((FromFileProperty) val).asBytes()
						: ((FromFileProperty) val).asString();
			}
			return val;
		}, parsingFunction);
	}

	public static Object getStubSideValues(Object json) {
		return getClientOrServerSideValues(json, STUB_SIDE, JSON_PARSING_FUNCTION);
	}

	public static Object getStubSideValues(Object json, Function<String, Object> parsingClosure) {
		return getClientOrServerSideValues(json, STUB_SIDE, parsingClosure);
	}

	public static Object getTestSideValues(Object json) {
		return getTestSideValues(json, JSON_PARSING_FUNCTION);
	}

	public static Object getTestSideValues(Object json, Function<String, Object> parsingClosure) {
		return getClientOrServerSideValues(json, TEST_SIDE, parsingClosure);
	}

	public static Object getTestSideValuesForText(Object json) {
		return getClientOrServerSideValues(json, TEST_SIDE, IDENTITY);
	}

	public static Object getStubSideValuesForNonBody(Object object) {
		return getClientOrServerSideValues(object, STUB_SIDE, IDENTITY);
	}

	public static Object getTestSideValuesForNonBody(Object object) {
		return getClientOrServerSideValues(object, TEST_SIDE, IDENTITY);
	}

}
