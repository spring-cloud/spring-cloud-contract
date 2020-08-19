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

package org.springframework.cloud.contract.verifier.builder;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

/**
 * All meta data required to generate a test class.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class GeneratedClassMetaData {

	final ContractVerifierConfigProperties configProperties;

	final Collection<ContractMetadata> listOfFiles;

	final String includedDirectoryRelativePath;

	final SingleTestGenerator.GeneratedClassData generatedClassData;

	GeneratedClassMetaData(ContractVerifierConfigProperties configProperties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath,
			SingleTestGenerator.GeneratedClassData generatedClassData) {
		this.configProperties = configProperties;
		this.listOfFiles = listOfFiles;
		this.includedDirectoryRelativePath = includedDirectoryRelativePath;
		this.generatedClassData = generatedClassData;
	}

	Collection<SingleContractMetadata> toSingleContractMetadata() {
		return this.listOfFiles.stream()
				.flatMap(metadata -> metadata.getConvertedContractWithMetadata().stream())
				.collect(Collectors.toList());
	}

	boolean isAnyJson() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isJson);
	}

	boolean isAnyIgnored() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isIgnored);
	}

	boolean isAnyXml() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isXml);
	}

	boolean isAnyHttp() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isHttp);
	}

	boolean isAnyMessaging() {
		return toSingleContractMetadata().stream()
				.anyMatch(SingleContractMetadata::isMessaging);
	}

}
