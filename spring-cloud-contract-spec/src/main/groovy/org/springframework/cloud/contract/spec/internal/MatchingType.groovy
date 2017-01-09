package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Represents the type of matching the should be done against
 * the body of the request or response.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
@CompileStatic
@PackageScope
enum MatchingType {
	TYPE, DATE, TIME, TIMESTAMP, REGEX
}
