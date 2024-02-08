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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import java.util.regex.Pattern

import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata

trait WireMockStubVerifier {

	void stubMappingIsValidWireMockStub(String mappingDefinition) {
		StubMapping stubMapping = WireMockStubMapping.buildFrom(mappingDefinition)
		stubMappingIsValidWireMockStub(stubMapping)
	}
	
	void stubMappingIsValidWireMockStub(StubMapping mappingDefinition) {
		mappingDefinition.request.bodyPatterns.findAll { it.isPresent() && it instanceof RegexPattern }.every {
			Pattern.compile(it.getValue())
		}
		String definition = mappingDefinition.toString()
		assert !definition.contains('org.springframework.cloud.contract.spec.internal')
		assert !definition.contains('cursor')
	}

	void stubMappingIsValidWireMockStub(Contract contractDsl) {
		stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub())
	}
}
