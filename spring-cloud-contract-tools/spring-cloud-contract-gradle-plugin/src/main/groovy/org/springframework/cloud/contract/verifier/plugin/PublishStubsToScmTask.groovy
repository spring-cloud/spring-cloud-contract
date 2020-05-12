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

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
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

	@CompileStatic
	static class Config {
		@Nested
		final Property<ContractVerifierExtension.ContractRepository> contractRepository
		@Nested
		final Property<ContractVerifierExtension.PublishStubsToScm> publishStubsToScm
		@Input
		final Property<StubRunnerProperties.StubsMode> contractsMode
		@Input
		final Property<Boolean> deleteStubsAfterTest
		@Input
		final Property<Boolean> failOnNoContracts
		@Input
		final MapProperty<String, String> contractsProperties
		@OutputDirectory
		final DirectoryProperty stubsOutputDir
		@Internal
		final ObjectFactory objects

		Config(ObjectFactory objects, ContractVerifierExtension extension) {
			this.objects = objects
			this.contractRepository = objects.property(ContractVerifierExtension.ContractRepository)
			this.contractRepository.set(extension.contractRepository)
			this.publishStubsToScm = objects.property(ContractVerifierExtension.PublishStubsToScm)
			this.publishStubsToScm.set(extension.publishStubsToScm)
			this.contractsMode = objects.property(StubRunnerProperties.StubsMode)
			this.contractsMode.set(extension.contractsMode)
			this.deleteStubsAfterTest = objects.property(Boolean)
			this.deleteStubsAfterTest.set(extension.failOnNoContracts)
			this.failOnNoContracts = objects.property(Boolean)
			this.failOnNoContracts.set(extension.deleteStubsAfterTest)
			this.contractsProperties = objects.mapProperty(String, String)
			this.contractsProperties.set(extension.contractsProperties)
			this.stubsOutputDir = objects.directoryProperty()
			this.stubsOutputDir.set(extension.stubsOutputDir)
		}
	}

	@TaskAction
	void publishStubsToScm() {
		ContractVerifierExtension.ContractRepository repository = merged()
		if (!shouldRun(repository)) {
			return
		}
		String projectName = project.group.toString() + ":" + project.name.toString() + ":" + this.project.version.toString()
		project.logger.info("Pushing Stubs to SCM for project [" + projectName + "]")
		StubRunnerOptions stubRunnerOptions = StubRunnerOptionsFactory.createStubRunnerOptions(
				repository, config.contractsMode.getOrElse(StubRunnerProperties.StubsMode.REMOTE), config.deleteStubsAfterTest.get(),
				config.contractsProperties.get(), config.failOnNoContracts.get())
		new ContractProjectUpdater(stubRunnerOptions).updateContractProject(projectName, config.stubsOutputDir.get().asFile.toPath())
	}

	static Config fromExtension(ContractVerifierExtension extension, ObjectFactory objects) {
		return new Config(objects, extension)
	}

	private boolean shouldRun(ContractVerifierExtension.ContractRepository repository) {
		String contractRepoUrl = repository.repositoryUrl.getOrNull() ?: ""
		if (!contractRepoUrl || !ScmStubDownloaderBuilder.isProtocolAccepted(contractRepoUrl)) {
			project.logger.warn("Skipping pushing stubs to scm since your contracts repository URL [${contractRepoUrl}] doesn't match any of the accepted protocols for SCM stub downloader")
			return false
		}
		return true
	}

	private ContractVerifierExtension.ContractRepository merged() {
		ContractVerifierExtension.ContractRepository original = config.contractRepository.get()
		ContractVerifierExtension.ContractRepository stubs = config.publishStubsToScm.get().getContractRepository()
		ContractVerifierExtension.ContractRepository copied = new ContractVerifierExtension.ContractRepository(config.objects)
		copied.setRepositoryUrl(stubs.getRepositoryUrl().getOrElse(original.getRepositoryUrl().getOrNull()))
		copied.setUsername(stubs.getUsername().getOrElse(original.getUsername().getOrNull()))
		copied.setPassword(stubs.getPassword().getOrElse(original.getPassword().getOrNull()))
		Integer port = stubs.getProxyPort().getOrElse(original.getProxyPort().getOrNull())
		if (port != null ) {
			copied.setProxyPort(port)
		}
		copied.setProxyHost(stubs.getProxyHost().getOrElse(original.getProxyHost().getOrNull()))
		return copied
	}
}
