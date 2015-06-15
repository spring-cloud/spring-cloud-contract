package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Body extends DslProperty {

	Body(Map<String, DslProperty> body) {
		super(extractValue(body, { DslProperty p -> p.clientValue}), extractValue(body, {DslProperty p -> p.serverValue}))
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> body, Closure valueProvider) {
		body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): valueProvider(entry.value)]
		} as Map<String, Object>
	}

	Body(List<DslProperty> bodyAsList) {
		super(bodyAsList.collect { DslProperty p -> p.clientValue }, bodyAsList.collect { DslProperty p -> p.serverValue })
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		super(bodyAsValue, bodyAsValue)
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	Body(MatchingStrategy matchingStrategy) {
		super(matchingStrategy, matchingStrategy)
	}


}
