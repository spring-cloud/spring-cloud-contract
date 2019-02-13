/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import java.util.regex.Pattern

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Matching strategy of dynamic parts of the body.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 1.0.3
 */
@ToString(includeFields = true, includePackage = false)
class BodyMatchers {
	private final RegexPatterns regexPatterns = new RegexPatterns()
	protected final List<BodyMatcher> matchers = []

	void jsonPath(String path, MatchingTypeValue matchingType) {
		this.matchers << new PathBodyMatcher(path, matchingType)
	}

	/**
	 * Adds xPath matcher; even though same implementation as in {@link BodyMatchers#jsonPath(java.lang.String, org.springframework.cloud.contract.spec.internal.MatchingTypeValue)},
	 * added for logical coherence in xml
	 * @param xPath the xPath used to find the element to match
	 * @param matchingTypeValue to match the element found by the xPath against
	 */
	void xPath(String xPath, MatchingTypeValue matchingTypeValue) {
		matchers << new PathBodyMatcher(xPath, matchingTypeValue)
	}

	/**
	 * @deprecated use{@link #matchers()}
	 */
	@Deprecated
	List<BodyMatcher> jsonPathMatchers() {
		return matchers()
	}

	boolean hasMatchers() {
		return !this.matchers.empty
	}

	List<BodyMatcher> matchers() {
		return this.matchers
	}

	MatchingTypeValue byDate() {
		return new MatchingTypeValue(MatchingType.DATE, this.regexPatterns.isoDate())
	}

	MatchingTypeValue byTime() {
		return new MatchingTypeValue(MatchingType.TIME, this.regexPatterns.isoTime())
	}

	MatchingTypeValue byTimestamp() {
		return new MatchingTypeValue(MatchingType.TIMESTAMP, this.regexPatterns.
				isoDateTime())
	}

	RegexMatchingTypeValue byRegex(String regex) {
		return byRegex(Pattern.compile(regex))
	}

	RegexMatchingTypeValue byRegex(RegexProperty regex) {
		assert regex
		return new RegexMatchingTypeValue(MatchingType.REGEX, regex)
	}

	RegexMatchingTypeValue byRegex(Pattern regex) {
		assert regex
		return new RegexMatchingTypeValue(MatchingType.REGEX, new RegexProperty(regex))
	}

	MatchingTypeValue byEquality() {
		return new MatchingTypeValue(MatchingType.EQUALITY, null)
	}

	MatchingTypeValue byType(@DelegatesTo(MatchingTypeValueHolder) Closure closure) {
		MatchingTypeValueHolder matchingTypeValue = new MatchingTypeValueHolder()
		closure.delegate = matchingTypeValue
		closure()
		return matchingTypeValue.matchingTypeValue
	}

	boolean equals(o) {
		if (this.is(o)) {
			return true
		}
		if (this.getClass() != o.class) {
			return false
		}
		BodyMatchers that = (BodyMatchers) o
		List<BodyMatcher> thisMatchers = this.matchers
		List<BodyMatcher> thatMatchers = that.matchers
		if (thisMatchers.size() != thatMatchers.size()) {
			return false
		}
		if (new HashSet<>(thisMatchers) != new HashSet(thatMatchers)) {
			return false
		}
		return true
	}

	int hashCode() {
		return (this.matchers != null ? this.matchers.hashCode() : 0)
	}
}

/**
 * Matching type with corresponding values
 */

@ToString(includePackage = false)
@EqualsAndHashCode
class RegexMatchingTypeValue extends MatchingTypeValue {

	RegexMatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence, Integer maxTypeOccurrence) {
		super(type, value, minTypeOccurrence, maxTypeOccurrence)
	}

	RegexMatchingTypeValue(MatchingType type, Object value) {
		super(type, value)
	}

	RegexMatchingTypeValue asInteger() {
		return typed(Integer)
	}

	private RegexMatchingTypeValue typed(Class clazz) {
		assert this.value instanceof RegexProperty
		RegexProperty regexProperty = (RegexProperty) this.value
		return new RegexMatchingTypeValue(
				this.type, new RegexProperty(regexProperty.clientValue,
				regexProperty.serverValue, clazz),
				this.minTypeOccurrence, this.maxTypeOccurrence
		)
	}

	RegexMatchingTypeValue asDouble() {
		return typed(Double)
	}

	RegexMatchingTypeValue asFloat() {
		return typed(Float)
	}

	RegexMatchingTypeValue asLong() {
		return typed(Long)
	}

	RegexMatchingTypeValue asShort() {
		return typed(Short)
	}

	RegexMatchingTypeValue asString() {
		return typed(String)
	}

	RegexMatchingTypeValue asBooleanType() {
		return typed(Boolean)
	}
}

/**
 * Matching type with corresponding values
 */
@Canonical
@ToString(includePackage = false)
class MatchingTypeValue {
	MatchingType type

	/**
	 * Value to check
	 */
	Object value

	/**
	 * Min occurrence when matching by type
	 */
	Integer minTypeOccurrence

	/**
	 * Max occurrence when matching by type
	 */
	Integer maxTypeOccurrence
}

@CompileStatic
@ToString(includePackage = false)
@EqualsAndHashCode
class MatchingTypeValueHolder {
	MatchingTypeValue matchingTypeValue = new MatchingTypeValue(type: MatchingType.TYPE)

	MatchingTypeValue minOccurrence(int number) {
		this.matchingTypeValue.minTypeOccurrence = number
		return this.matchingTypeValue
	}

	MatchingTypeValue maxOccurrence(int number) {
		this.matchingTypeValue.maxTypeOccurrence = number
		return this.matchingTypeValue
	}

	MatchingTypeValue occurrence(int number) {
		this.matchingTypeValue.minTypeOccurrence = number
		this.matchingTypeValue.maxTypeOccurrence = number
		return this.matchingTypeValue
	}
}
