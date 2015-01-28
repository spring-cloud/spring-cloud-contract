package io.coderate.accurest.util

import groovy.json.JsonException
import groovy.json.JsonSlurper

import java.util.regex.Pattern

/**
 * @author Marcin Grzejszczak
 */
class StubMappingConverter {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(/^\$\{(.*):(.*)\}$/)
	public static final int SERVER_SIDE_GROUP = 2

	static Map toStubMappingOnServerSide(File stubMapping) {
		def json = new JsonSlurper().parse(stubMapping)
		return convertPlaceholders(json as Map, { String value ->
			getGroupFromMatchingPattern(value)
		})
	}

	private static Map convertPlaceholders(Map map, Closure closure) {
		return map.collectEntries {
			key, value ->
				[key, transformValue(value, closure)]
		}
	}

	static def transformValue(def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertPlaceholders(json, closure)
				}
			} catch (JsonException ignore) {
				return closure(value)
			}
		} else if (value instanceof Map) {
			return convertPlaceholders(value as Map, closure)
		} else if (value instanceof List) {
			return value.collect({ transformValue(it, closure) })
		}

		return value
	}

	private static Object getGroupFromMatchingPattern(String value) {
		return value.matches(PLACEHOLDER_PATTERN) ? PLACEHOLDER_PATTERN.matcher(value)[0][SERVER_SIDE_GROUP] : value
	}

}
