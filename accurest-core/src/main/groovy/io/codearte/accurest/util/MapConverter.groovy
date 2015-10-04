package io.codearte.accurest.util

import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.Optional

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
		}.findAll {
			!(it.value instanceof Optional)
		}
	}

	static Object getClientOrServerSideValues(json, boolean clientSide) {
		return transformValues(json) {
			if (it instanceof DslProperty) {
				DslProperty dslProperty = ((DslProperty) it)
				return clientSide ?
						getClientOrServerSideValues(dslProperty.clientValue, clientSide) : getClientOrServerSideValues(dslProperty.serverValue, clientSide)
			} else if (it instanceof GString) {
				return ContentUtils.extractValue(it , null, {
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
}
