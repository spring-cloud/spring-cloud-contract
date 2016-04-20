package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
@EqualsAndHashCode
class StubData {
	final Integer port
	final List<GroovyDslWrapper> contracts
}
