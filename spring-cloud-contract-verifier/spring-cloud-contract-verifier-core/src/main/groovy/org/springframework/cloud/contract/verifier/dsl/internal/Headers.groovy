/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 * Represents a set of headers of a request / response or a message
 *
 * @since 1.0.0
 */
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

	void executeForEachHeader(Closure closure) {
		entries?.each {
			header -> closure(header)
		}
	}

	/**
	 * Converts the headers into their stub side representations and returns as
	 * a map of String key => Object value.
	 */
	Map<String , Object> asStubSideMap() {
		def acc = [:].withDefault { [] as Collection<Object> }
		return entries.inject(acc as Map<String, Object>) { Map<String, Object> map, Header header ->
			map[header.name] = header.clientValue
			return map
		} as Map<String , Object>
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
