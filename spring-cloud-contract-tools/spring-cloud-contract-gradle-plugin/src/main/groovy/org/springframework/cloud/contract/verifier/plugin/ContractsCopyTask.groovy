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
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.WorkResult

import org.springframework.cloud.contract.verifier.converter.ToYamlConverter
import org.springframework.util.StringUtils
// TODO: Convert to incremental task: https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks
/**
 * Task that copies the contracts in order for the jar task to
 * generate the jar. It takes into consideration the inclusion
 * patterns when working with repo with shared contracts.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @since 1.0.2
 */
@PackageScope
@CompileStatic
class ContractsCopyTask extends DefaultTask {

	static final String TASK_NAME = 'copyContracts'
	static final String CONTRACTS = "contracts"
	static final String BACKUP = "original"
	@Nested
	Config config

	static class Config {
		@Input
		Provider<Boolean> convertToYaml
		@Input
		Provider<Boolean> excludeBuildFolders
		@Input
		Provider<Boolean> failOnNoContracts
		@Input
		Provider<String> includedRootFolderAntPattern
		@InputDirectory
		Provider<Directory> contractsDirectory
		@Input
		Provider<String> contractsRepository

		@OutputDirectory
		DirectoryProperty copiedContractsFolder
		@Optional
		@OutputDirectory
		DirectoryProperty backupContractsFolder
	}

	@TaskAction
	void sync() {
		File contractsDirectory = config.contractsDirectory.get().asFile
		String contractsRepository = config.contractsRepository.get()
		throwExceptionWhenFailOnNoContracts(contractsDirectory, contractsRepository)
		String antPattern = "${config.includedRootFolderAntPattern.get()}*.*"
		String slashSeparatedGroupId = project.group.toString().replace(".", File.separator)
		String slashSeparatedAntPattern = antPattern.replace(slashSeparatedGroupId, project.group.toString())
		File output = config.copiedContractsFolder.get().asFile
		logger.info("Downloading and unpacking files from [${contractsDirectory}] to [$output]. The inclusion ant patterns are [${antPattern}] and [${slashSeparatedAntPattern}]")
		sync(contractsDirectory, antPattern, slashSeparatedAntPattern, config.excludeBuildFolders.get(), output)
		if (config.convertToYaml.get()) {
			convertBackedUpDslsToYaml(contractsDirectory, antPattern, slashSeparatedAntPattern, output, config.excludeBuildFolders.get())
		}
	}

	static Config fromExtension(ContractVerifierExtension extension, TaskProvider<InitContractsTask> initContractsTask, String root, Project project) {
		return new Config(
				convertToYaml: extension.convertToYaml,
				excludeBuildFolders: extension.excludeBuildFolders,
				failOnNoContracts: extension.failOnNoContracts,
				includedRootFolderAntPattern: initContractsTask.flatMap { it.config.includedRootFolderAntPattern },
				contractsDirectory: initContractsTask.flatMap { it.config.initialisedContractsDirectory },
				contractsRepository: extension.contractRepository.repositoryUrl.isPresent() ? extension.contractRepository.repositoryUrl : project.provider({ String s -> "" }),
				copiedContractsFolder: createTaskOutput(root, extension.stubsOutputDir, ContractsCopyTask.CONTRACTS, project),
				backupContractsFolder: createTaskOutput(root, extension.stubsOutputDir, ContractsCopyTask.BACKUP, project)
		)
	}

	private void convertBackedUpDslsToYaml(File file, String antPattern, String slashSeparatedAntPattern, File outputContractsFolder, boolean excludeBuildFolders) {
		sync(file, antPattern, slashSeparatedAntPattern, excludeBuildFolders, config.backupContractsFolder.get().asFile)
		ToYamlConverter.replaceContractWithYaml(outputContractsFolder)
		logger.info("Replaced DSL files with their YAML representation at [" + outputContractsFolder + "]")
	}

	protected WorkResult sync(File file, String antPattern, String slashSeparatedAntPattern, boolean excludeBuildFolders, File outputContractsFolder) {
		// TODO: Is there any better way to make it statically compiled, avoiding explicit creation of new Action?
		// sync will remove files from target if they are removed from source. So using it here instead of copy:
		return project.sync(new Action<CopySpec>() {
			@Override
			void execute(final CopySpec spec) {
				spec.with {
					from(file)
					// by default group id is slash separated...
					include(antPattern)
					// ...we also want to allow dot separation
					include(slashSeparatedAntPattern)
					if (excludeBuildFolders) {
						exclude "**/target/**", "**/build/**", "**/.mvn/**", "**/.gradle/**"
					}
					into(outputContractsFolder)
				}
			}
		})
	}

	private static DirectoryProperty createTaskOutput(String root, DirectoryProperty stubsOutputDir, String suffix, Project project) {
		Provider<Directory> provider = stubsOutputDir.flatMap {
			Directory dir = it
			File output = project.file("${dir.asFile}/${root}/${suffix}")

			DirectoryProperty property = project.objects.directoryProperty()
			property.set(output)
			return property
		}
		DirectoryProperty property = project.objects.directoryProperty();
		property.set(provider)
		return property
	}

	private void throwExceptionWhenFailOnNoContracts(File file, String contractsRepository) {
		if (StringUtils.hasText(contractsRepository)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Contracts repository is set, will not throw an exception that the contracts are not found")
			}
			return
		}
		if (config.failOnNoContracts.get() && (!file.exists() || file.listFiles().length == 0)) {
			throw new GradleException("Contracts could not be found: ["
					+ file.getAbsolutePath()
					+ "] .\nPlease make sure that the contracts were defined, or set the [failOnNoContracts] flag to [false]")
		}
	}
}