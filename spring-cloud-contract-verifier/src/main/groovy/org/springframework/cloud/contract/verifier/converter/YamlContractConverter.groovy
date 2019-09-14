/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.converter

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter

/**
 * Simple converter from and to a {@link YamlContract} to a collection of {@link Contract}
 *
 * @since 1.2.1
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
@Slf4j
@CompileStatic
class YamlContractConverter implements ContractConverter<List<YamlContract>> {

	public static final YamlContractConverter INSTANCE = new YamlContractConverter()
	private final YAMLMapper mapper = new YAMLMapper()
	private final YamlToContracts yamlToContracts = new YamlToContracts()
	private final ContractsToYaml contractsToYaml = new ContractsToYaml()

	@Override
	boolean isAccepted(File file) {
		String name = file.getName()
		boolean acceptFile = name.endsWith(".yml") || name.endsWith(".yaml")
		if (acceptFile){
			try {
				this.yamlToContracts.convertFrom(file)
			} catch (e) {
				log.warn("Error Processing yaml file. Skipping Contract Generation ", e)
				acceptFile = false
			}
		}
		return acceptFile
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		return this.yamlToContracts.convertFrom(file)
	}

	@Override
	List<YamlContract> convertTo(Collection<Contract> contracts) {
		return this.contractsToYaml.convertTo(contracts)
	}

	@Override
	Map<String, byte[]> store(List<YamlContract> contracts) {
		return contracts.collectEntries {
			return [(name(it)):
							this.mapper.writeValueAsString(it).bytes]
		}
	}

	protected String name(YamlContract contract) {
		return (contract.name ?:
				String.valueOf(Math.abs(contract.hashCode()))) + ".yml"
	}
}
