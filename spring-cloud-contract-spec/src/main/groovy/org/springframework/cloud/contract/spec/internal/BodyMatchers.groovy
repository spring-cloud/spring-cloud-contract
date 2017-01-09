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
	private final List<BodyMatcher> jsonPathRegexMatchers = []

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
		return new MatchingTypeValue(MatchingType.REGEX, regex)
	}
}

@ToString
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
	String value() {
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
class MatchingTypeValue {
	MatchingType type

	/**
	 * Value of regular expression
	 */
	String value

	/**
	 * Min occurrence when matching by type
	 */
	Integer minTypeOccurrence

	/**
	 * Max occurrence when matching by type
	 */
	Integer maxTypeOccurrence
}
