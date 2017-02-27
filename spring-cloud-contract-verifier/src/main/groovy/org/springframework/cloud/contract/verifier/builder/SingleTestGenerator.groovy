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

package org.springframework.cloud.contract.verifier.builder

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractMetadata

/**
 * Builds a single test.
 *
 * @since 1.1.0
 */
@CompileStatic
interface SingleTestGenerator {

	/**
	 * Creates contents of a single test class in which all test scenarios from
	 * the contract metadata should be placed.
	 *
	 * @param properties - properties passed to the plugin
	 * @param listOfFiles - list of parsed contracts with additional metadata
	 * @param className - the name of the generated test class
	 * @param classPackage - the name of the package in which the test class should be stored
	 * @param includedDirectoryRelativePath - relative path to the included directory
	 * @return contents of a single test class
	 */
	String buildClass(ContractVerifierConfigProperties properties, Collection<ContractMetadata> listOfFiles,
					  String className, String classPackage, String includedDirectoryRelativePath)
}
