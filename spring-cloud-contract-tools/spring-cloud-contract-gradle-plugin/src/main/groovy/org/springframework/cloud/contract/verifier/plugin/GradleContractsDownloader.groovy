package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.util.StringUtils

import java.util.concurrent.ConcurrentHashMap
/**
 * @author Marcin Grzejszczak
 */
@PackageScope
class GradleContractsDownloader {

	private static final String LATEST_VERSION = '+'

	private final Project project
	private final Logger log
	private static final Map<StubConfiguration, File> downloadedContract = new ConcurrentHashMap<>()
	
	GradleContractsDownloader(Project project, Logger log) {
		this.project = project
		this.log = log
	}
	
	File downloadAndUnpackContractsIfRequired(ContractVerifierExtension extension,
											  ContractVerifierConfigProperties config) {
		File defaultContractsDir = extension.contractsDslDir
		this.log.info("Project has group id [${this.project.group}], artifact id [${this.project.name}]")
		// download contracts, unzip them and pass as output directory
		if (shouldDownloadContracts(extension)) {
			this.log.info("For project [${this.project.name}] Download dependency is provided - will download contract jars")
			StubConfiguration configuration = stubConfiguration(extension.contractDependency)
			File cachedFolder = downloadedContract.get(configuration)
			if (cachedFolder) {
				this.log.info("For project [${this.project.name}] Returning the cached location of the contracts")
				contractDownloader(extension, configuration).updatePropertiesWithInclusion(cachedFolder, config)
				return cachedFolder
			}
			File downloadedContracts = contractDownloader(extension, configuration).unpackedDownloadedContracts(config)
			downloadedContract.put(configuration, downloadedContracts)
			return downloadedContracts
		}
		this.log.info("For project [${this.project.name}] will use contracts provided in the folder [" + defaultContractsDir + "]")
		return defaultContractsDir
	}

	private boolean shouldDownloadContracts(ContractVerifierExtension extension) {
		return (StringUtils.hasText(extension.contractsRepositoryUrl) || extension.contractsWorkOffline) &&
				(StringUtils.hasText(extension.contractDependency.artifactId) ||
						StringUtils.hasText(extension.contractDependency.stringNotation))
	}

	private ContractDownloader contractDownloader(ContractVerifierExtension extension, StubConfiguration configuration) {
		return new ContractDownloader(stubDownloader(extension), configuration,
				extension.contractsPath, this.project.group as String, this.project.name)
	}

	private AetherStubDownloader stubDownloader(ContractVerifierExtension extension) {
		return new AetherStubDownloader(
				new StubRunnerOptionsBuilder()
						.withStubRepositoryRoot(extension.contractsRepositoryUrl)
						.withWorkOffline(extension.contractsWorkOffline)
						.build())
	}

	private StubConfiguration stubConfiguration(ContractVerifierExtension.Dependency contractDependency) {
		String groupId = contractDependency.groupId
		String artifactId = contractDependency.artifactId
		String version = StringUtils.hasText(contractDependency.version) ?
				contractDependency.version : LATEST_VERSION
		String classifier = contractDependency.classifier
		String stringNotation = contractDependency.stringNotation
		if (StringUtils.hasText(stringNotation)) {
			StubConfiguration stubConfiguration = new StubConfiguration(stringNotation)
			return new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId,
					stubConfiguration.version, contractDependency.classifier)
		}
		return new StubConfiguration(groupId, artifactId, version, classifier)
	}
}
