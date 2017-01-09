package org.springframework.cloud.contract.spec.internal

/**
 * Represents the type of matching the should be done against
 * the body of the request or response.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
enum MatchingType {
	TYPE, DATE, TIME, TIMESTAMP, REGEX
}
