package io.codearte.accurest.util

import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.internal.DslProperty

/**
 * @author Marcin Grzejszczak
 */
class MapConverter {

	static def transformToClientValues(def value) {
		return transformValues(value) {
			it instanceof DslProperty ? it.clientValue : it
		}
	}

	static def transformValues(def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convert(json, closure)
				}
			} catch (Exception ignore) {
				return closure(value)
			}
		} else if (value instanceof Map) {
			return convert(value as Map, closure)
		} else if (value instanceof List) {
			return value.collect({ transformValues(it, closure) })
		}
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

}
