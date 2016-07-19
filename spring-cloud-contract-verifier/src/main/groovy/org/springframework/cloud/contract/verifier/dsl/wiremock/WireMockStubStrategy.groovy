/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.cloud.contract.verifier.file.ContractMetadata

import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping

/**
 * Converts a {@link ContractMetadata} into a WireMock stub
 *
 * @since 1.0.0
 */
@CompileStatic
class WireMockStubStrategy {

	private static final String STEP_START = "Started"
	private static final String STEP_PREFIX = "Step"
	private final WireMockRequestStubStrategy wireMockRequestStubStrategy
	private final WireMockResponseStubStrategy wireMockResponseStubStrategy
	private final Integer priority
	private final ContractMetadata contract
	private final String rootName

	WireMockStubStrategy(String rootName, ContractMetadata contract, org.springframework.cloud.contract.spec.Contract groovyDsl) {
		this.rootName = rootName
		this.contract = contract
		this.wireMockRequestStubStrategy = new WireMockRequestStubStrategy(groovyDsl)
		this.wireMockResponseStubStrategy = new WireMockResponseStubStrategy(groovyDsl)
		this.priority = groovyDsl.priority
	}

	/**
	 * Converts {@link ContractMetadata} to String version of {@link StubMapping}
	 */
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