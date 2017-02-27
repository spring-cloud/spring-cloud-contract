/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl

import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubMapping
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import java.util.regex.Pattern

trait WireMockStubVerifier {

	void stubMappingIsValidWireMockStub(String mappingDefinition) {
		StubMapping stubMapping = WireMockStubMapping.buildFrom(mappingDefinition)
		stubMapping.request.bodyPatterns.findAll { it.isPresent() && it instanceof RegexPattern }.every {
			Pattern.compile(it.getValue())
		}
		assert !mappingDefinition.contains('org.springframework.cloud.contract.spec.internal')
	}

	void stubMappingIsValidWireMockStub(Contract contractDsl) {
		stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub())
	}

}
