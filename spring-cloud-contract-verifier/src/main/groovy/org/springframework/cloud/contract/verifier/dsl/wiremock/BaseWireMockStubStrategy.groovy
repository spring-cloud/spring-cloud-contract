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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonBuilder
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.json.JSONObject

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor
import org.springframework.cloud.contract.verifier.template.TemplateProcessor
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.MapConverter

import static ContentUtils.extractValue
import static org.springframework.cloud.contract.verifier.util.MapConverter.transformValues
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

	private static final String WRAPPER = "UNQUOTE_ME"

	protected final TemplateProcessor processor
	protected final ContractTemplate template
	protected final Contract contract

	protected BaseWireMockStubStrategy(Contract contract) {
		this.processor = processor()
		this.template = contractTemplate()
		this.contract = contract
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor()
	}

	private ContractTemplate contractTemplate() {
		return new HandlebarsTemplateProcessor()
	}

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
	 * For the given {@link ContentType} returns the String version of the body
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
		transformedMap = transformMapIfRequestPresent(transformedMap)
		String json = toJson(transformedMap)
		// the space is important cause at the end of the json body you also have a }
		// you can't have 4 } next to each other
		String unquotedJson = json.replace('"' + WRAPPER, '').replace(WRAPPER + '"', ' ')
		String unescapedJson = unquotedJson.replace("\\/", "/")
		return parseBody(unescapedJson, contentType)
	}

	private Object transformMapIfRequestPresent(Object transformedMap) {
		def requestBody = contract.request.body
		if (requestBody == null) {
			return transformedMap
		}
		String testSideBody = toJson(
				MapConverter.getTestSideValues(requestBody))
		DocumentContext context = JsonPath.parse(testSideBody)
		return processEntriesForTemplating(transformedMap, context)
	}

	private Object processEntriesForTemplating(transformedMap, DocumentContext context) {
		return transformValues(transformedMap, {
			if (it instanceof String && processor.containsJsonPathTemplateEntry(it)) {
				String jsonPath = processor.jsonPathFromTemplateEntry(it)
				if (!jsonPath) {
					return it
				}
				Object value = context.read(jsonPath)
				if (value instanceof String) {
					return it
				}
				return "${WRAPPER}${it}${WRAPPER}"
			} else if (it instanceof String && processor.containsTemplateEntry(it) && template.body() == it) {
				return template.escapedBody()
			}
			return it
		})
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

	private static String toJson(Object value) {
		if (value instanceof Map) {
			Map convertedMap = MapConverter.transformValues(value) {
				it instanceof GString ? it.toString() : it
			} as Map
			return new JSONObject(new JsonBuilder(convertedMap).toString())
		}
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