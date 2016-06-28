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

package org.springframework.cloud.contract.verifier.util

import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.verifier.dsl.internal.DslProperty

import static ContentUtils.extractValue
/**
 * Class that constructs a String from a body. The body can be a GString
 * or a map.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@CompileStatic
class BodyExtractor {

	private BodyExtractor() {}

	/**
	 * Returns the string representation of the body for the server side.
	 * That means that all the interpolations etc. will be resolved for the
	 * server side.
	 */
	static String extractTestValueFrom(Object body) {
		Object bodyValue = extractServerValueFromBody(body)
		String json = new JsonOutput().toJson(bodyValue)
		json = StringEscapeUtils.unescapeJavaScript(json)
		return trimRepeatedQuotes(json)
	}

	/**
	 * Returns the string representation of the body for the client side.
	 * That means that all the interpolations etc. will be resolved for the
	 * client side.
	 */
	static String extractStubValueFrom(Object body) {
		Object bodyValue = extractClientValueFromBody(body);
		String json = new JsonOutput().toJson(bodyValue)
		json = StringEscapeUtils.unescapeJavaScript(json)
		return trimRepeatedQuotes(json)
	}

	private static String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	static Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	static Object extractClientValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.clientValue })
		} else if (bodyValue instanceof DslProperty) {
			return extractClientValueFromBody(bodyValue.clientValue)
		} else {
			return MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.clientValue : it })
		}
	}
}
