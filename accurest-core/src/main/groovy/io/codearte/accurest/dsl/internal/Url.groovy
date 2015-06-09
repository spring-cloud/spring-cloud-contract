package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import static io.codearte.accurest.util.ValidateUtils.validateServerValueIsAvailable

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Url extends DslProperty {

	QueryParameters queryParameters

	Url(DslProperty prop) {
		super(prop.clientValue, prop.serverValue)
		validateServerValueIsAvailable(prop.serverValue, "Url")
	}

	Url(Object url) {
		super(url)
		validateServerValueIsAvailable(url, "Url")
	}

	void queryParameters(@DelegatesTo(QueryParameters) Closure closure) {
		this.queryParameters = new QueryParameters()
		closure.delegate = queryParameters
		closure()
	}

}
