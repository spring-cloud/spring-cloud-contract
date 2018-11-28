package org.springframework.cloud.contract.spec.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@ToString(includePackage = false)
@EqualsAndHashCode
class XPathBodyMatcher extends PathBodyMatcher {

	OPERATION_TYPE operationType

	XPathBodyMatcher(String path, MatchingTypeValue matchingTypeValue, OPERATION_TYPE operationType) {
		super(path, matchingTypeValue)
		this.operationType = operationType
	}

	static enum OPERATION_TYPE {
		MATCHES, DOES_NOT_MATCH, CONTAINS, ABSENT, EQUAL_TO_XML, EQUAL_TO, CASE_INSENSITIVE
	}
}