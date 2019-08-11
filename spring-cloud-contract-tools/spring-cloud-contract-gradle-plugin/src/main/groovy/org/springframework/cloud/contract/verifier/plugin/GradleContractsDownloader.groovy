package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ImmutableOptions
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.StubDownloader
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.util.StringUtils

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
class GradleContractsDownloader {

	private static final String LATEST_VERSION = '+'

	private final Project project
	private final Logger logger
	protected static final Map<StubConfiguration, File> downloadedContract = new ConcurrentHashMap<>()

	GradleContractsDownloader(Project project, Logger logger) {
		this.project = project
		this.logger = logger
	}

	DownloadedData downloadAndUnpackContractsIfRequired(ContractVerifierExtension.Dependency contractDependency,
														ContractVerifierExtension.ContractRepository contractRepository, String contractsPath,
														StubRunnerProperties.StubsMode contractsMode, boolean deleteStubsAfterTest,
														Map<String, String> contractsProperties, boolean failOnNoContracts) {
		if (!shouldDownloadContracts(contractDependency, contractRepository)) {
			return null
		}
		logger.info("Project has group id [{}], artifact id [{}]", project.group, project.name)
		// download contracts, unzip them and pass as output directory

		logger.info("For project [${project.name}] Download dependency is provided - will download contract jars")
		logger.info("Contract dependency [{}]", contractDependency)
		StubConfiguration configuration = stubConfiguration(contractDependency)
		logger.info("Got the following contract dependency to download [{}]", configuration)
		logger.info("The contract dependency is a changing one [{}] and cache download switch is set to [{}]",
				configuration.isVersionChanging(), contractRepository.cacheDownloadedContracts.get())
		if (!configuration.isVersionChanging() && contractRepository.cacheDownloadedContracts.get()) {
			logger.info("Resolved a non changing version - will try to return the folder from a cache")
			File cachedFolder = downloadedContract.get(configuration)
			if (cachedFolder) {
				logger.info("For project [{}] returning the cached location of the contracts", project.name)
				final ContractDownloader.InclusionProperties inclusionProperties =
						contractDownloader(configuration, contractRepository, contractsPath, contractsMode,
								deleteStubsAfterTest, contractsProperties, failOnNoContracts).createNewInclusionProperties(cachedFolder)

				return new DownloadedData(
						downloadedContracts: contractsSubDirIfPresent(cachedFolder, logger),
						inclusionProperties: inclusionProperties
				)
			}
		}
		final ContractDownloader contractDownloader =
				contractDownloader(configuration, contractRepository, contractsPath, contractsMode, deleteStubsAfterTest,
						contractsProperties, failOnNoContracts);
		final File downloadedContracts = contractDownloader.unpackAndDownloadContracts();
		final ContractDownloader.InclusionProperties inclusionProperties =
				contractDownloader.createNewInclusionProperties(downloadedContracts)

		downloadedContract.put(configuration, downloadedContracts)

		return new DownloadedData(
				downloadedContracts: contractsSubDirIfPresent(downloadedContracts, logger),
				inclusionProperties: inclusionProperties
		)
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

	@PackageScope
	StubConfiguration stubConfiguration(ContractVerifierExtension.Dependency contractDependency) {
		String groupId = contractDependency.groupId.getOrNull()
		String artifactId = contractDependency.artifactId.getOrNull()
		String version = StringUtils.hasText(contractDependency.version.getOrNull()) ?
				contractDependency.version.getOrNull() : LATEST_VERSION
		String classifier = contractDependency.classifier.getOrNull()
		String stringNotation = contractDependency.stringNotation.getOrNull()
		if (StringUtils.hasText(stringNotation)) {
			StubConfiguration stubConfiguration = new StubConfiguration(stringNotation)
			return new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId,
					stubConfiguration.version, stubConfiguration.classifier)
		}
		return new StubConfiguration(groupId, artifactId, version, classifier)
	}

	protected ContractDownloader contractDownloader(StubConfiguration configuration,
													ContractVerifierExtension.ContractRepository contractRepository,
													String contractsPath, StubRunnerProperties.StubsMode contractsMode,
													boolean deleteStubsAfterTest, Map<String, String> contractsProperties,
													boolean failOnNoContracts) {
		return new ContractDownloader(stubDownloader(contractRepository, contractsMode, deleteStubsAfterTest, contractsProperties, failOnNoContracts),
				configuration, contractsPath, project.group as String, project.name, project.version as String)
	}

	protected StubDownloader stubDownloader(ContractVerifierExtension.ContractRepository contractRepository,
											StubRunnerProperties.StubsMode contractsMode, boolean deleteStubsAfterTest,
											Map<String, String> contractsProperties, boolean failOnNoContracts) {
		StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider()
		return provider.get(StubRunnerOptionsFactory.createStubRunnerOptions(contractRepository, contractsMode,
				deleteStubsAfterTest, contractsProperties, failOnNoContracts))
	}

	private static boolean shouldDownloadContracts(ContractVerifierExtension.Dependency contractDependency,
												   ContractVerifierExtension.ContractRepository contractRepository) {
		return StringUtils.hasText(contractDependency.getArtifactId().getOrNull()) ||
				StringUtils.hasText(contractDependency.getStringNotation().getOrNull()) ||
				StringUtils.hasText(contractRepository.repositoryUrl.getOrNull())
	}

	@ImmutableOptions(knownImmutableClasses = [File, ContractDownloader.InclusionProperties])
	@Immutable
	static class DownloadedData {
		final File downloadedContracts
		final ContractDownloader.InclusionProperties inclusionProperties
	}
}
