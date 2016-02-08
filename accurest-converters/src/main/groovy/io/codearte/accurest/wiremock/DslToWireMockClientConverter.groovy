package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.WireMockStubStrategy
import io.codearte.accurest.file.Contract

import java.nio.charset.StandardCharsets

@CompileStatic
class DslToWireMockClientConverter extends DslToWireMockConverter {

	@Override
	String convertContent(String rootName, Contract contract) {
		String dslContent = contract.path.getText(StandardCharsets.UTF_8.toString())
		return new WireMockStubStrategy(rootName, contract, createGroovyDSLfromStringContent(dslContent)).toWireMockClientStub()
	}
}
