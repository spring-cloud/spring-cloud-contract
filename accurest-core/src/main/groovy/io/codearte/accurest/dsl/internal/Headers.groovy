package io.codearte.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
class Headers {

	Set<Header> entries = []

	void header(Map<String, Object> singleHeader) {
		Map.Entry<String, Object> first = singleHeader.entrySet().first()
		entries << new Header(first?.key, first?.value)
	}

	void header(String headerKey, Object headerValue) {
		entries << new Header(headerKey, headerValue)
	}

	void collect(Closure closure) {
		entries?.each {
			header -> closure(header)
		}
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false
		Headers headers = (Headers) o
		if (entries != headers.entries) return false
		return true
	}

	int hashCode() {
		return entries.hashCode()
	}
}
