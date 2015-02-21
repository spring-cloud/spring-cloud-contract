package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic
import io.coderate.accurest.dsl.WiremockStubStrategy

@CompileStatic
class DslToWiremockServerConverter extends DslToWiremockConverter {

	@Override
	String convertContent(String dslBody) {
		return new WiremockStubStrategy(createGroovyDSLfromStringContent(dslBody)).toWiremockServerStub()
	}
}
