package org.springframework.cloud.contract.maven.verifier;

import java.io.File;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.springframework.cloud.contract.maven.verifier.stubrunner.AetherStubDownloaderFactory;
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.ClasspathStubProvider;
import org.springframework.cloud.contract.stubrunner.ContractDownloader;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder;
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
	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;
	private final RepositorySystemSession repoSession;
	private final StubDownloaderBuilderProvider stubDownloaderBuilderProvider;
	private final String repositoryUsername;
	private final String repositoryPassword;
	private final String repositoryProxyHost;
	private final Integer repositoryProxyPort;
	private final boolean contractsSnapshotCheckSkip;

	MavenContractsDownloader(MavenProject project, Dependency contractDependency,
			String contractsPath, String contractsRepositoryUrl,
			StubRunnerProperties.StubsMode stubsMode, Log log,
			AetherStubDownloaderFactory aetherStubDownloaderFactory,
			RepositorySystemSession repoSession, String repositoryUsername,
			String repositoryPassword, String repositoryProxyHost,
			Integer repositoryProxyPort, boolean contractsSnapshotCheckSkip) {
		this.project = project;
		this.contractDependency = contractDependency;
		this.contractsPath = contractsPath;
		this.contractsRepositoryUrl = contractsRepositoryUrl;
		this.stubsMode = stubsMode;
		this.log = log;
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
		this.repoSession = repoSession;
		this.repositoryUsername = repositoryUsername;
		this.repositoryPassword = repositoryPassword;
		this.repositoryProxyHost = repositoryProxyHost;
		this.repositoryProxyPort = repositoryProxyPort;
		this.stubDownloaderBuilderProvider = new StubDownloaderBuilderProvider();
		this.contractsSnapshotCheckSkip = contractsSnapshotCheckSkip;
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
		StubDownloaderBuilder builder = this.stubDownloaderBuilderProvider.get();
		if (this.stubsMode == StubRunnerProperties.StubsMode.LOCAL || this.stubsMode == StubRunnerProperties.StubsMode.REMOTE) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Will download contracts from [" + this.contractsRepositoryUrl + "]. "
						+ "Stubs mode equals [" + this.stubsMode + "]");
			}
			return stubDownloader(builder);
		}
		if (customStubDownloader(builder)) {
			return builder.build(buildOptions());
		}
		if (this.stubsMode == StubRunnerProperties.StubsMode.CLASSPATH) {
			return new ClasspathStubProvider().build(buildOptions());
		}
		this.log.info("Will download contracts using current build's Maven repository setup");
		return this.aetherStubDownloaderFactory.build(this.repoSession);
	}

	private StubDownloader stubDownloader(StubDownloaderBuilder builder) {
		if (customStubDownloader(builder)) {
			return builder.build(buildOptions());
		}
		return new AetherStubDownloader(buildOptions());
	}

	private boolean customStubDownloader(StubDownloaderBuilder builder) {
		if (builder != null) {
			logStubDownloader(builder);
			return true;
		}
		return false;
	}

	private void logStubDownloader(StubDownloaderBuilder builder) {
		this.log.info("A custom stub downloader [" + builder + "] was provided");
	}

	StubRunnerOptions buildOptions() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(this.contractsRepositoryUrl)
				.withStubsMode(this.stubsMode)
				.withUsername(this.repositoryUsername)
				.withPassword(this.repositoryPassword)
				.withSnapshotCheckSkip(this.contractsSnapshotCheckSkip);
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
