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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

/**
 * Converts contracts into their stub representation.
 *
 * @param <T> - type of stub mapping
 * @since 1.1.0
 */
public interface StubGenerator<T> {

	/**
	 * @param mapping - potential stub mapping mapping
	 * @return {@code true} if this converter could have generated this mapping stub.
	 */
	default boolean canReadStubMapping(File mapping) {
		return mapping.getName().endsWith(fileExtension());
	}

	/**
	 * @param rootName - root name of the contract
	 * @param content - metadata of the contract
	 * @return the collection of converted contracts into stubs. One contract can result
	 * in multiple stubs.
	 */
	Map<Contract, String> convertContents(String rootName, ContractMetadata content);

	/**
	 * Post process a generated stub mapping.
	 * @param stubMapping - mapping of a stub
	 * @param contract - contract for which stub was generated
	 * @return the converted stub mapping
	 */
	default T postProcessStubMapping(T stubMapping, Contract contract) {
		List<StubPostProcessor> processors = StubPostProcessor.PROCESSORS.stream().filter(p -> p.isApplicable(contract))
				.collect(Collectors.toList());
		if (processors.isEmpty()) {
			return defaultStubMappingPostProcessing(stubMapping, contract);
		}
		T stub = stubMapping;
		for (StubPostProcessor processor : processors) {
			stub = (T) processor.postProcess(stub, contract);
		}
		return stub;
	}

	/**
	 * Stub mapping to chose when no post processors where found on the classpath.
	 * @param stubMapping - mapping of a stub
	 * @param contract - contract for which stub was generated
	 * @return the converted stub mapping
	 */
	default T defaultStubMappingPostProcessing(T stubMapping, Contract contract) {
		return stubMapping;
	}

	/**
	 * @param inputFileName - name of the input file
	 * @return the name of the converted stub file. If you have multiple contracts in a
	 * single file then a prefix will be added to the generated file. If you provide the
	 * {@link Contract#getName} field then that field will override the generated file
	 * name.
	 *
	 * Example: name of file with 2 contracts is {@code foo.groovy}, it will be converted
	 * by the implementation to {@code foo.json}. The recursive file converter will create
	 * two files {@code 0_foo.json} and {@code 1_foo.json}
	 */
	String generateOutputFileNameForInput(String inputFileName);

	/**
	 * Describes the file extension of the generated mapping that this stub generator can
	 * handle.
	 * @return string describing the file extension
	 */
	default String fileExtension() {
		return ".json";
	}

}
