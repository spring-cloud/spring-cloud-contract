package org.springframework.cloud.contract.spec.internal

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
class BodyMatchers {
	private final RegexPatterns regexPatterns = new RegexPatterns()
	private final List<BodyMatcher> jsonPathMatchers = []

	void jsonPath(String path, MatchingTypeValue matchingType) {
		this.jsonPathMatchers << new JsonPathBodyMatcher(path, matchingType.value)
	}

	List<BodyMatcher> jsonPathMatchers() {
		return this.jsonPathMatchers
	}

	MatchingTypeValue byType() {
		return new MatchingTypeValue(MatchingType.TYPE)
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
		return new MatchingTypeValue(MatchingType.REGEX, regex)
	}
}

@ToString
@EqualsAndHashCode
@Canonical
@CompileStatic
class JsonPathBodyMatcher implements BodyMatcher {
	String jsonPath
	String value

	@Override
	String path() {
		return this.jsonPath
	}

	@Override
	String value() {
		return this.value
	}
}

@Canonical
class MatchingTypeValue {
	MatchingType type
	String value
}
