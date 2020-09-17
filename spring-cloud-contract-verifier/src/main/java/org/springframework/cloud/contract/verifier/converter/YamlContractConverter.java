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

package org.springframework.cloud.contract.verifier.converter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

/**
 * Simple converter from and to a {@link YamlContract} to a collection of
 * {@link Contract}.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.2.1
 */
public class YamlContractConverter implements ContractConverter<List<YamlContract>> {

	private static final Logger log = LoggerFactory.getLogger(YamlContractConverter.class);

	public static final YamlContractConverter INSTANCE = new YamlContractConverter();

	private final YAMLMapper mapper = new YAMLMapper();

	private final YamlToContracts yamlToContracts = new YamlToContracts();

	private final ContractsToYaml contractsToYaml = new ContractsToYaml();

	@Override
	public boolean isAccepted(File file) {
		String name = file.getName();
		boolean acceptFile = name.endsWith(".yml") || name.endsWith(".yaml");
		if (acceptFile) {
			try {
				this.yamlToContracts.convertFrom(file);
			}
			catch (Exception e) {
				log.warn("Error Processing yaml file. Skipping Contract Generation ", e);
				acceptFile = false;
			}
		}
		return acceptFile;
	}

	@Override
	public Collection<Contract> convertFrom(File file) {
		return this.yamlToContracts.convertFrom(file);
	}

	@Override
	public List<YamlContract> convertTo(Collection<Contract> contracts) {
		return this.contractsToYaml.convertTo(contracts);
	}

	@Override
	public Map<String, byte[]> store(List<YamlContract> contracts) {
		return contracts.stream().collect(toMap(this::name, this::getBytes));
	}

	@Override
	public List<YamlContract> read(byte[] bytes) {
		try {
			return singletonList(this.mapper.readValue(bytes, YamlContract.class));
		}
		catch (Exception e) {
			try {
				return this.mapper.readerForListOf(YamlContract.class).readValue(bytes);
			}
			catch (IOException ioException) {
				throw new RuntimeException(ioException);
			}
		}
	}

	protected String name(YamlContract contract) {
		return StringUtils.defaultIfEmpty(contract.name, String.valueOf(Math.abs((contract.hashCode())))) + ".yml";
	}

	protected byte[] getBytes(YamlContract yamlContract) {
		try {
			return this.mapper.writeValueAsString(yamlContract).getBytes();
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
