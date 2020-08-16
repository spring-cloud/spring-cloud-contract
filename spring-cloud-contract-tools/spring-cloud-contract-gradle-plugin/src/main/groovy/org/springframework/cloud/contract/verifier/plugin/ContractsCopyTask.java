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

package org.springframework.cloud.contract.verifier.plugin;

import java.io.File;
import java.time.Instant;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.WorkResult;
import org.springframework.cloud.contract.stubrunner.ContractDownloader;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.converter.ToYamlConverter;
import org.springframework.util.StringUtils;

// TODO: Convert to incremental task: https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks
/**
 * Task that copies the contracts in order for the jar task to
 * generate the jar. It takes into consideration the inclusion
 * patterns when working with repo with shared contracts.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 1.0.2
 */
class ContractsCopyTask extends DefaultTask {

	static final String TASK_NAME = "copyContracts";
	static final String CONTRACTS = "contracts";
	static final String BACKUP = "original";

	@Nested
	Config config;

	static class Config {
		@Input
		Provider<Boolean> convertToYaml;
		@Input
		Provider<Boolean> excludeBuildFolders;
		@Input
		Provider<Boolean> failOnNoContracts;
		String contractsDirectoryPath;
		Provider<Directory> contractsDirectory;
		// All fields inside `@Nested` one are properly marked as an `@Input` to work with incremental build:
		@Nested
		@Optional
		ContractVerifierExtension.Dependency contractDependency;
		@Nested
		@Optional
		ContractVerifierExtension.ContractRepository contractRepository;
		@Input
		@Optional
		Property<StubRunnerProperties.StubsMode> contractsMode;
		@Input
		Property<Boolean> deleteStubsAfterTest;
		@Input
		MapProperty<String, String> contractsProperties;
		@Input
		@Optional
		Property<String> contractsPath;

		@Input
		@Optional
		Instant getForceDownloadOfTheLatestContracts() {
			// If we have `dynamic` version (`+` or `SNAPSHOT`) - we should mark this task as out of date for every run:
			if (shouldDownloadContracts() && getStubConfiguration().isVersionChanging()) {
				return Instant.now(); // This will trigger re-download of contracts
			}
			else {
				return null; // This will not trigger re-download of contracts
			}
		}

		@Optional
		@InputDirectory
		Provider<Directory> getContractsDirectory() {
			contractsDirectoryPath = contractsDirectory.get().getAsFile().getAbsolutePath();
			if (shouldDownloadContracts() || contractFolderMissing()) {
				return null;
			}
			else {
				return contractsDirectory;
			}
		}

		private boolean contractFolderMissing() {
			return contractsDirectory.isPresent() && !contractsDirectory.get().getAsFile().exists();
		}

		@Internal
		boolean shouldDownloadContracts() {
			return StringUtils.hasText(contractDependency.getArtifactId().getOrNull()) ||
					StringUtils.hasText(contractDependency.getStringNotation().getOrNull()) ||
					StringUtils.hasText(contractRepository.getRepositoryUrl().getOrNull());
		}

		@Internal
		StubConfiguration getStubConfiguration() {
			return GradleContractsDownloaderHelper.stubConfiguration(contractDependency);
		}

		@OutputDirectory
		DirectoryProperty copiedContractsFolder;
		@OutputDirectory
		DirectoryProperty stubsOutputDir;
		@Optional
		@OutputDirectory
		DirectoryProperty backupContractsFolder;
	}

	@TaskAction
	void sync() {
		final DownloadedData downloadedData = downloadContractsIfNeeded();
		final File contractsDirectory;
		String antPattern = "";
		if (downloadedData != null) {
			contractsDirectory = downloadedData.downloadedContracts;
			antPattern = downloadedData.inclusionProperties.getIncludedRootFolderAntPattern() + "*.*";
			getLogger().info("Contracts got downloaded to [{}]", contractsDirectory);
		}
		else if (config.contractsDirectory != null && config.contractsDirectory.isPresent()) {
			contractsDirectory = config.contractsDirectory.get().getAsFile();
			antPattern = "**/";
		}
		else {
			contractsDirectory = null;
		}
		getLogger().info("For project [{}] will use contracts provided in the folder [{}]", getProject().getName(), contractsDirectory);
		final String contractsRepository = config.contractRepository.getRepositoryUrl().getOrElse("");
		throwExceptionWhenFailOnNoContracts(contractsDirectory, contractsRepository);
		if (contractsDirectory == null) {
			getLogger().info("Contracts directory not set and contracts weren't downloaded. There's nothing to copy");
			return;
		}
		final String slashSeparatedGroupId = getProject().getGroup().toString().replace(".", File.separator);
		final String slashSeparatedAntPattern = antPattern.replace(slashSeparatedGroupId, getProject().getGroup().toString());
		File output = config.copiedContractsFolder.get().getAsFile();
		getLogger().info("Downloading and unpacking files from [{}] to [{}]. The inclusion ant patterns are [{}] and [{}]", contractsDirectory, output, antPattern, slashSeparatedAntPattern);
		sync(contractsDirectory, antPattern, slashSeparatedAntPattern, config.excludeBuildFolders.get(), output);
		if (config.convertToYaml.get()) {
			convertBackedUpDslsToYaml(contractsDirectory, antPattern, slashSeparatedAntPattern, output, config.excludeBuildFolders.get());
		}
	}

	static Config fromExtension(ContractVerifierExtension extension, String root, Project project) {
		Config config = new Config();
		config.convertToYaml = extension.getConvertToYaml();
		config.excludeBuildFolders = extension.getExcludeBuildFolders();
		config.failOnNoContracts = extension.getFailOnNoContracts();
		config.contractsDirectory = extension.getContractsDslDir();
		config.stubsOutputDir = extension.getStubsOutputDir();
		config.copiedContractsFolder = createTaskOutput(root, extension.getStubsOutputDir(), ContractsCopyTask.CONTRACTS, project);
		config.backupContractsFolder = createTaskOutput(root, extension.getStubsOutputDir(), ContractsCopyTask.BACKUP, project);
		config.contractDependency = extension.getContractDependency();
		config.contractRepository = extension.getContractRepository();
		config.contractsMode = extension.getContractsMode();
		config.deleteStubsAfterTest = extension.getDeleteStubsAfterTest();
		config.contractsProperties = extension.getContractsProperties();
		config.contractsPath = extension.getContractsPath();
		return config;
	}

	private void convertBackedUpDslsToYaml(File file, String antPattern, String slashSeparatedAntPattern, File outputContractsFolder, boolean excludeBuildFolders) {
		sync(file, antPattern, slashSeparatedAntPattern, excludeBuildFolders, config.backupContractsFolder.get().getAsFile());
		ToYamlConverter.replaceContractWithYaml(outputContractsFolder);
		getLogger().info("Replaced DSL files with their YAML representation at [{}]", outputContractsFolder);
	}

	protected WorkResult sync(File file, String antPattern, String slashSeparatedAntPattern, boolean excludeBuildFolders, File outputContractsFolder) {
		return getProject().sync(spec -> {
			spec.from(file);
			// by default group id is slash separated...
			spec.include(antPattern);
			// ...we also want to allow dot separation
			spec.include(slashSeparatedAntPattern);
			if (excludeBuildFolders) {
				spec.exclude("**/target/**", "**/build/**", "**/.mvn/**", "**/.gradle/**");
			}
			spec.into(outputContractsFolder);
		});
	}

	private DownloadedData downloadContractsIfNeeded() {
		if (config.shouldDownloadContracts()) {
			getLogger().info("Project has group id [{}], artifact id [{}]", getProject().getGroup(), getProject().getName());
			getLogger().info("For project [{}] Download dependency is provided - will download contract jars", getProject().getName());
			getLogger().info("Contract dependency [{}]", config.contractDependency);
			StubConfiguration configuration = config.getStubConfiguration();
			getLogger().info("Got the following contract dependency to download [{}]", configuration);
			getLogger().info("The contract dependency is a changing one [{}]", configuration.isVersionChanging());

			final StubDownloader downloader = new StubDownloaderBuilderProvider().get(
					StubRunnerOptionsFactory.createStubRunnerOptions(config.contractRepository,
							config.contractsMode.getOrNull(), config.deleteStubsAfterTest.get(),
							config.contractsProperties.get(), config.failOnNoContracts.get()));
			final ContractDownloader contractDownloader = new ContractDownloader(downloader, configuration,
					config.contractsPath.getOrNull(), getProject().getGroup().toString(), getProject().getName(), getProject().getVersion().toString());
			final File downloadedContracts = contractDownloader.unpackAndDownloadContracts();
			final ContractDownloader.InclusionProperties inclusionProperties =
					contractDownloader.createNewInclusionProperties(downloadedContracts);

			// TODO: inclusionProperties.includedContracts is never used eventually. Review this:
			return new DownloadedData(contractsSubDirIfPresent(downloadedContracts, getLogger()), inclusionProperties);
		}
		else {
			return null;
		}
	}

	private static DirectoryProperty createTaskOutput(String root, DirectoryProperty stubsOutputDir, String suffix, Project project) {
		return project.getObjects().directoryProperty().convention(stubsOutputDir.dir(root + File.separator + suffix));
	}

	private void throwExceptionWhenFailOnNoContracts(File file, String contractsRepository) {
		if (StringUtils.hasText(contractsRepository)) {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Contracts repository is set, will not throw an exception that the contracts are not found");
			}
			return;
		}
		if (config.failOnNoContracts.get() && (file == null || !file.exists() || file.listFiles().length == 0)) {
			String path = file != null ? file.getAbsolutePath() : config.contractsDirectoryPath;
			throw new GradleException("Contracts could not be found: [" + path + "]\nPlease make sure that the contracts were defined, or set the [failOnNoContracts] flag to [false]");
		}
	}

	private static File contractsSubDirIfPresent(File contractsDirectory, Logger logger) {
		File contracts = new File(contractsDirectory, "contracts");
		if (contracts.exists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Contracts folder found [{}]", contracts);
			}
			contractsDirectory = contracts;
		}
		return contractsDirectory;
	}

	private static class DownloadedData {
		final File downloadedContracts;
		final ContractDownloader.InclusionProperties inclusionProperties;

		private DownloadedData(File downloadedContracts, ContractDownloader.InclusionProperties inclusionProperties) {
			this.downloadedContracts = downloadedContracts;
			this.inclusionProperties = inclusionProperties;
		}
	}
}