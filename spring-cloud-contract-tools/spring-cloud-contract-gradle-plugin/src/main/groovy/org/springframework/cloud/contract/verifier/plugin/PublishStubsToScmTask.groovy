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
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
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
	Config config
	private Closure customizationClosure = Closure.IDENTITY

	static class Config {
		private ObjectFactory objects
		ContractVerifierExtension.ContractRepository contractRepository
		Property<StubRunnerProperties.StubsMode> contractsMode
		Property<Boolean> deleteStubsAfterTest
		Property<Boolean> failOnNoContracts
		MapProperty<String, String> contractsProperties
		DirectoryProperty stubsOutputDir

		void setContractsMode(Property<StubRunnerProperties.StubsMode> contractsMode) {
			this.contractsMode = contractsMode
		}

		void setContractsMode(String contractsMode) {
			if (contractsMode) {
				this.contractsMode = objects.property(StubRunnerProperties.StubsMode).convention(StubRunnerProperties.StubsMode.valueOf(contractsMode.toUpperCase()))
			}
		}

		void setDeleteStubsAfterTest(Property<Boolean> deleteStubsAfterTest) {
			this.deleteStubsAfterTest = deleteStubsAfterTest
		}

		void setDeleteStubsAfterTest(Boolean deleteStubsAfterTest) {
			if (deleteStubsAfterTest != null) {
				this.deleteStubsAfterTest = objects.property(Boolean).convention(deleteStubsAfterTest)
			}
		}

		void setFailOnNoContracts(Property<Boolean> failOnNoContracts) {
			this.failOnNoContracts = failOnNoContracts
		}

		void setFailOnNoContracts(Boolean failOnNoContracts) {
			if (failOnNoContracts != null) {
				this.failOnNoContracts = objects.property(Boolean).convention(failOnNoContracts)
			}
		}

		void setContractsProperties(MapProperty<String, String> contractsProperties) {
			this.contractsProperties = contractsProperties
		}

		void setContractsProperties(Map<String, String> contractsProperties) {
			if (contractsProperties) {
				this.contractsProperties = objects.mapProperty(String, String).convention(contractsProperties)
			}
		}

		void setStubsOutputDir(DirectoryProperty stubsOutputDir) {
			this.stubsOutputDir = stubsOutputDir
		}

		void setStubsOutputDir(String stubsOutputDir) {
			if (stubsOutputDir) {
				this.stubsOutputDir = objects.directoryProperty()
				this.stubsOutputDir.set(new File(stubsOutputDir))
			}
		}

		void setStubsOutputDir(GString stubsOutputDir) {
			if (stubsOutputDir) {
				this.stubsOutputDir = objects.directoryProperty()
				this.stubsOutputDir.set(new File(stubsOutputDir.toString()))
			}
		}

		void contractRepository(@DelegatesTo(ContractVerifierExtension.ContractRepository) Closure closure) {
			closure.delegate = contractRepository
			closure.call()
		}

		@Deprecated
		void contractDependency(Closure closure) {
			// Here only to preserve backward compatibility. Was never used here.
		}

		@PackageScope
		void setObjects(ObjectFactory objects) {
			this.objects = objects
		}
	}

	@TaskAction
	void publishStubsToScm() {
		applyConfigCustomizations()
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

	static Config fromExtension(ContractVerifierExtension extension, ObjectFactory objects) {
		return new Config(
				contractRepository: extension.contractRepository,
				contractsMode: extension.contractsMode,
				failOnNoContracts: extension.failOnNoContracts,
				deleteStubsAfterTest: extension.deleteStubsAfterTest,
				contractsProperties: extension.contractsProperties,
				stubsOutputDir: extension.stubsOutputDir,
				objects: objects
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

	/**
	 * Some builds might want to customize this task only, overriding settings from plugin.
	 */
	void customize(@DelegatesTo(Config) Closure closure) {
		customizationClosure = closure
	}

	private void applyConfigCustomizations() {
		// Needs to be copied, otherwise properties won't be updated:
		config.contractRepository = copy(config.contractRepository, project.objects)
		customizationClosure.delegate = config
		customizationClosure.call()
	}

	private static ContractVerifierExtension.ContractRepository copy(ContractVerifierExtension.ContractRepository original, ObjectFactory objects) {
		ContractVerifierExtension.ContractRepository copied = new ContractVerifierExtension.ContractRepository(objects)
		if (original.repositoryUrl.getOrNull()) {
			copied.repositoryUrl.set(original.repositoryUrl.get())
		}
		if (original.username.getOrNull()) {
			copied.username.set(original.username.get())
		}
		if (original.password.getOrNull()) {
			copied.password.set(original.password.get())
		}
		if (original.proxyPort.getOrNull()) {
			copied.proxyPort.set(original.proxyPort.get())
		}
		if (original.proxyHost.getOrNull()) {
			copied.proxyHost.set(original.proxyHost.get())
		}
		return copied
	}
}
