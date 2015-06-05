package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Url extends DslProperty {

	QueryParameters queryParameters

	Url(DslProperty prop) {
		super(prop.clientValue, prop.serverValue)
	}

	Url(Object url) {
		super(url)
	}

	void queryParameters(@DelegatesTo(QueryParameters) Closure closure) {
		this.queryParameters = new QueryParameters()
		closure.delegate = queryParameters
		closure()
	}

}
