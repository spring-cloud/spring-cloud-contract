package org.springframework.cloud.contract.verifier.plugin

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.StubDownloader
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.util.StringUtils
/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
class GradleContractsDownloader {

	private static final String LATEST_VERSION = '+'

	private final Project project
	private final Logger log
	protected static final Map<StubConfiguration, File> downloadedContract = new ConcurrentHashMap<>()
	
	GradleContractsDownloader(Project project, Logger log) {
		this.project = project
		this.log = log
	}
	
	File downloadAndUnpackContractsIfRequired(ContractVerifierExtension extension,
											  ContractVerifierConfigProperties config) {
		File defaultContractsDir = extension.contractsDslDir
		this.log.info("Project has group id [{}], artifact id [{}]", this.project.group, this.project.name)
		// download contracts, unzip them and pass as output directory
		if (shouldDownloadContracts(extension)) {
			this.log.info("For project [${this.project.name}] Download dependency is provided - will download contract jars")
			this.log.info("Contract dependency [{}]", extension.contractDependency)
			StubConfiguration configuration = stubConfiguration(extension.contractDependency)
			this.log.info("Got the following contract dependency to download [{}]", configuration)
			this.log.info("The contract dependency is a changing one [{}] and cache download switch is set to [{}]",
					configuration.isVersionChanging(), extension.contractRepository.cacheDownloadedContracts)
			if (!configuration.isVersionChanging() && extension.contractRepository.cacheDownloadedContracts) {
				this.log.info("Resolved a non changing version - will try to return the folder from a cache")
				File cachedFolder = downloadedContract.get(configuration)
				if (cachedFolder) {
					this.log.info("For project [{}] returning the cached location of the contracts", this.project.name)
					contractDownloader(extension, configuration).updatePropertiesWithInclusion(cachedFolder, config)
					return cachedFolder
				}
			}
			File downloadedContracts = contractDownloader(extension, configuration).unpackedDownloadedContracts(config)
			downloadedContract.put(configuration, downloadedContracts)
			return downloadedContracts
		}
		this.log.info("For project [{}] will use contracts provided in the folder [{}]", this.project.name, defaultContractsDir)
		return defaultContractsDir
	}

	private boolean shouldDownloadContracts(ContractVerifierExtension extension) {
		return [StubRunnerProperties.StubsMode.LOCAL, StubRunnerProperties.StubsMode.REMOTE].any {
			it == extension.contractsMode } && (StringUtils.hasText(extension.contractDependency.artifactId) ||
						StringUtils.hasText(extension.contractDependency.stringNotation))
	}

	protected ContractDownloader contractDownloader(ContractVerifierExtension extension, StubConfiguration configuration) {
		return new ContractDownloader(stubDownloader(extension), configuration,
				extension.contractsPath, this.project.group as String, this.project.name)
	}

	protected StubDownloader stubDownloader(ContractVerifierExtension extension) {
        StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider()
		StubRunnerOptionsBuilder options = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(extension.contractRepository.repositoryUrl)
				.withStubsMode(extension.contractsMode)
				.withUsername(extension.contractRepository.username)
				.withPassword(extension.contractRepository.password)
				.withSnapshotCheckSkip(extension.contractsSnapshotCheckSkip)
		if (extension.contractRepository.proxyPort) {
			options = options.withProxy(extension.contractRepository.proxyHost, extension.contractRepository.proxyPort)
		}
		return provider.getOrDefaultDownloader(options.build())
	}

	@PackageScope StubConfiguration stubConfiguration(ContractVerifierExtension.Dependency contractDependency) {
		String groupId = contractDependency.groupId
		String artifactId = contractDependency.artifactId
		String version = StringUtils.hasText(contractDependency.version) ?
				contractDependency.version : LATEST_VERSION
		String classifier = contractDependency.classifier
		String stringNotation = contractDependency.stringNotation
		if (StringUtils.hasText(stringNotation)) {
			StubConfiguration stubConfiguration = new StubConfiguration(stringNotation)
			return new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId,
					stubConfiguration.version, stubConfiguration.classifier)
		}
		return new StubConfiguration(groupId, artifactId, version, classifier)
	}
}
