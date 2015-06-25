package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.WireMockStubStrategy

@CompileStatic
class DslToWireMockClientConverter extends DslToWireMockConverter {

	@Override
	String convertContent(String dslBody) {
		return new WireMockStubStrategy(createGroovyDSLfromStringContent(dslBody)).toWireMockClientStub()
	}
}
