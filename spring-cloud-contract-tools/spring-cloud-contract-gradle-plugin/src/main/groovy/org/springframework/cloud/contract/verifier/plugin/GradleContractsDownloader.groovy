package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader
import org.springframework.cloud.contract.stubrunner.ContractDownloader
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder
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
	
	File downloadAndUnpackContractsIfRequired(ContractVerifierExtension extension) {
		File defaultContractsDir = extension.contractsDslDir
		// download contracts, unzip them and pass as output directory
		if (shouldDownloadContracts(extension)) {
			this.log.info("For project [${this.project.name}] Download dependency is provided - will download contract jars")
			StubConfiguration configuration = stubConfiguration(extension.contractDependency)
			if (downloadedContract.get(configuration)) {
				this.log.info("For project [${this.project.name}] Returning the cached location of the contracts")
				return downloadedContract.get(configuration)
			}
			File downloadedContracts = contractDownloader(extension, configuration).unpackedDownloadedContracts(
					ExtensionToProperties.fromExtension(extension))
			downloadedContract.put(configuration, downloadedContracts)
			return downloadedContracts
		}
		this.log.info("For project [${this.project.name}] will use contracts provided in the folder [" + defaultContractsDir + "]")
		return defaultContractsDir
	}

	private boolean shouldDownloadContracts(ContractVerifierExtension extension) {
		return StringUtils.hasText(extension.contractDependency.artifactId)
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
			return new StubConfiguration(stringNotation)
		}
		return new StubConfiguration(groupId, artifactId, version, classifier)
	}
}
