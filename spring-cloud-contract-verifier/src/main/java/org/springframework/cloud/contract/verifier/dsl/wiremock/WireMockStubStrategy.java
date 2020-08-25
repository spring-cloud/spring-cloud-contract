/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

/**
 * Converts a {@link ContractMetadata} into a WireMock {@link StubMapping}.
 *
 * @since 1.0.0
 */
public class WireMockStubStrategy {

	private static final String STEP_START = "Started";

	private static final String STEP_PREFIX = "Step";

	private final WireMockRequestStubStrategy wireMockRequestStubStrategy;

	private final WireMockResponseStubStrategy wireMockResponseStubStrategy;

	private final Integer priority;

	private final ContractMetadata contract;

	private final String rootName;

	private final Contract groovyDsl;

	public WireMockStubStrategy(String rootName, ContractMetadata contract,
			Contract groovyDsl) {
		this.rootName = rootName;
		this.contract = contract;
		SingleContractMetadata singleContractMetadata = contract.forContract(groovyDsl);
		this.wireMockRequestStubStrategy = new WireMockRequestStubStrategy(groovyDsl,
				singleContractMetadata);
		this.wireMockResponseStubStrategy = new WireMockResponseStubStrategy(groovyDsl,
				singleContractMetadata);
		this.priority = groovyDsl.getPriority();
		this.groovyDsl = groovyDsl;
	}

	/**
	 * Converts {@link ContractMetadata} to {@link StubMapping}.
	 */
	public StubMapping toWireMockClientStub() {
		StubMapping stubMapping = new StubMapping();
		RequestPattern request = wireMockRequestStubStrategy.buildClientRequestContent();
		ResponseDefinition response = wireMockResponseStubStrategy
				.buildClientResponseContent();
		if (priority != null) {
			stubMapping.setPriority(priority);
		}
		stubMapping.setRequest(request);
		stubMapping.setResponse(response);
		if (request == null || response == null) {
			return null;
		}
		if (groovyDsl.getIgnored() || contract.getIgnored()) {
			return null;
		}
		if (contract.getOrder() != null) {
			stubMapping.setScenarioName("Scenario_" + rootName);
			stubMapping.setRequiredScenarioState(contract.getOrder() == 0 ? STEP_START
					: STEP_PREFIX + contract.getOrder());
			if (contract.getOrder() < contract.getGroupSize() - 1) {
				stubMapping.setNewScenarioState(STEP_PREFIX + (contract.getOrder() + 1));
			}
		}
		return stubMapping;
	}

}
