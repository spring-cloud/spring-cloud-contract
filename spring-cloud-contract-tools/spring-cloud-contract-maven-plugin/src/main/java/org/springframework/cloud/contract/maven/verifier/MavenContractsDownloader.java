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
	private final boolean contractsWorkOffline;
	private final Log log;
	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;
	private final RepositorySystemSession repoSession;
	private final StubDownloaderBuilderProvider stubDownloaderBuilderProvider;

	MavenContractsDownloader(MavenProject project, Dependency contractDependency,
			String contractsPath, String contractsRepositoryUrl,
			boolean contractsWorkOffline, Log log,
			AetherStubDownloaderFactory aetherStubDownloaderFactory,
			RepositorySystemSession repoSession) {
		this.project = project;
		this.contractDependency = contractDependency;
		this.contractsPath = contractsPath;
		this.contractsRepositoryUrl = contractsRepositoryUrl;
		this.contractsWorkOffline = contractsWorkOffline;
		this.log = log;
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
		this.repoSession = repoSession;
		this.stubDownloaderBuilderProvider = new StubDownloaderBuilderProvider();
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
		if (StringUtils.hasText(this.contractsRepositoryUrl) || this.contractsWorkOffline) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Will download contracts from [" + this.contractsRepositoryUrl + "]. "
						+ "Work offline switch equals to [" + this.contractsWorkOffline + "]");
			}
			if (builder != null) {
				logStubDownloader(builder);
				return builder.build(buildOptions());
			}
			return new AetherStubDownloader(buildOptions());
		}
		this.log.info("Will download contracts using current build's Maven repository setup");
		if (builder != null) {
			logStubDownloader(builder);
			return builder.build(buildOptions());
		}
		if (StringUtils.isEmpty(this.contractsRepositoryUrl) && !this.contractsWorkOffline) {
			return new ClasspathStubProvider().build(buildOptions());
		}
		return this.aetherStubDownloaderFactory.build(this.repoSession);
	}

	private void logStubDownloader(StubDownloaderBuilder builder) {
		this.log.info("A custom stub downloader [" + builder + "] was provided");
	}

	StubRunnerOptions buildOptions() {
		return new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(this.contractsRepositoryUrl)
				.withWorkOffline(this.contractsWorkOffline)
				.build();
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
