package org.springframework.cloud.contract.spec.internal


import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@TupleConstructor(includeSuperFields = true)
@ToString(includePackage = false)
@EqualsAndHashCode
@PackageScope
class XmlPathBodyMatcher extends PathBodyMatcher {

	OPERATION_TYPE operationType

	static enum OPERATION_TYPE {
		MATCHES, DOES_NOT_MATCH, CONTAINS, ABSENT, EQUAL_TO_XML, ANYTHING, EQUAL_TO,
		CASE_INSENSITIVE
	}
}