package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.springframework.cloud.contract.stubrunner.ContractDownloader;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.util.StringUtils;

/**
 * Downloads JAR with contracts
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
class MavenContractsDownloader {

	private static final String LATEST_VERSION = "+";
	private static final String CONTRACTS_DIRECTORY_PROP = "CONTRACTS_DIRECTORY";

	private final MavenProject project;
	private final Dependency contractDependency;
	private final String contractsPath;
	private final String contractsRepositoryUrl;
	private final StubRunnerProperties.StubsMode stubsMode;
	private final Log log;
	private final StubDownloaderBuilderProvider stubDownloaderBuilderProvider;
	private final String repositoryUsername;
	private final String repositoryPassword;
	private final String repositoryProxyHost;
	private final Integer repositoryProxyPort;
	private final boolean contractsSnapshotCheckSkip;
	private final boolean deleteStubsAfterTest;
	private final Map<String, String> contractsProperties;

	MavenContractsDownloader(MavenProject project, Dependency contractDependency,
			String contractsPath, String contractsRepositoryUrl,
			StubRunnerProperties.StubsMode stubsMode, Log log, String repositoryUsername,
			String repositoryPassword, String repositoryProxyHost,
			Integer repositoryProxyPort, boolean contractsSnapshotCheckSkip,
			boolean deleteStubsAfterTest, Map<String, String> contractsProperties) {
		this.project = project;
		this.contractDependency = contractDependency;
		this.contractsPath = contractsPath;
		this.contractsRepositoryUrl = contractsRepositoryUrl;
		this.stubsMode = stubsMode;
		this.log = log;
		this.repositoryUsername = repositoryUsername;
		this.repositoryPassword = repositoryPassword;
		this.repositoryProxyHost = repositoryProxyHost;
		this.repositoryProxyPort = repositoryProxyPort;
		this.stubDownloaderBuilderProvider = new StubDownloaderBuilderProvider();
		this.contractsSnapshotCheckSkip = contractsSnapshotCheckSkip;
		this.deleteStubsAfterTest = deleteStubsAfterTest;
		this.contractsProperties = contractsProperties;
	}

	File downloadAndUnpackContractsIfRequired(ContractVerifierConfigProperties config, File defaultContractsDir) {
		String contractsDirFromProp = this.project.getProperties().getProperty(CONTRACTS_DIRECTORY_PROP);
		File downloadedContractsDir = StringUtils.hasText(contractsDirFromProp) ?
				new File(contractsDirFromProp) : null;
		// reuse downloaded contracts from another mojo
		if (downloadedContractsDir != null && downloadedContractsDir.exists()) {
			this.log.info("Another mojo has downloaded the contracts - will reuse them from [" + downloadedContractsDir + "]");
			contractDownloader().updatePropertiesWithInclusion(downloadedContractsDir, config);
			return downloadedContractsDir;
		} else if (shouldDownloadContracts()) {
			this.log.info("Download dependency is provided - will download contract jars");
			File downloadedContracts = contractDownloader().unpackedDownloadedContracts(config);
			this.project.getProperties().setProperty(CONTRACTS_DIRECTORY_PROP, downloadedContracts.getAbsolutePath());
			return downloadedContracts;
		}
		this.log.info("Will use contracts provided in the folder [" + defaultContractsDir + "]");
		return defaultContractsDir;
	}

	private boolean shouldDownloadContracts() {
		return this.contractDependency != null && StringUtils.hasText(this.contractDependency.getArtifactId());
	}

	private ContractDownloader contractDownloader() {
		return new ContractDownloader(stubDownloader(), stubConfiguration(),
				this.contractsPath, this.project.getGroupId(), this.project.getArtifactId());
	}

	private StubDownloader stubDownloader() {
		StubRunnerOptions stubRunnerOptions = buildOptions();
		return this.stubDownloaderBuilderProvider.
				get(stubRunnerOptions);
	}

	StubRunnerOptions buildOptions() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(this.contractsRepositoryUrl)
				.withStubsMode(this.stubsMode)
				.withUsername(this.repositoryUsername)
				.withPassword(this.repositoryPassword)
				.withSnapshotCheckSkip(this.contractsSnapshotCheckSkip)
				.withDeleteStubsAfterTest(this.deleteStubsAfterTest)
				.withProperties(this.contractsProperties);
		if (this.repositoryProxyPort != null) {
			builder.withProxy(this.repositoryProxyHost, this.repositoryProxyPort);
		}
		return builder.build();
	}

	private StubConfiguration stubConfiguration() {
		String groupId = this.contractDependency.getGroupId();
		String artifactId = this.contractDependency.getArtifactId();
		String version = StringUtils.hasText(this.contractDependency.getVersion()) ?
				this.contractDependency.getVersion() : LATEST_VERSION;
		String classifier = this.contractDependency.getClassifier();
		return new StubConfiguration(groupId, artifactId, version, classifier);
	}
}
