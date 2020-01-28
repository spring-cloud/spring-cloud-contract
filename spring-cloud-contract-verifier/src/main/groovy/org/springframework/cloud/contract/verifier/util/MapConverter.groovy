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

package org.springframework.cloud.contract.verifier.util

import java.util.function.Function

import groovy.json.JsonSlurper

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor
import org.springframework.cloud.contract.verifier.template.TemplateProcessor

/**
 * Converts an object into either client or server side representation.
 * Iterates over the structure of an object (depending on whether it's an
 * iterable or a primitive type etc.), converts the {@link DslProperty} into their
 * client / server representation and returns the result
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.1.0
 */
class MapConverter {

	public static final boolean STUB_SIDE = true
	public static final boolean TEST_SIDE = false
	public static final Closure JSON_PARSING_CLOSURE = { String value ->
		new JsonSlurper().parseText(value)
	}
	public static final Function<String, Object> JSON_PARSING_FUNCTION = { String value ->
		new JsonSlurper().parseText(value)
	} as Function

	private final TemplateProcessor templateProcessor

	MapConverter() {
		this.templateProcessor = processor()
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor()
	}

	/**
	 * @return the object with client side values of {@link org.springframework.cloud.contract.spec.internal.DslProperty}
	 */
	static def transformToClientValues(def value) {
		return transformValues(value) {
			it instanceof DslProperty ? it.clientValue : it
		}
	}

	static Closure fromFunction(Function function) {
		return {
			function.apply(it)
		}
	}

	/**
	 * Iterates over the structure of the object and executes the closure
	 * on each element of that structure.
	 *
	 * @return the transformed structure
	 */
	static def transformValues(def value, Closure closure,
			Closure parsingClosure = JSON_PARSING_CLOSURE) {
		if (value instanceof String && value) {
			try {
				def parsed = parsingClosure(value)
				if (parsed instanceof Map) {
					return convert(parsed, closure, parsingClosure)
				}
				else if (parsed instanceof List) {
					return transformValues(parsed, closure, parsingClosure)
				}
			}
			catch (Exception ignore) {
			}
			return extractValue(value, closure)
		}
		else if (value instanceof Map) {
			return convert(value as Map, closure, parsingClosure)
		}
		else if (value instanceof List) {
			return value.collect({ transformValues(it, closure, parsingClosure) })
		}
		return transformValue(closure, value, parsingClosure)
	}

	/**
	 * Transforms a value with the given closure. Needs to be protected, otherwise
	 * method access exception will occur at runtime.
	 */
	protected static Object transformValue(Closure closure, Object value, Closure parsingClosure) {
		return extractValue(value, { Object val ->
			Object newValue = closure(val)
			if (newValue instanceof Map || newValue instanceof List || newValue instanceof String && value) {
				return transformValues(newValue, closure, parsingClosure)
			}
			return newValue
		})
	}

	private static extractValue(Object value, Closure closure) {
		try {
			return closure(value)
		}
		catch (Exception ignore) {
			return value
		}
	}

	private static Map convert(Map map, Closure closure, Closure parsingClosure) {
		return map.collectEntries {
			key, value ->
				[key, transformValues(value, closure, parsingClosure)]
		}
	}

	/**
	 * If {@code clientSide} is {@code true} returns the client side value for the
	 * provided object
	 */
	static Object getClientOrServerSideValues(json, boolean clientSide,
			Closure parsingClosure = JSON_PARSING_CLOSURE) {
		return transformValues(json, {
			if (it instanceof DslProperty) {
				DslProperty dslProperty = ((DslProperty) it)
				return clientSide ?
						getClientOrServerSideValues(dslProperty.clientValue, clientSide, parsingClosure) :
						getClientOrServerSideValues(dslProperty.serverValue, clientSide, parsingClosure)
			}
			else if (it instanceof GString) {
				ContentType type = new MapConverter().templateProcessor.
						containsJsonPathTemplateEntry(
								ContentUtils.
										extractValueForGString(it, ContentUtils.GET_TEST_SIDE).
										toString()
						) ? ContentType.TEXT : null
				return ContentUtils.extractValue(it, type, {
					if (it instanceof DslProperty) {
						return clientSide ?
								getClientOrServerSideValues((it as DslProperty).clientValue, clientSide, parsingClosure) :
								getClientOrServerSideValues((it as DslProperty).serverValue, clientSide, parsingClosure)
					}
					return it
				})
			}
			else if (it instanceof FromFileProperty) {
				return it.isByte() ? it.asBytes() : it.asString()
			}
			return it
		}, parsingClosure)
	}

	static Object getStubSideValues(json, Closure parsingClosure = JSON_PARSING_CLOSURE) {
		return getClientOrServerSideValues(json, STUB_SIDE, parsingClosure)
	}

	static Object getTestSideValues(json, Function function) {
		return getClientOrServerSideValues(json, TEST_SIDE, { function.apply(it) })
	}

	static Object getTestSideValues(json, Closure parsingClosure = JSON_PARSING_CLOSURE) {
		return getClientOrServerSideValues(json, TEST_SIDE, parsingClosure)
	}

	static Object getTestSideValuesForText(json) {
		return getClientOrServerSideValues(json, TEST_SIDE, Closure.IDENTITY)
	}

	static Object getStubSideValuesForNonBody(object) {
		return getClientOrServerSideValues(object, STUB_SIDE, Closure.IDENTITY)
	}

	static Object getTestSideValuesForNonBody(object) {
		return getClientOrServerSideValues(object, TEST_SIDE, Closure.IDENTITY)
	}
}
