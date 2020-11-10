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
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.springframework.cloud.contract.stubrunner.ContractDownloader;
import org.springframework.cloud.contract.stubrunner.ScmStubDownloaderBuilder;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.StubRunnerPropertyUtils;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.converter.ToYamlConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

// TODO: Convert to incremental task: https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks
/**
 * Task that copies the contracts in order for the jar task to generate the jar. It takes
 * into consideration the inclusion patterns when working with repo with shared contracts.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 1.0.2
 */
@CacheableTask
class ContractsCopyTask extends DefaultTask {

	static final String TASK_NAME = "copyContracts";
	static final String CONTRACTS = "contracts";
	static final String BACKUP = "original";

	// inputs
	private final Property<Boolean> convertToYaml;

	private final Property<Boolean> failOnNoContracts;

	private final DirectoryProperty contractsDirectory;

	private final Dependency contractDependency;

	private final Repository contractRepository;

	private final Property<StubRunnerProperties.StubsMode> contractsMode;

	private final MapProperty<String, String> contractsProperties;

	private final Provider<Map<String, String>> allContractsProperties;

	private final Property<String> contractsPath;

	private final Property<Boolean> excludeBuildFolders;

	/**
	 * @see ContractVerifierExtension#deleteStubsAfterTest
	 *
	 * This property will delete the temporary dependency or Git repository from which
	 * stubs were copied to this task's output directory.
	 */
	private final Property<Boolean> deleteStubsAfterTest;

	// outputs
	private final DirectoryProperty copiedContractsFolder;

	private final DirectoryProperty backupContractsFolder;

	@Inject
	public ContractsCopyTask(ObjectFactory objects) {
		convertToYaml = objects.property(Boolean.class);
		failOnNoContracts = objects.property(Boolean.class);
		contractsDirectory = objects.directoryProperty();
		contractDependency = objects.newInstance(Dependency.class);
		contractRepository = objects.newInstance(Repository.class);
		contractsMode = objects.property(StubRunnerProperties.StubsMode.class);
		contractsProperties = objects.mapProperty(String.class, String.class);
		allContractsProperties = contractsProperties.flatMap(contractsProps -> {
			MapProperty<String, String> allProps = objects.mapProperty(String.class, String.class);
			allProps.putAll(contractsProps);
			String gitCommitId = this.discoverGitCommitId(contractsProps);
			if (StringUtils.hasText(gitCommitId)) {
				allProps.put("git.commit", gitCommitId);
			}
			return allProps;
		});
		contractsPath = objects.property(String.class);
		excludeBuildFolders = objects.property(Boolean.class);
		deleteStubsAfterTest = objects.property(Boolean.class);

		copiedContractsFolder = objects.directoryProperty();
		backupContractsFolder = objects.directoryProperty();

		this.getOutputs().upToDateWhen(task -> !(this.shouldDownloadContracts()
				&& this.getContractDependency().toStubConfiguration().isVersionChanging()));
	}

	@TaskAction
	void sync() {
		final File contractsDirectory;
		final String antPattern;
		if (shouldDownloadContracts()) {
			DownloadedData downloadedData = downloadContracts();
			contractsDirectory = downloadedData.downloadedContracts;
			antPattern = downloadedData.inclusionProperties.getIncludedRootFolderAntPattern() + "*.*";
			getLogger().info("Contracts got downloaded to [{}]", contractsDirectory);
		}
		else {
			contractsDirectory = this.contractsDirectory.getAsFile().getOrNull();
			antPattern = "**/";
		}
		getLogger().info("For project [{}] will use contracts provided in the folder [{}]", getProject().getName(),
				contractsDirectory);
		final String contractsRepository = this.contractRepository.getRepositoryUrl().getOrElse("");
		throwExceptionWhenFailOnNoContracts(contractsDirectory, contractsRepository);

		final String slashSeparatedGroupId = getProject().getGroup().toString().replace(".", File.separator);
		final String dotSeparatedAntPattern = antPattern.replace(slashSeparatedGroupId,
				getProject().getGroup().toString());
		File output = copiedContractsFolder.get().getAsFile();
		getLogger().info(
				"Downloading and unpacking files from [{}] to [{}]. The inclusion ant patterns are [{}] and [{}]",
				contractsDirectory, output, antPattern, dotSeparatedAntPattern);
		sync(contractsDirectory, antPattern, dotSeparatedAntPattern, excludeBuildFolders.get(), output);
		if (convertToYaml.get()) {
			convertContractsToYaml(contractsDirectory, antPattern, dotSeparatedAntPattern, output,
					excludeBuildFolders.get());
		}
	}

	private void convertContractsToYaml(File file, String antPattern, String slashSeparatedAntPattern,
			File outputContractsFolder, boolean excludeBuildFolders) {
		sync(file, antPattern, slashSeparatedAntPattern, excludeBuildFolders, backupContractsFolder.get().getAsFile());
		ToYamlConverter.replaceContractWithYaml(outputContractsFolder);
		getLogger().info("Replaced DSL files with their YAML representation at [{}]", outputContractsFolder);
	}

	private void sync(File file, String antPattern, String dotSeparatedAntPattern, boolean excludeBuildFolders,
			File outputContractsFolder) {
		getProject().sync(spec -> {
			spec.from(file);
			// by default group id is slash separated...
			spec.include(antPattern);
			// ...we also want to allow dot separation
			spec.include(dotSeparatedAntPattern);
			if (excludeBuildFolders) {
				spec.exclude("**/target/**", "**/build/**", "**/.mvn/**", "**/.gradle/**");
			}
			spec.into(outputContractsFolder);
		});
	}

	private DownloadedData downloadContracts() {
		String groupId = getProject().getGroup().toString();
		String artifactId = getProject().getName();
		getLogger().info("Project has group id [{}], artifact id [{}]", groupId, artifactId);
		getLogger().info("For project [{}] Download dependency is provided - will download contract jars", artifactId);
		getLogger().info("Contract dependency [{}]", contractDependency);
		StubConfiguration configuration = contractDependency.toStubConfiguration();
		getLogger().info("Got the following contract dependency to download [{}]", configuration);
		getLogger().info("The contract dependency is a changing one [{}]", configuration.isVersionChanging());

		final StubDownloader downloader = new StubDownloaderBuilderProvider().get(createStubRunnerOptions());
		final ContractDownloader contractDownloader = new ContractDownloader(downloader, configuration,
				contractsPath.getOrNull(), groupId, artifactId, getProject().getVersion().toString());
		final File downloadedContracts = contractDownloader.unpackAndDownloadContracts();
		final ContractDownloader.InclusionProperties inclusionProperties = contractDownloader
				.createNewInclusionProperties(downloadedContracts);

		// TODO: inclusionProperties.includedContracts is never used eventually. Review
		// this:
		return new DownloadedData(contractsSubDirIfPresent(downloadedContracts), inclusionProperties);
	}

	private void throwExceptionWhenFailOnNoContracts(@Nullable File file, String contractsRepository) {
		if (StringUtils.hasText(contractsRepository)) {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(
						"Contracts repository is set, will not throw an exception that the contracts are not found");
			}
			return;
		}
		if (failOnNoContracts.get() && (!file.exists() || file.listFiles().length == 0)) {
			String path = file.getAbsolutePath();
			throw new GradleException("Contracts could not be found: [" + path
					+ "]\nPlease make sure that the contracts were defined, or set the [failOnNoContracts] flag to [false]");
		}
	}

	private File contractsSubDirIfPresent(File contractsDirectory) {
		File contracts = new File(contractsDirectory, "contracts");
		if (contracts.exists()) {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Contracts folder found [{}]", contracts);
			}
			contractsDirectory = contracts;
		}
		return contractsDirectory;
	}

	private class DownloadedData {

		final File downloadedContracts;

		final ContractDownloader.InclusionProperties inclusionProperties;

		private DownloadedData(File downloadedContracts, ContractDownloader.InclusionProperties inclusionProperties) {
			this.downloadedContracts = downloadedContracts;
			this.inclusionProperties = inclusionProperties;
		}

	}

	@Input
	Property<Boolean> getConvertToYaml() {
		return convertToYaml;
	}

	@Input
	Property<Boolean> getFailOnNoContracts() {
		return failOnNoContracts;
	}

	@InputDirectory
	@Optional
	@PathSensitive(PathSensitivity.RELATIVE)
	DirectoryProperty getContractsDirectory() {
		return contractsDirectory;
	}

	@Nested
	Dependency getContractDependency() {
		return contractDependency;
	}

	static class Dependency {

		private static final String LATEST_VERSION = "+";

		private final Property<String> groupId;

		private final Property<String> artifactId;

		private final Property<String> version;

		private final Property<String> classifier;

		private final Property<String> stringNotation;

		@Inject
		public Dependency(ObjectFactory objects) {
			groupId = objects.property(String.class);
			artifactId = objects.property(String.class);
			version = objects.property(String.class);
			classifier = objects.property(String.class);
			stringNotation = objects.property(String.class);
		}

		@Input
		@Optional
		Property<String> getGroupId() {
			return groupId;
		}

		@Input
		@Optional
		Property<String> getArtifactId() {
			return artifactId;
		}

		@Input
		@Optional
		Property<String> getVersion() {
			return version;
		}

		@Input
		@Optional
		Property<String> getClassifier() {
			return classifier;
		}

		@Input
		@Optional
		Property<String> getStringNotation() {
			return stringNotation;
		}

		@Internal
		StubConfiguration toStubConfiguration() {
			String stringNotation = this.stringNotation.getOrNull();
			if (StringUtils.hasText(stringNotation)) {
				return new StubConfiguration(stringNotation);
			}

			String groupId = this.groupId.getOrNull();
			String artifactId = this.artifactId.getOrNull();
			String version = StringUtils.hasText(this.version.getOrNull()) ? this.version.getOrNull() : LATEST_VERSION;
			String classifier = this.classifier.getOrNull();
			return new StubConfiguration(groupId, artifactId, version, classifier);
		}

	}

	@Nested
	Repository getContractRepository() {
		return contractRepository;
	}

	static class Repository {

		private final Property<String> repositoryUrl;

		private final Property<String> username;

		private final Property<String> password;

		private final Property<String> proxyHost;

		private final Property<Integer> proxyPort;

		@Inject
		public Repository(ObjectFactory objects) {
			repositoryUrl = objects.property(String.class);
			username = objects.property(String.class);
			password = objects.property(String.class);
			proxyHost = objects.property(String.class);
			proxyPort = objects.property(Integer.class);
		}

		@Input
		@Optional
		Property<String> getRepositoryUrl() {
			return repositoryUrl;
		}

		@Input
		@Optional
		Property<String> getUsername() {
			return username;
		}

		@Input
		@Optional
		Property<String> getPassword() {
			return password;
		}

		@Input
		@Optional
		Property<String> getProxyHost() {
			return proxyHost;
		}

		@Input
		@Optional
		Property<Integer> getProxyPort() {
			return proxyPort;
		}

	}

	@Input
	@Optional
	Property<StubRunnerProperties.StubsMode> getContractsMode() {
		return contractsMode;
	}

	@Internal
	MapProperty<String, String> getContractsProperties() {
		return contractsProperties;
	}

	@Input
	Provider<Map<String, String>> getAllContractsProperties() {
		return allContractsProperties;
	}

	@Input
	@Optional
	Property<String> getContractsPath() {
		return contractsPath;
	}

	@Input
	Property<Boolean> getExcludeBuildFolders() {
		return excludeBuildFolders;
	}

	@Input
	Property<Boolean> getDeleteStubsAfterTest() {
		return deleteStubsAfterTest;
	}

	@OutputDirectory
	DirectoryProperty getCopiedContractsFolder() {
		return copiedContractsFolder;
	}

	@Optional
	@OutputDirectory
	DirectoryProperty getBackupContractsFolder() {
		return backupContractsFolder;
	}

	private boolean shouldDownloadContracts() {
		return StringUtils.hasText(contractDependency.getArtifactId().getOrNull())
				|| StringUtils.hasText(contractDependency.getStringNotation().getOrNull())
				|| StringUtils.hasText(contractRepository.getRepositoryUrl().getOrNull());
	}

	private StubRunnerOptions createStubRunnerOptions() {
		StubRunnerOptionsBuilder options = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(contractRepository.repositoryUrl.getOrNull()).withStubsMode(contractsMode.get())
				.withUsername(contractRepository.username.getOrNull())
				.withPassword(contractRepository.password.getOrNull())
				.withDeleteStubsAfterTest(deleteStubsAfterTest.get()).withProperties(allContractsProperties.getOrNull())
				.withFailOnNoStubs(failOnNoContracts.get());
		if (contractRepository.proxyPort.isPresent()) {
			options = options.withProxy(contractRepository.proxyHost.getOrNull(), contractRepository.proxyPort.get());
		}
		return options.build();
	}

	@Nullable
	private String discoverGitCommitId(Map<String, String> props) {
		String repositoryUrl = contractRepository.getRepositoryUrl().getOrNull();
		if (repositoryUrl != null && ScmStubDownloaderBuilder.isProtocolAccepted(repositoryUrl)) {
			String branch = StubRunnerPropertyUtils.getProperty(props, "git.branch");
			branch = StringUtils.hasText(branch) ? branch : "master";
			UsernamePasswordCredentialsProvider provider = null;
			if (StringUtils.hasText(contractRepository.getUsername().getOrNull())) {
				provider = new UsernamePasswordCredentialsProvider(contractRepository.getUsername().get(),
						contractRepository.getPassword().get());
			}
			try {
				String repoUrl;
				URI repoUri = URI.create(repositoryUrl);
				String part = repoUri.getSchemeSpecificPart();
				if (!StringUtils.hasLength(part)) {
					repoUrl = part;
				}
				else {
					repoUrl = part.startsWith("//") ? part.substring(2) : part;
				}
				Collection<Ref> refs = Git.lsRemoteRepository().setRemote(repoUrl).setCredentialsProvider(provider)
						.call();
				for (Ref ref : refs) {
					if (ref.getName().equals(branch) || ref.getName().equals("refs/heads/" + branch)
							|| ref.getName().equals("refs/tags/" + branch)) {
						return ref.getObjectId().name();
					}
				}
			}
			catch (GitAPIException e) {
				ContractsCopyTask.this.getLogger().warn("Unable to determine git repository commit id", e);
			}
		}
		return null;
	}

}