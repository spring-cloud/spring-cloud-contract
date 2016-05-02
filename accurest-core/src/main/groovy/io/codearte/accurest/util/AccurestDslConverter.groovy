package io.codearte.accurest.util

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.GroovyDsl
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Converts to Groovy DSL
 *
 * @author Marcin Grzejszczak
 */
@CompileStatic
class AccurestDslConverter {

	static GroovyDsl convert(String dsl) {
		return groovyShell().evaluate(dsl) as GroovyDsl
	}

	static GroovyDsl convert(File dsl) {
		return groovyShell().evaluate(dsl) as GroovyDsl
	}

	private static GroovyShell groovyShell() {
		return new GroovyShell(AccurestDslConverter.classLoader, new Binding(), new CompilerConfiguration(sourceEncoding: 'UTF-8'))
	}
}
