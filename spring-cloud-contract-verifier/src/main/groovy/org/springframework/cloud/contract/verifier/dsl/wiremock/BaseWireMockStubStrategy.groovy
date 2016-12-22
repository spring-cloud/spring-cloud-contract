/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import groovy.json.JsonBuilder
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils

import static ContentUtils.extractValue
import static MapConverter.transformValues

/**
 * Common abstraction over WireMock Request / Response conversion implementations
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
abstract class BaseWireMockStubStrategy {

	/**
	 * Returns the stub side values from the object
	 */
	protected getStubSideValue(Object object) {
		return MapConverter.getStubSideValues(object)
	}

	private static Closure transform = {
		it instanceof DslProperty ? transformValues(it.clientValue, transform) : it
	}

	/**
	 * For the given {@link org.springframework.cloud.contract.verifier.util.ContentType} returns the String version of the body
	 */
	String parseBody(Object value, ContentType contentType) {
		return parseBody(value.toString(), contentType)
	}

	/**
	 * For the given {@link ContentType} returns the Boolean version of the body
	 */
	Boolean parseBody(Boolean value, ContentType contentType) {
		return value
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body
	 */
	String parseBody(Map map, ContentType contentType) {
		def transformedMap = MapConverter.getStubSideValues(map)
		return parseBody(toJson(transformedMap), contentType)
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body
	 */
	String parseBody(List list, ContentType contentType) {
		List result = []
		list.each {
			if (it instanceof Map) {
				result += MapConverter.getStubSideValues(it)
			} else {
				result += parseBody(it, contentType)
			}
		}
		return parseBody(toJson(result), contentType)
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body
	 */
	String parseBody(GString value, ContentType contentType) {
		Object processedValue = extractValue(value, contentType, { Object o -> o instanceof DslProperty ? o.clientValue : o })
		if (processedValue instanceof GString) {
			return parseBody(processedValue.toString(), contentType)
		}
		return parseBody(processedValue, contentType)
	}

	/**
	 * For the given {@link ContentType} returns the String version of the body
	 */
	String parseBody(String value, ContentType contentType) {
		return value
	}

	private static toJson(Object value) {
		return new JsonBuilder(value).toString()
	}

	/**
	 * Attempts to guess the {@link ContentType} from body and headers. Returns
	 * {@link ContentType#UNKNOWN} if it fails to guess.
	 */
	protected ContentType tryToGetContentType(Object body, Headers headers) {
		ContentType contentType = ContentUtils.recognizeContentTypeFromHeader(headers)
		if (contentType == ContentType.UNKNOWN) {
			if (!body) {
				return ContentType.UNKNOWN
			}
			return ContentUtils.getClientContentType(body)
		}
		return contentType
	}
}