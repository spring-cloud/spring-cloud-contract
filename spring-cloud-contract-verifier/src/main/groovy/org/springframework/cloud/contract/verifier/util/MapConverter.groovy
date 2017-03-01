/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util

import groovy.json.JsonSlurper
import org.springframework.cloud.contract.spec.internal.DslProperty
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

	private final TemplateProcessor templateProcessor

	MapConverter() {
		this.templateProcessor = processor()
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor()
	}

	/**
	 * Returns the object with client side values of {@link org.springframework.cloud.contract.spec.internal.DslProperty}
	 */
	static def transformToClientValues(def value) {
		return transformValues(value) {
			it instanceof DslProperty ? it.clientValue : it
		}
	}

	/**
	 * Iterates over the structure of the object and executes the closure
	 * on each element of that structure.
	 *
	 * Returns the transformed structure
	 */
	static def transformValues(def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convert(json, closure)
				}
			} catch (Exception ignore) {
			}
			return extractValue(value, closure)
		} else if (value instanceof Map) {
			return convert(value as Map, closure)
		} else if (value instanceof List) {
			return value.collect({ transformValues(it, closure) })
		}
		return transformValue(closure, value)
	}

	/**
	 * Transforms a value with the given closure. Needs to be protected, otherwise
	 * method access exception will occur at runtime.
	 */
	protected static Object transformValue(Closure closure, Object value) {
		return extractValue(value, { Object val->
			Object newValue = closure(val)
			if (newValue instanceof Map || newValue instanceof List || newValue instanceof String && value) {
				return transformValues(newValue, closure)
			}
			return newValue
		})
	}

	private static extractValue(Object value, Closure closure) {
		try {
			return closure(value)
		} catch (Exception ignore) {
			return value
		}
	}

	private static Map convert(Map map, Closure closure) {
		return map.collectEntries {
			key, value ->
				[key, transformValues(value, closure)]
		}
	}

	/**
	 * If {@code clientSide} is {@code true} returns the client side value for the
	 * provided object
	 */
	static Object getClientOrServerSideValues(json, boolean clientSide) {
		return transformValues(json) {
			if (it instanceof DslProperty) {
				DslProperty dslProperty = ((DslProperty) it)
				return clientSide ?
						getClientOrServerSideValues(dslProperty.clientValue, clientSide) : getClientOrServerSideValues(dslProperty.serverValue, clientSide)
			} else if (it instanceof GString) {
				ContentType type = new MapConverter().templateProcessor.containsJsonPathTemplateEntry(
							ContentUtils.extractValueForGString(it, ContentUtils.GET_TEST_SIDE).toString()
					) ? ContentType.TEXT : null
				return ContentUtils.extractValue(it , type, {
					if (it instanceof DslProperty) {
						return clientSide ?
								getClientOrServerSideValues((it as DslProperty).clientValue, clientSide) : getClientOrServerSideValues((it as DslProperty).serverValue, clientSide)
					}
					return it
				})
			}
			return it
		}
	}

	static Object getStubSideValues(json) {
		return getClientOrServerSideValues(json, STUB_SIDE)
	}

	static Object getTestSideValues(json) {
		return getClientOrServerSideValues(json, TEST_SIDE)
	}
}
