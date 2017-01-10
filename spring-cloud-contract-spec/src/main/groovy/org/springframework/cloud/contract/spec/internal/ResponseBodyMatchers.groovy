package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Body matchers for the response side (output message, REST response)
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ResponseBodyMatchers extends BodyMatchers {

	MatchingTypeValue byType() {
		return new MatchingTypeValue(MatchingType.TYPE)
	}

	MatchingTypeValue byType(@DelegatesTo(MatchingTypeValueHolder) Closure closure) {
		MatchingTypeValueHolder matchingTypeValue = new MatchingTypeValueHolder()
		closure.delegate = matchingTypeValue
		return closure() as MatchingTypeValue
	}
}

@CompileStatic
class MatchingTypeValueHolder {
	MatchingTypeValue matchingTypeValue = new MatchingTypeValue()

	MatchingTypeValue minOccurrence(int number) {
		this.matchingTypeValue.minTypeOccurrence = number
		return this.matchingTypeValue
	}

	MatchingTypeValue maxOccurrence(int number) {
		this.matchingTypeValue.maxTypeOccurrence = number
		return this.matchingTypeValue
	}
}
