package io.coderate.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
class Headers {

	Set<Header> headers = []

	void header(Map<String, Object> singleHeader) {
		Map.Entry<String, Object> first = singleHeader.entrySet().first()
		headers << new Header(first?.key, first?.value)
	}

	void header(String headerKey, Object headerValue) {
		headers << new Header(headerKey, headerValue)
	}

}
