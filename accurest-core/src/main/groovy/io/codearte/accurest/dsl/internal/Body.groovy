package io.codearte.accurest.dsl.internal

import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.runtime.GStringImpl

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
class Body extends DslProperty {

	Body(Map<String, DslProperty> body) {
		super(extractValue(body, {it.clientValue}), extractValue(body, {it.serverValue}))
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> body, Closure valueProvider) {
		body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): valueProvider(entry.value)]
		} as Map<String, Object>
	}

	Body(List bodyAsList) {
		super(bodyAsList.collect { it.clientValue }, bodyAsList.collect { it.serverValue })
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		super(extractValue(bodyAsValue, {it.clientValue}), extractValue(bodyAsValue, {it.serverValue}))
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	private static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		GString clientGString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] clientValues = bodyAsValue.values.collect { it instanceof DslProperty ? valueProvider(it) : it } as Object[]
		return new JsonSlurper().parseText(new GStringImpl(clientValues, clientGString.strings).toString())
	}
	
}
