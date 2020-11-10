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
import java.util.Map;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

/**
 * Converts contracts into their stub representation.
 *
 * @since 1.1.0
 */
public interface StubGenerator {

	/**
	 * @param fileName - file name
	 * @return {@code true} if the converter can handle the file to convert it into a
	 * stub.
	 * @deprecated use {@link StubGenerator#canReadStubMapping(File)}
	 */
	@Deprecated
	default boolean canReadStubMapping(String fileName) {
		return fileName.endsWith(fileExtension());
	}

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
