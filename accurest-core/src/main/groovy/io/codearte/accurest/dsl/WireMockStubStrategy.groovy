package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
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
		StubMapping stubMapping = new StubMapping()
		RequestPattern request = wireMockRequestStubStrategy.buildClientRequestContent()
		ResponseDefinition response = wireMockResponseStubStrategy.buildClientResponseContent()
		if (priority) {
			stubMapping.priority = priority
		}
		stubMapping.request = request
		stubMapping.response = response
		return StubMapping.buildJsonStringFor(stubMapping)
	}
}