package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.codearte.accurest.file.Contract

@CompileStatic
class WireMockStubStrategy {

	private static final String STEP_START = "Started"
	private static final String STEP_PREFIX = "Step"
	private final WireMockRequestStubStrategy wireMockRequestStubStrategy
	private final WireMockResponseStubStrategy wireMockResponseStubStrategy
	private final Integer priority
	private final Contract contract
	private final String rootName

	WireMockStubStrategy(String rootName, Contract contract, GroovyDsl groovyDsl) {
		this.rootName = rootName
		this.contract = contract
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

		if (!request || !response) {
			return ''
		}

		if (contract.order != null) {
			stubMapping.scenarioName = "Scenario_" + rootName
			stubMapping.requiredScenarioState = contract.order == 0 ? STEP_START : STEP_PREFIX + contract.order
			if (contract.order < contract.groupSize - 1) {
				stubMapping.newScenarioState = STEP_PREFIX + (contract.order + 1)
			}
		}

		return StubMapping.buildJsonStringFor(stubMapping)
	}
}