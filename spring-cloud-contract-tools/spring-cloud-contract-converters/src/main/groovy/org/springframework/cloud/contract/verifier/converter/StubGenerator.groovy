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

package org.springframework.cloud.contract.verifier.converter

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata

/**
 * Converts contracts into their stub representation.
 *
 * @since 1.0.0
 */
@CompileStatic
interface StubGenerator {

	/**
	 * Returns {@code true} if the converter can handle the file to convert it into a stub.
	 */
	boolean canHandleFileName(String fileName)

	/**
	 * Returns the collection of converted contracts into stubs. One contract can
	 * result in multiple stubs.
	 */
	Map<Contract, String> convertContents(String rootName, ContractMetadata content)

	/**
	 * Returns the name of the converted stub file. If you have multiple contracts
	 * in a single file then a prefix will be added to the generated file. If you
	 * provide the {@link Contract#name} field then that field will override the
	 * generated file name.
	 *
	 * Example: name of file with 2 contracts is {@code foo.groovy}, it will be
	 * converted by the implementation to {@code foo.json}. The recursive file
	 * converter will create two files {@code 0_foo.json} and {@code 1_foo.json}
	 */
	String generateOutputFileNameForInput(String inputFileName)
}