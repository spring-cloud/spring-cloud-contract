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
import groovy.transform.Immutable
import groovy.transform.ImmutableOptions
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.WorkResult
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.StubDownloader
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.converter.ToYamlConverter
import org.springframework.util.StringUtils

import java.time.Instant

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
		Provider<Directory> contractsDirectory
		// All fields inside `@Nested` one are properly marked as an `@Input` to work with incremental build:
		@Nested
		@Optional
		ContractVerifierExtension.Dependency contractDependency
		@Nested
		@Optional
		ContractVerifierExtension.ContractRepository contractRepository
		@Input
		@Optional
		Property<StubRunnerProperties.StubsMode> contractsMode
		@Input
		Property<Boolean> deleteStubsAfterTest
		@Input
		MapProperty<String, String> contractsProperties
		@Input
		@Optional
		Property<String> contractsPath
		@Input
		@Optional
		Instant getForceDownloadOfTheLatestContracts() {
			// If we have `dynamic` version (`+` or `SNAPSHOT`) - we should mark this task as out of date for every run:
			if (shouldDownloadContracts() && getStubConfiguration().isVersionChanging()) {
				return Instant.now() // This will trigger re-download of contracts
			} else {
				return null // This will not trigger re-download of contracts
			}
		}
		@Optional
		@InputDirectory
		Provider<Directory> getContractsDirectory() {
			if (shouldDownloadContracts()) {
				return null
			} else {
				return contractsDirectory
			}
		}

		@Internal
		boolean shouldDownloadContracts() {
			return StringUtils.hasText(contractDependency.getArtifactId().getOrNull()) ||
					StringUtils.hasText(contractDependency.getStringNotation().getOrNull()) ||
					StringUtils.hasText(contractRepository.repositoryUrl.getOrNull())
		}
		@Internal
		StubConfiguration getStubConfiguration() {
			return GradleContractsDownloaderHelper.stubConfiguration(contractDependency)
		}

		@OutputDirectory
		DirectoryProperty copiedContractsFolder
		@Optional
		@OutputDirectory
		DirectoryProperty backupContractsFolder
	}

	@TaskAction
	void sync() {
		final DownloadedData downloadedData = downloadContractsIfNeeded()
		final File contractsDirectory
		final String antPattern
		if (downloadedData) {
			contractsDirectory = downloadedData.downloadedContracts
			antPattern = "${downloadedData.inclusionProperties.includedRootFolderAntPattern}*.*"
		} else {
			contractsDirectory = config.contractsDirectory.get().asFile
			antPattern = "**/"
		}
		logger.info("For project [{}] will use contracts provided in the folder [{}]", project.name, contractsDirectory)
		final String contractsRepository = config.contractRepository.repositoryUrl.isPresent() ? config.contractRepository.repositoryUrl : ""
		throwExceptionWhenFailOnNoContracts(contractsDirectory, contractsRepository)

		final String slashSeparatedGroupId = project.group.toString().replace(".", File.separator)
		final String slashSeparatedAntPattern = antPattern.replace(slashSeparatedGroupId, project.group.toString())
		final File output = config.copiedContractsFolder.get().asFile
		logger.info("Downloading and unpacking files from [${contractsDirectory}] to [$output]. The inclusion ant patterns are [${antPattern}] and [${slashSeparatedAntPattern}]")
		sync(contractsDirectory, antPattern, slashSeparatedAntPattern, config.excludeBuildFolders.get(), output)
		if (config.convertToYaml.get()) {
			convertBackedUpDslsToYaml(contractsDirectory, antPattern, slashSeparatedAntPattern, output, config.excludeBuildFolders.get())
		}
	}

	static Config fromExtension(ContractVerifierExtension extension, String root, Project project) {
		return new Config(
				convertToYaml: extension.convertToYaml,
				excludeBuildFolders: extension.excludeBuildFolders,
				failOnNoContracts: extension.failOnNoContracts,
				contractsDirectory: extension.contractsDslDir,
				copiedContractsFolder: createTaskOutput(root, extension.stubsOutputDir, ContractsCopyTask.CONTRACTS, project),
				backupContractsFolder: createTaskOutput(root, extension.stubsOutputDir, ContractsCopyTask.BACKUP, project),
				contractDependency: extension.contractDependency,
				contractRepository: extension.contractRepository,
				contractsMode: extension.contractsMode,
				deleteStubsAfterTest: extension.deleteStubsAfterTest,
				contractsProperties: extension.contractsProperties,
				contractsPath: extension.contractsPath
		)
	}

	private void convertBackedUpDslsToYaml(File file, String antPattern, String slashSeparatedAntPattern, File outputContractsFolder, boolean excludeBuildFolders) {
		sync(file, antPattern, slashSeparatedAntPattern, excludeBuildFolders, config.backupContractsFolder.get().asFile)
		ToYamlConverter.replaceContractWithYaml(outputContractsFolder)
		logger.info("Replaced DSL files with their YAML representation at [" + outputContractsFolder + "]")
	}

	protected WorkResult sync(File file, String antPattern, String slashSeparatedAntPattern, boolean excludeBuildFolders, File outputContractsFolder) {
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

	private DownloadedData downloadContractsIfNeeded() {
		if (config.shouldDownloadContracts()) {
			logger.info("Project has group id [{}], artifact id [{}]", project.group, project.name)
			logger.info("For project [${project.name}] Download dependency is provided - will download contract jars")
			logger.info("Contract dependency [{}]", config.contractDependency)
			StubConfiguration configuration = config.getStubConfiguration()
			logger.info("Got the following contract dependency to download [{}]", configuration)
			logger.info("The contract dependency is a changing one [{}]", configuration.isVersionChanging())

			final StubDownloader downloader = new StubDownloaderBuilderProvider().get(
					StubRunnerOptionsFactory.createStubRunnerOptions(config.contractRepository,
							config.contractsMode.getOrNull(), config.deleteStubsAfterTest.get(),
							config.contractsProperties.get(), config.failOnNoContracts.get()))
			final ContractDownloader contractDownloader = new ContractDownloader(downloader, configuration,
					config.contractsPath.getOrNull(), project.group as String, project.name, project.version as String)
			final File downloadedContracts = contractDownloader.unpackAndDownloadContracts();
			final ContractDownloader.InclusionProperties inclusionProperties =
					contractDownloader.createNewInclusionProperties(downloadedContracts)

			// TODO: inclusionProperties.includedContracts is never used eventually. Review this:
			return new DownloadedData(
					downloadedContracts: contractsSubDirIfPresent(downloadedContracts, logger),
					inclusionProperties: inclusionProperties
			)
		} else {
			return null
		}
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

	private static File contractsSubDirIfPresent(File contractsDirectory, Logger logger) {
		File contracts = new File(contractsDirectory, "contracts")
		if (contracts.exists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Contracts folder found [" + contracts + "]")
			}
			contractsDirectory = contracts
		}
		return contractsDirectory
	}

	@ImmutableOptions(knownImmutableClasses = [File, ContractDownloader.InclusionProperties])
	@Immutable
	private static class DownloadedData {
		final File downloadedContracts
		final ContractDownloader.InclusionProperties inclusionProperties
	}
}