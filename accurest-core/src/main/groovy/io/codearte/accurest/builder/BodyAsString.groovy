package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.util.MapConverter

import static io.codearte.accurest.util.ContentUtils.extractValue

/**
 * Class that constructs a String from Body
 *
 * @author Marcin Grzejszczak
 */
class BodyAsString {

	static String extractServerValueFrom(Object body) {
		Object bodyValue = extractServerValueFromBody(body)
		String json = new JsonOutput().toJson(bodyValue)
		json = StringEscapeUtils.unescapeJavaScript(json)
		return trimRepeatedQuotes(json)
	}

	static String extractClientValueFrom(Object body) {
		Object bodyValue = extractClientValueFromBody(body);
		String json = new JsonOutput().toJson(bodyValue)
		json = StringEscapeUtils.unescapeJavaScript(json)
		return trimRepeatedQuotes(json)
	}

	private static String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	private static Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	private static Object extractClientValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.clientValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.clientValue : it })
		}
		return bodyValue
	}
}
