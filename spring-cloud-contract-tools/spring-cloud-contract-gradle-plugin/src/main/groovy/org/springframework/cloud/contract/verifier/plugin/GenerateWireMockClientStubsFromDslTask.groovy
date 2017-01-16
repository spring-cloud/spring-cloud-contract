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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Task
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverter

import static org.springframework.cloud.contract.verifier.plugin.SpringCloudContractVerifierGradlePlugin.COPY_CONTRACTS_TASK_NAME
//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
/**
 * Generates stubs from the contracts. The name is WireMock related but the implementation
 * can differ
 *
 * @since 1.0.0
 */
class GenerateWireMockClientStubsFromDslTask extends ConventionTask {

	private static final String DEFAULT_MAPPINGS_FOLDER = 'mappings'

	File stubsOutputDir

	ContractVerifierExtension configProperties
	GradleContractsDownloader downloader

	@TaskAction
	void generate() {
		logger.info("Stubs output dir [${getStubsOutputDir()}")
		Task copyContractsTask = project.getTasksByName(COPY_CONTRACTS_TASK_NAME, false).first()
		ContractVerifierConfigProperties props = props(copyContractsTask)
		File contractsDslDir = contractsDslDir(copyContractsTask, props)
		logger.info("Spring Cloud Contract Verifier Plugin: Invoking DSL to client stubs conversion")
		props.contractsDslDir = contractsDslDir
		props.includedContracts = ".*"
		File outMappingsDir = getStubsOutputDir() != null ? new File(getStubsOutputDir(), DEFAULT_MAPPINGS_FOLDER)
				: new File(project.buildDir, "stubs/$DEFAULT_MAPPINGS_FOLDER")
		logger.info("Contracts dir is [${contractsDslDir}] output stubs dir is [${outMappingsDir}]")
		RecursiveFilesConverter converter = new RecursiveFilesConverter(
				props, outMappingsDir)
		converter.processFiles()
	}

	private ContractVerifierConfigProperties props(Task task) {
		try {
			return task.ext.contractVerifierConfigProperties
		} catch (Exception e) {
			project.logger.error("Couldn't retrieve the configuration property set by the copy contracts task", e)
			ContractVerifierConfigProperties props = ExtensionToProperties.fromExtension(getConfigProperties())
			getDownloader().downloadAndUnpackContractsIfRequired(getConfigProperties(), props)
			return props
		}
	}

	private File contractsDslDir(Task task, ContractVerifierConfigProperties props) {
		try {
			return task.ext.contractsDslDir
		} catch (Exception e) {
			project.logger.error("Couldn't retrieve the contract dsl property set by the copy contracts task", e)
			return props.contractsDslDir
		}
	}
}
