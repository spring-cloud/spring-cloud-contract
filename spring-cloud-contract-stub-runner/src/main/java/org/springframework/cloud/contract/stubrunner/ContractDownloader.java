package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.util.StringUtils;

/**
 * Downloads a JAR with contracts and sets up the plugin configuration with proper
 * inclusion patterns
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class ContractDownloader {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubDownloader stubDownloader;
	private final StubConfiguration contractsJarStubConfiguration;
	private final String contractsPath;
	private final String projectGroupId;
	private final String projectArtifactId;

	public ContractDownloader(StubDownloader stubDownloader,
			StubConfiguration contractsJarStubConfiguration,
			String contractsPath, String projectGroupId, String projectArtifactId) {
		this.stubDownloader = stubDownloader;
		this.contractsJarStubConfiguration = contractsJarStubConfiguration;
		this.contractsPath = contractsPath;
		this.projectGroupId = projectGroupId;
		this.projectArtifactId = projectArtifactId;
	}

	/**
	 * Downloads JAR containing all the contracts. Plugin configuration gets updated with
	 * the inclusion pattern for the downloaded contracts. The JAR with the contracts contains all
	 * the contracts for all the projects. We're interested only in its subset.
	 *
	 * @param config - Plugin configuration that will get updated with the inclusion pattern
	 * @return location of the unpacked downloaded stubs
	 */
	public File unpackedDownloadedContracts(ContractVerifierConfigProperties config) {
		File contractsDirectory = unpackAndDownloadContracts();
		updatePropertiesWithInclusion(contractsDirectory, config);
		return contractsDirectory;
	}

	public ContractVerifierConfigProperties updatePropertiesWithInclusion(File contractsDirectory,
			ContractVerifierConfigProperties config) {
		String pattern = StringUtils.hasText(this.contractsPath) ? patternFromProperty(contractsDirectory) :
				groupArtifactToPattern(contractsDirectory);
		log.info("Pattern to pick contracts equals [" + pattern + "]");
		config.setIncludedContracts(pattern);
		return config;
	}

	private String patternFromProperty(File contractsDirectory) {
		return "^" + contractsDirectory.getAbsolutePath() + contractsPath() + ".*$";
	}

	private String contractsPath() {
		return this.contractsPath.startsWith(File.separator) ? this.contractsPath : File.separator + this.contractsPath;
	}

	private File unpackAndDownloadContracts() {
		log.info("Will download contracts for [" + this.contractsJarStubConfiguration + "]");
		Map.Entry<StubConfiguration, File> unpackedContractStubs = this.stubDownloader
				.downloadAndUnpackStubJar(null, this.contractsJarStubConfiguration);
		if (unpackedContractStubs == null) {
			throw new IllegalStateException("The contracts failed to be downloaded!");
		}
		return unpackedContractStubs.getValue();
	}

	private String groupArtifactToPattern(File contractsDirectory) {
		return "^" +
				contractsDirectory.getAbsolutePath() +
				File.separator +
				this.projectGroupId.replace(".", File.separator) +
				File.separator +
				this.projectArtifactId
				+ File.separator +
				".*$";
	}
}
