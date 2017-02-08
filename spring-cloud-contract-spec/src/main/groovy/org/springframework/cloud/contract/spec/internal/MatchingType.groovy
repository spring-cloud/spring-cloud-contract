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
	/**
	 * The default approach - ensures that the value received
	 * in a body for the given path is as presented in the contract body
	 */
	EQUALITY,
	/**
	 * Verification by type - is the type received in a body
	 * for the given path is of the same type. If it's a collection
	 * you can verify number of occurrences
	 */
	TYPE,
	/**
	 * Special version of regex for date check
	 */
	DATE,
	/**
	 * Special version of regex for time check
	 */
	TIME,
	/**
	 * Special version of regex for timestamp check
	 */
	TIMESTAMP,
	/**
	 * Verification if the value for the given path matches the
	 * provided regex
	 */
	REGEX

	static boolean regexRelated(MatchingType type) {
		if (type == EQUALITY || type == TYPE ) {
			return false
		}
		return true
	}
}
