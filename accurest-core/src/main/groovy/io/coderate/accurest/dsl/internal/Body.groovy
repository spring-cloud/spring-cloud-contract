package io.coderate.accurest.dsl.internal

import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.runtime.GStringImpl

@ToString(includePackage = false, includeFields = true)
@EqualsAndHashCode(includeFields = true)
class Body extends DslProperty {

	Body() {
		super(null)
	}

	Body(Map<String, DslProperty> body) {
		super(body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): entry.value.clientValue]
		} as Map<String, Object>,
				body.collectEntries { Map.Entry<String, DslProperty> entry ->
					[(entry.key): entry.value.serverValue]
				} as Map<String, Object>)
	}

	Body(List bodyAsList) {
		super(bodyAsList.collect { it.clientValue }, bodyAsList.collect { it.serverValue })
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		super(extractClientValue(bodyAsValue), extractServerValue(bodyAsValue))
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	private static def extractClientValue(GString bodyAsValue) {
		GString clientGString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] clientValues = bodyAsValue.values.collect { it instanceof DslProperty ? it.clientValue : it } as Object[]
		return new JsonSlurper().parseText(new GStringImpl(clientValues, clientGString.strings).toString())
	}

	private static def extractServerValue(GString bodyAsValue) {
		GString clientGString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] serverValues = bodyAsValue.values.collect { it instanceof DslProperty ? it.serverValue : it } as Object[]
		return new JsonSlurper().parseText(new GStringImpl(serverValues, clientGString.strings).toString())
	}
	
}
