package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic

/**
 * Represents the type of matching the should be done against
 * the body of the request or response.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
@CompileStatic
enum MatchingType {
	EQUALITY, TYPE, DATE, TIME, TIMESTAMP, REGEX
}
