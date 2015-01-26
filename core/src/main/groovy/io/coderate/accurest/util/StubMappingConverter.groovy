package io.coderate.accurest.util

import groovy.json.JsonSlurper

import java.util.regex.Pattern

/**
 * @author Marcin Grzejszczak
 */
class StubMappingConverter {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(/^\$\{(.*)\}:\$\{(.*)\}$/)
	public static final int SERVER_SIDE_GROUP = 2

	static Map toStubMappingOnServerSide(File stubMapping) {
		def json = new JsonSlurper().parse(stubMapping)
		convertPlaceholders(json as Map, { String value ->
			getGroupFromMatchingPattern(value)
		})
		return json
	}

	private static void convertPlaceholders(Map map, Closure closure) {
		map.each {
			if (it instanceof Map.Entry) {
				Map.Entry entry = it as Map.Entry
				if (entry.value instanceof String) {
					String value = entry.value as String
					entry.value = closure(value)
				} else if (entry.value instanceof Map) {
					convertPlaceholders(entry.value as Map, closure)
				}
			}
		}
	}

	private static Object getGroupFromMatchingPattern(String value) {
		return value.matches(PLACEHOLDER_PATTERN) ? PLACEHOLDER_PATTERN.matcher(value)[0][SERVER_SIDE_GROUP] : value
	}

}
