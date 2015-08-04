package io.codearte.accurest.dsl

import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
class WireMockStubStrategy {

	private final WireMockRequestStubStrategy wireMockRequestStubStrategy
	private final WireMockResponseStubStrategy wireMockResponseStubStrategy
	private final Integer priority

	WireMockStubStrategy(GroovyDsl groovyDsl) {
		this.wireMockRequestStubStrategy = new WireMockRequestStubStrategy(groovyDsl)
		this.wireMockResponseStubStrategy = new WireMockResponseStubStrategy(groovyDsl)
		this.priority = groovyDsl.priority
	}

	@CompileDynamic
	String toWireMockClientStub() {
		def wiremockStubDefinition = [request : wireMockRequestStubStrategy.buildClientRequestContent(),
									  response: wireMockResponseStubStrategy.buildClientResponseContent()]
		if (priority) {
			wiremockStubDefinition.priority = priority
		}
		return JsonOutput.prettyPrint(JsonOutput.toJson(wiremockStubDefinition))
	}
}