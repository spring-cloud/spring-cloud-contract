package io.coderate.accurest.dsl.internal

import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.runtime.GStringImpl

@ToString(includePackage = false, includeFields = true)
@EqualsAndHashCode(includeFields = true)
class Body {

	private Map<String, DslProperty> body
	private DslProperty bodyAsValue
	private List<DslProperty> bodyAsList

	Body() {
		this.body = [:]
	}

	Body(Map<String, DslProperty> body) {
		this.body = body
	}

	Body(List bodyAsList) {
		this.bodyAsList = bodyAsList
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		this.bodyAsValue = new DslProperty(getClientValue(bodyAsValue), getServerValue(bodyAsValue))
	}

	private def getClientValue(GString bodyAsValue) {
		GString clientGString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] clientValues = bodyAsValue.values.collect { it instanceof DslProperty ? it.clientValue : it } as Object[]
		return new JsonSlurper().parseText(new GStringImpl(clientValues, clientGString.strings).toString())
	}

	private def getServerValue(GString bodyAsValue) {
		GString clientGString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] serverValues = bodyAsValue.values.collect { it instanceof DslProperty ? it.serverValue : it } as Object[]
		return new JsonSlurper().parseText(new GStringImpl(serverValues, clientGString.strings).toString())
	}

	Body(DslProperty bodyAsValue) {
		this.bodyAsValue = bodyAsValue
	}

	Object forClientSide() {
		if (bodyAsValue) {
			return bodyAsValue.clientValue
		} else if (bodyAsList) {
			bodyAsList.collect { it.clientValue }
		}
		return body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): entry.value.clientValue]
		} as Map<String, Object>
	}

	Object forServerSide() {
		if (bodyAsValue) {
			return bodyAsValue.serverValue
		} else if (bodyAsList) {
			bodyAsList.collect { it.serverValue }
		}
		return body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): entry.value.serverValue]
		} as Map<String, Object>
	}
}
