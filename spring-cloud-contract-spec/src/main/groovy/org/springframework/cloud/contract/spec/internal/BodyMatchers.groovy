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
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Matching strategy of dynamic parts of the body.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
@CompileStatic
@ToString(includeFields = true, includePackage = false)
class BodyMatchers {
	private final RegexPatterns regexPatterns = new RegexPatterns()
	protected final List<BodyMatcher> jsonPathRegexMatchers = []

	void jsonPath(String path, MatchingTypeValue matchingType) {
		this.jsonPathRegexMatchers << new JsonPathBodyMatcher(path, matchingType)
	}

	boolean hasMatchers() {
		return !this.jsonPathRegexMatchers.empty
	}

	List<BodyMatcher> jsonPathMatchers() {
		return this.jsonPathRegexMatchers
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
		List<BodyMatcher> thisMatchers = this.jsonPathRegexMatchers
		List<BodyMatcher> thatMatchers = that.jsonPathRegexMatchers
		if (thisMatchers.size() != thatMatchers.size()) return false
		if (new HashSet<>(thisMatchers) != new HashSet(thatMatchers)) return false
		return true
	}

	int hashCode() {
		return (this.jsonPathRegexMatchers != null ? this.jsonPathRegexMatchers.hashCode() : 0)
	}
}

@ToString(includePackage = false)
@EqualsAndHashCode
@Canonical
@CompileStatic
class JsonPathBodyMatcher implements BodyMatcher {
	String jsonPath
	MatchingTypeValue matchingTypeValue

	@Override
	MatchingType matchingType() {
		return this.matchingTypeValue.type
	}

	@Override
	String path() {
		return this.jsonPath
	}

	@Override
	Object value() {
		return this.matchingTypeValue.value
	}

	@Override
	Integer minTypeOccurrence() {
		return this.matchingTypeValue.minTypeOccurrence
	}

	@Override
	Integer maxTypeOccurrence() {
		return this.matchingTypeValue.maxTypeOccurrence
	}
}

/**
 * Matching type with corresponding values
 */
@Canonical
@ToString(includePackage = false)
@EqualsAndHashCode
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
