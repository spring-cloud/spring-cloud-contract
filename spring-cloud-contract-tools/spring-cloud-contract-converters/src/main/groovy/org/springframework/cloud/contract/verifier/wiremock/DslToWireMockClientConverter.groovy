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

package org.springframework.cloud.contract.verifier.wiremock

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import java.nio.charset.StandardCharsets

/**
 * Converts DSLs to WireMock stubs
 *
 * @since 1.0.0
 */
@CompileStatic
class DslToWireMockClientConverter extends DslToWireMockConverter {

	@Override
	@Deprecated
	String convertContent(String rootName, ContractMetadata contract) {
		return convertASingleContract(rootName, contract, contract.convertedContract.first() ?: createGroovyDSLFromStringContent(
				contract.path.getText(StandardCharsets.UTF_8.toString())).first())
	}

	private String convertASingleContract(String rootName, ContractMetadata contract, Contract dsl) {
		return new WireMockStubStrategy(rootName, contract, dsl).toWireMockClientStub()
	}

	@Override
	Collection<String> convertContents(String rootName, ContractMetadata contract) {
		if (contract.convertedContract.size() == 1) {
			return [convertASingleContract(rootName, contract, contract.convertedContract.first())]
		}
		List<String> convertedContracts = []
		contract.convertedContract.eachWithIndex { Contract dsl, int index ->
			String name = "${rootName}_${index}"
			convertedContracts << convertASingleContract(name, contract, dsl)
		}
		return convertedContracts
	}
}
