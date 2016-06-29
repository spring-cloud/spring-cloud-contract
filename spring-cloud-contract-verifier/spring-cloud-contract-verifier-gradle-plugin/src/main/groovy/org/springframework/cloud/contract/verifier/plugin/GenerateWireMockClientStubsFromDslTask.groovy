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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter
import org.springframework.cloud.contract.verifier.wiremock.RecursiveFilesConverter
//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
/**
 * Generates WireMock stubs from the contracts
 *
 * @since 1.0.0
 */
class GenerateWireMockClientStubsFromDslTask extends ConventionTask {

	private static final String DEFAULT_MAPPINGS_FOLDER = 'mappings'

	@InputDirectory
	File contractsDslDir
	@OutputDirectory
	File stubsOutputDir

	ContractVerifierConfigProperties configProperties

	@TaskAction
	void generate() {
		logger.info("Spring Cloud Contract Verifier Plugin: Invoking DSL to WireMock client stubs conversion")
		logger.debug("From '${getContractsDslDir()}' to '${getStubsOutputDir()}'")
		ContractVerifierConfigProperties props = getConfigProperties()
		File outMappingsDir = props.stubsOutputDir != null ? new File(props.stubsOutputDir, DEFAULT_MAPPINGS_FOLDER)
				: new File(project.buildDir, "stubs/$DEFAULT_MAPPINGS_FOLDER")
		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(),
				getConfigProperties(), outMappingsDir)
		converter.processFiles()
	}
}
