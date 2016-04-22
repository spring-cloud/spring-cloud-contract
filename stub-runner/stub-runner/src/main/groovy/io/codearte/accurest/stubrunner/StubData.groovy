package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import io.codearte.accurest.dsl.GroovyDsl

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
@EqualsAndHashCode
class StubData {
	final Integer port
	final List<GroovyDsl> contracts
}
