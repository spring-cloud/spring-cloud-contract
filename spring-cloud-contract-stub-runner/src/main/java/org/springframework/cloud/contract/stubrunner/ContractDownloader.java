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
 * inclusion patterns.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class ContractDownloader {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubDownloader stubDownloader;

	private final StubConfiguration contractsJarStubConfiguration;

	private final String contractsPath;

	private final String projectGroupId;

	private final String projectArtifactId;

	private final String projectVersion;

	public ContractDownloader(StubDownloader stubDownloader, StubConfiguration contractsJarStubConfiguration,
			String contractsPath, String projectGroupId, String projectArtifactId, String projectVersion) {
		this.stubDownloader = stubDownloader;
		this.contractsJarStubConfiguration = contractsJarStubConfiguration;
		this.contractsPath = contractsPath;
		this.projectGroupId = projectGroupId;
		this.projectArtifactId = projectArtifactId;
		this.projectVersion = projectVersion;
	}

	/**
	 * Downloads JAR containing all the contracts. The JAR with the contracts contains all
	 * the contracts for all the projects. We're interested only in its subset.
	 * @return location of the unpacked downloaded stubs
	 */
	public File unpackAndDownloadContracts() {
		if (log.isDebugEnabled()) {
			log.debug("Will download contracts for [" + this.contractsJarStubConfiguration + "]");
		}
		Map.Entry<StubConfiguration, File> unpackedContractStubs = this.stubDownloader
				.downloadAndUnpackStubJar(this.contractsJarStubConfiguration);
		if (unpackedContractStubs == null) {
			throw new IllegalStateException("The contracts failed to be downloaded!");
		}
		return unpackedContractStubs.getValue();
	}

	/**
	 * After JAR with all the contracts is downloaded and unpacked - we need to get new
	 * inclusion pattern for those contracts. The JAR with the contracts contains all the
	 * contracts for all the projects. We're interested only in its subset.
	 * @param contractsDirectory - location of the unpacked downloaded stubs.
	 * @return new inclusion properties, calculated for those downloaded contracts.
	 */
	public InclusionProperties createNewInclusionProperties(File contractsDirectory) {
		String pattern;
		String includedAntPattern;
		if (StringUtils.hasText(this.contractsPath)) {
			pattern = patternFromProperty(contractsDirectory);
			log.info("Will pick a pattern from the contractPath property");
			includedAntPattern = wrapWithAntPattern(contractsPath());
		}
		else {
			log.info("Will pick a pattern from group id and artifact id");
			if (hasGavInPath(contractsDirectory)) {
				if (log.isDebugEnabled()) {
					log.debug("Group & artifact in path");
				}
				contractsDirectory = contractsSubDirIfPresent(contractsDirectory);
				// we're already under proper folder (for the given group and artifact)
				pattern = fileToPattern(contractsDirectory);
				includedAntPattern = "**/";
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("No group & artifact in path");
				}
				pattern = groupArtifactToPattern(contractsDirectory);
				includedAntPattern = wrapWithAntPattern(slashSeparatedGroupId() + "/" + this.projectArtifactId);
			}
		}
		log.info("Pattern to pick contracts equals [" + pattern + "]");
		log.info("Ant Pattern to pick files equals [" + includedAntPattern + "]");
		return new InclusionProperties(pattern, includedAntPattern);
	}

	private File contractsSubDirIfPresent(File contractsDirectory) {
		File contracts = new File(contractsDirectory, "contracts");
		if (contracts.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("Contracts folder found [" + contracts + "]");
			}
			contractsDirectory = contracts;
		}
		return contractsDirectory;
	}

	private boolean hasGavInPath(File file) {
		return hasVersionInPath(file) && hasSeparatedGroupInPath(file, File.separator)
				|| hasSeparatedGroupInPath(file, ".");
	}

	private boolean hasVersionInPath(File file) {
		return file.getAbsolutePath().contains(this.projectVersion);
	}

	private boolean hasSeparatedGroupInPath(File file, String separator) {
		return file.getAbsolutePath().contains(groupAndArtifact(separator));
	}

	private String groupAndArtifact(String separator) {
		return this.projectGroupId + separator + this.projectArtifactId;
	}

	private String patternFromProperty(File contractsDirectory) {
		return ("^" + contractsDirectory.getAbsolutePath() + "(" + File.separator + ")?" + ".*"
				+ contractsPath().replace("/", File.separator) + ".*$").replace("\\", "\\\\");
	}

	private String contractsPath() {
		return surroundWithSeparator(this.contractsPath);
	}

	private String surroundWithSeparator(String string) {
		String path = string.startsWith(File.separator) ? string : File.separator + string;
		return path.endsWith(File.separator) ? path : path + File.separator;
	}

	private String wrapWithAntPattern(String path) {
		String changedPath = surroundWithSeparator(path).replace(File.separator, "/");
		return "**" + changedPath.replace(File.separator, "/") + "**/";
	}

	private String groupArtifactToPattern(File contractsDirectory) {
		return ("^" + contractsDirectory.getAbsolutePath() + "(" + File.separator + ")?" + ".*"
				+ slashSeparatedGroupId() + File.separator + this.projectArtifactId + File.separator + ".*$")
						.replace("\\", "\\\\");
	}

	private String fileToPattern(File contractsDirectory) {
		return ("^" + contractsDirectory.getAbsolutePath() + ".*$").replace("\\", "\\\\");
	}

	private String slashSeparatedGroupId() {
		return this.projectGroupId.replace(".", File.separator);
	}

	/**
	 * Holder for updated inclusion properties, which are calculated after jar with
	 * contracts was downloaded / unpacked.
	 */
	public static class InclusionProperties {

		/**
		 * @see ContractVerifierConfigProperties.includedContracts
		 */
		private final String includedContracts;

		/**
		 * @see ContractVerifierConfigProperties.includedRootFolderAntPattern
		 */
		private final String includedRootFolderAntPattern;

		InclusionProperties(final String includedContracts, final String includedRootFolderAntPattern) {
			this.includedContracts = includedContracts;
			this.includedRootFolderAntPattern = includedRootFolderAntPattern;
		}

		public String getIncludedContracts() {
			return includedContracts;
		}

		public String getIncludedRootFolderAntPattern() {
			return includedRootFolderAntPattern;
		}

	}

}
