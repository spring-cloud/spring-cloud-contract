package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.codearte.accurest.dsl.GroovyDsl

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
class GroovyDslWrapper {

	@Delegate final GroovyDsl groovyDsl

	GroovyDslWrapper(GroovyDsl groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	boolean hasHttpPart() {
		return groovyDsl.request
	}
}
