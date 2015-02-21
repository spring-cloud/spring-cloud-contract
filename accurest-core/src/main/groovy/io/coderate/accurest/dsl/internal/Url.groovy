package io.coderate.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
class Url extends DslProperty {

	Url(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	Url(String bodyAsValue) {
		super(bodyAsValue)
	}
	
}
