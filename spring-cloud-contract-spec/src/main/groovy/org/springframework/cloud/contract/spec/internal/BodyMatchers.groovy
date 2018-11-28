/*
 *  Copyright 2013-2018 the original author or authors.
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
package org.springframework.cloud.contract.spec.internal

import java.util.regex.Pattern

import groovy.transform.Canonical
import groovy.transform.ToString

import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.ABSENT
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.ANYTHING
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.CASE_INSENSITIVE
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.CONTAINS
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.DOES_NOT_MATCH
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.EQUAL_TO
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.EQUAL_TO_XML
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.MATCHES

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
		this.matchers << new JsonPathBodyMatcher(path, matchingType)
	}

	// TODO: consider extracting xml-specific logic

	void xPathMatches(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, MATCHES)
	}

	void xPathContains(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, CONTAINS)
	}

	void xPathDoesNotMatch(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, DOES_NOT_MATCH)
	}

	void xPathAbsent(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, ABSENT)
	}

	void xPathEqualToXml(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, EQUAL_TO_XML)
	}

	void xPathAnything(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, ANYTHING)
	}

	void xPathEqualTo(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, EQUAL_TO)
	}

	void xPathCaseInsensitive(String xmlPath, MatchingTypeValue matchingTypeValue) {
		matchers << new XPathBodyMatcher(xmlPath, matchingTypeValue, CASE_INSENSITIVE)
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
		return new MatchingTypeValue(MatchingType.TIMESTAMP, this.regexPatterns.isoDateTime())
	}

	MatchingTypeValue byRegex(String regex) {
		assert regex
		return new MatchingTypeValue(MatchingType.REGEX, regex)
	}

	// Backward compatibility with RegexPatterns
	MatchingTypeValue byRegex(Pattern regex) {
		assert regex
		return new MatchingTypeValue(MatchingType.REGEX, regex)
	}

	MatchingTypeValue byEquality() {
		return new MatchingTypeValue(MatchingType.EQUALITY, null)
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (this.getClass() != o.class) return false
		BodyMatchers that = (BodyMatchers) o
		List<BodyMatcher> thisMatchers = this.matchers
		List<BodyMatcher> thatMatchers = that.matchers
		if (thisMatchers.size() != thatMatchers.size()) return false
		if (new HashSet<>(thisMatchers) != new HashSet(thatMatchers)) return false
		return true
	}

	int hashCode() {
		return (this.matchers != null ? this.matchers.hashCode() : 0)
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
