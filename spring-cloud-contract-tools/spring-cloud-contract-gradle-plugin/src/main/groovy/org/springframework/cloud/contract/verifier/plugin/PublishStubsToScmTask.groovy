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

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.stubrunner.ContractProjectUpdater
import org.springframework.cloud.contract.stubrunner.ScmStubDownloaderBuilder
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties

/**
 * For SCM based repositories will copy the generated stubs
 * to the cloned repo with contracts and stubs. Will also
 * commit the changes and push them to origin.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @since 2.0.0
 */
@CompileStatic
class PublishStubsToScmTask extends DefaultTask {

	static final String TASK_NAME = 'publishStubsToScm'
	@Nested
	Config config

	static class Config {
		// All fields inside `@Nested` one are properly marked as an `@Input` to work with incremental build:
		@Nested
		@Optional
		ContractVerifierExtension.ContractRepository contractRepository
		@Input
		@Optional
		Provider<StubRunnerProperties.StubsMode> contractsMode
		@Input
		Provider<Boolean> deleteStubsAfterTest
		@Input
		Provider<Boolean> failOnNoContracts
		@Input
		MapProperty<String, String> contractsProperties

		@OutputDirectory
		DirectoryProperty stubsOutputDir
	}

	@TaskAction
	void publishStubsToScm() {
		if (!shouldRun()) {
			return
		}
		String projectName = project.group.toString() + ":" + project.name.toString() + ":" + this.project.version.toString()
		project.logger.info("Pushing Stubs to SCM for project [" + projectName + "]")
		StubRunnerOptions stubRunnerOptions = StubRunnerOptionsFactory.createStubRunnerOptions(
				config.contractRepository, config.contractsMode.getOrNull(), config.deleteStubsAfterTest.get(),
				config.contractsProperties.get(), config.failOnNoContracts.get())
		new ContractProjectUpdater(stubRunnerOptions).updateContractProject(projectName, config.stubsOutputDir.get().asFile.toPath())
	}

	static Config fromExtension(ContractVerifierExtension extension) {
		return new Config(
				contractRepository: extension.contractRepository,
				contractsMode: extension.contractsMode,
				failOnNoContracts: extension.failOnNoContracts,
				deleteStubsAfterTest: extension.deleteStubsAfterTest,
				contractsProperties: extension.contractsProperties,

				stubsOutputDir: extension.stubsOutputDir
		)
	}

	private boolean shouldRun() {
		String contractRepoUrl = config.contractRepository.repositoryUrl.getOrNull() ?: ""
		if (!contractRepoUrl || !ScmStubDownloaderBuilder.isProtocolAccepted(contractRepoUrl)) {
			project.logger.warn("Skipping pushing stubs to scm since your contracts repository URL [${contractRepoUrl}] doesn't match any of the accepted protocols for SCM stub downloader")
			return false
		}
		return true
	}
}
