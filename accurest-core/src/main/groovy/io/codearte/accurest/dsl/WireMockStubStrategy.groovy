package io.codearte.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
class WireMockStubStrategy {

	private final WireMockRequestStubStrategy wireMockRequestStubStrategy
	private final WireMockResponseStubStrategy wireMockResponseStubStrategy

	WireMockStubStrategy(GroovyDsl groovyDsl) {
		this.wireMockRequestStubStrategy = new WireMockRequestStubStrategy(groovyDsl)
		this.wireMockResponseStubStrategy = new WireMockResponseStubStrategy(groovyDsl)
	}

	String toWireMockClientStub() {
		return JsonOutput.prettyPrint(JsonOutput.toJson([request : wireMockRequestStubStrategy.buildClientRequestContent(),
		                                                 response: wireMockResponseStubStrategy.buildClientResponseContent()]))
	}
}
