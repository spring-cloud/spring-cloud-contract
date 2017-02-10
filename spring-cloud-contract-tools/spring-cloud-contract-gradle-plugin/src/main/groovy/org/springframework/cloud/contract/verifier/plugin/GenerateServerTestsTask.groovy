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

import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.spec.ContractVerifierException
import org.springframework.cloud.contract.verifier.TestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import static org.springframework.cloud.contract.verifier.plugin.SpringCloudContractVerifierGradlePlugin.COPY_CONTRACTS_TASK_NAME
/**
 * Task used to generate server side tests
 *
 * @since 1.0.0
 */
class GenerateServerTestsTask extends ConventionTask {

	File generatedTestSourcesDir

	//TODO: How to deal with @Input*, @Output* and that domain object?
	ContractVerifierExtension configProperties
	GradleContractsDownloader downloader

	@TaskAction
	void generate() {
		logger.info("Generated test sources dir [${getGeneratedTestSourcesDir()}]")
		Task copyContractsTask = project.getTasksByName(COPY_CONTRACTS_TASK_NAME, false).first()
		ContractVerifierConfigProperties props = props(copyContractsTask)
		File contractsDslDir = contractsDslDir(copyContractsTask, props)
		if (getConfigProperties().getContractDependency()) {
			project.logger.debug("Updating the stubs locations for the case where we have a JAR with contracts")
			props.contractsDslDir = contractsDslDir
			props.includedContracts = ".*"
		}
		project.logger.info("Spring Cloud Contract Verifier Plugin: Invoking test sources generation")
		project.logger.info("Contracts are unpacked to [${contractsDslDir}]")
		project.logger.info("Included contracts are [${props.includedContracts}]")

		project.sourceSets.test.groovy {
			project.logger.info("Registering ${getConfigProperties().generatedTestSourcesDir} as test source directory")
			srcDir getConfigProperties().generatedTestSourcesDir
		}

		try {
			props = props ?: ExtensionToProperties.fromExtension(getConfigProperties())
			props.contractsDslDir = contractsDslDir
			TestGenerator generator = new TestGenerator(props)
			int generatedClasses = generator.generate()
			project.logger.info("Generated {} test classes", generatedClasses)
		} catch (ContractVerifierException e) {
			throw new GradleException("Spring Cloud Contract Verifier Plugin exception: ${e.message}", e)
		}
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
