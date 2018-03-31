package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 * Matching strategy of dynamic parts of the request.
 *
 * @author Tim Ysewyn
 * @since 2.0.0
 */
@CompileStatic
@TypeChecked
@EqualsAndHashCode
@ToString(includeFields = true, includePackage = false)
class StubMatchers {

	BodyMatchers bodyMatchers = new BodyMatchers()

	void bodyMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		closure.delegate = this.bodyMatchers
		closure()
	}

}
