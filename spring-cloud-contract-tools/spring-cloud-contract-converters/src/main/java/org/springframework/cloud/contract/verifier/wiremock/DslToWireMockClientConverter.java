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

package org.springframework.cloud.contract.verifier.wiremock;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

/**
 * Converts DSLs to WireMock stubs.
 *
 * @since 1.0.0
 */
public class DslToWireMockClientConverter extends DslToWireMockConverter {

	private String convertASingleContract(String rootName, ContractMetadata contract,
			Contract dsl) {
		return new WireMockStubStrategy(rootName, contract, dsl).toWireMockClientStub();
	}

	@Override
	public Map<Contract, String> convertContents(String rootName,
			ContractMetadata contract) {
		List<Contract> httpContracts = httpContracts(contract);
		if (httpContracts.isEmpty()) {
			return new HashMap<>();
		}
		if (contract.getConvertedContract().size() == 1) {
			return Collections.singletonMap(
					first((List<Contract>) contract.getConvertedContract()),
					convertASingleContract(rootName, contract,
							first((List<Contract>) contract.getConvertedContract())));
		}
		return convertContracts(rootName, contract, httpContracts);
	}

	private List<Contract> httpContracts(ContractMetadata contract) {
		return contract.getConvertedContract().stream()
				.filter(c -> c.getRequest() != null).collect(Collectors.toList());
	}

	private Map<Contract, String> convertContracts(String rootName,
			ContractMetadata contract, List<Contract> contractsWithRequest) {
		Map<Contract, String> convertedContracts = new LinkedHashMap<>();
		for (int i = 0; i < contractsWithRequest.size(); i++) {
			Contract dsl = contractsWithRequest.get(i);
			String name = StringUtils.hasText(dsl.getName())
					? NamesUtil.convertIllegalPackageChars(dsl.getName())
					: rootName + "_" + i;
			convertedContracts.put(dsl, convertASingleContract(name, contract, dsl));
		}
		return convertedContracts;
	}

	private static <T> T first(List<T> self) {
		if (self.isEmpty()) {
			throw new NoSuchElementException(
					"Cannot access first() element from an empty List");
		}
		return self.get(0);
	}

}
