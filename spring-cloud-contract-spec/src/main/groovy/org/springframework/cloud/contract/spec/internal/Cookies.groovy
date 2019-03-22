/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 * Represents a set of http cookies
 *
 * @author Alex Xandra Albert Sim
 * @since 1.2.5
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
class Cookies {

	Set<Cookie> entries = []

	void cookie(Map<String, Object> singleCookie) {
		Map.Entry<String, Object> first = singleCookie.entrySet().first()
		entries << new Cookie(first?.key, first?.value)
	}

	void cookie(String cookieKey, Object cookieValue) {
		entries << new Cookie(cookieKey, cookieValue)
	}

	void executeForEachCookie(Closure closure) {
		entries?.each {
			cookie -> closure(cookie)
		}
	}

	DslProperty matching(String value) {
		return new DslProperty(value)
	}

	boolean equals(o) {
		if (this.is(o)) {
			return true
		}
		if (getClass() != o.class) {
			return false
		}
		Cookies cookies = (Cookies) o
		if (cookies != cookies.entries) {
			return false
		}
		return true
	}

	int hashCode() {
		return entries.hashCode()
	}

	/**
	 * Converts the headers into their stub side representations and returns as
	 * a map of String key => Object value.
	 */
	Map<String, Object> asStubSideMap() {
		def acc = [:].withDefault { [] as Collection<Object> }
		return entries.
				inject(acc as Map<String, Object>) { Map<String, Object> map, Cookie cookie ->
					map[cookie.key] = cookie.clientValue
					return map
				} as Map<String, Object>
	}

	/**
	 * Converts the headers into their stub side representations and returns as
	 * a map of String key => Object value.
	 */
	Map<String, Object> asTestSideMap() {
		def acc = [:].withDefault { [] as Collection<Object> }
		return entries.
				inject(acc as Map<String, Object>) { Map<String, Object> map, Cookie cookie ->
					map[cookie.key] = cookie.serverValue
					return map
				} as Map<String, Object>
	}
}
