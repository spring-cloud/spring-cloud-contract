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
class JsonPathBodyMatcher extends PathBodyMatcher {
}
