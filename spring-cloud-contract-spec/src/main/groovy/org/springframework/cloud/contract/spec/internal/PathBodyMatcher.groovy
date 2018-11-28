package org.springframework.cloud.contract.spec.internal

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.ToString

/**
 * @author Marcin Grzejszczak
 */
@ToString(includePackage = false)
@Canonical
abstract class PathBodyMatcher implements BodyMatcher {

	String path
	MatchingTypeValue matchingTypeValue

	@Override
	MatchingType matchingType() {
		return this.matchingTypeValue.type
	}

	@Override
	String path() {
		return this.path
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
