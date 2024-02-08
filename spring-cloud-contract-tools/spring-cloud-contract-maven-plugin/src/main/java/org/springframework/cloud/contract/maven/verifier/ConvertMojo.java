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

package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.eclipse.aether.RepositorySystemSession;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverter;
import org.springframework.cloud.contract.verifier.converter.ToYamlConverter;

import static org.springframework.cloud.contract.maven.verifier.ChangeDetector.inputFilesChangeDetected;

/**
 * Convert Spring Cloud Contract Verifier contracts into stubs mappings.
 * <p>
 * This goal allows you to generate `stubs-jar` or execute `spring-cloud-contract:run`
 * with generated mappings.
 *
 * @author Mariusz Smykula
 */
@Mojo(name = "convert", requiresProject = false, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class ConvertMojo extends AbstractMojo {

	static final String DEFAULT_STUBS_DIR = "${project.build.directory}/stubs/";
	static final String MAPPINGS_PATH = "/mappings";
	static final String CONTRACTS_PATH = "/contracts";
	static final String ORIGINAL_PATH = "/original";

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * Directory containing Spring Cloud Contract Verifier contracts written using the
	 * GroovyDSL.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.contractsDirectory",
			defaultValue = "${project.basedir}/src/test/resources/contracts")
	private File contractsDirectory;

	/**
	 * Directory where the generated WireMock stubs from Groovy DSL should be placed. You
	 * can then mention them in your packaging task to create jar with stubs
	 */
	@Parameter(defaultValue = DEFAULT_STUBS_DIR)
	private File stubsDirectory;

	/**
	 * Directory containing contracts written using the GroovyDSL
	 * <p>
	 * This parameter is only used when goal is executed outside of maven project.
	 */
	@Parameter(property = "contractsDirectory", defaultValue = "${basedir}")
	private File source;

	@Parameter(property = "stubsDirectory", defaultValue = "${basedir}")
	private File destination;

	@Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession mavenSession;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * The URL from which a JAR containing the contracts should get downloaded. If not
	 * provided but artifactid / coordinates notation was provided then the current
	 * Maven's build repositories will be taken into consideration
	 */
	@Parameter(property = "contractsRepositoryUrl")
	private String contractsRepositoryUrl;

	@Parameter(property = "contractDependency")
	private Dependency contractDependency;

	/**
	 * The path in the JAR with all the contracts where contracts for this particular
	 * service lay. If not provided will be resolved to {@code groupid/artifactid}.
	 * Example: If {@code groupid} is {@code com.example} and {@code artifactid} is
	 * {@code service} then the resolved path will be {@code /com/example/artifactid}
	 */
	@Parameter(property = "contractsPath")
	private String contractsPath;

	/**
	 * Picks the mode in which stubs will be found and registered.
	 */
	@Parameter(property = "contractsMode", defaultValue = "CLASSPATH")
	private StubRunnerProperties.StubsMode contractsMode;

	/**
	 * If {@code true} then any file laying in a path that contains {@code build} or
	 * {@code target} will get excluded in further processing.
	 */
	@Parameter(property = "excludeBuildFolders", defaultValue = "false")
	private boolean excludeBuildFolders;

	/**
	 * The user name to be used to connect to the repo with contracts.
	 */
	@Parameter(property = "contractsRepositoryUsername")
	private String contractsRepositoryUsername;

	/**
	 * The password to be used to connect to the repo with contracts.
	 */
	@Parameter(property = "contractsRepositoryPassword")
	private String contractsRepositoryPassword;

	/**
	 * The proxy host to be used to connect to the repo with contracts.
	 */
	@Parameter(property = "contractsRepositoryProxyHost")
	private String contractsRepositoryProxyHost;

	/**
	 * The proxy port to be used to connect to the repo with contracts.
	 */
	@Parameter(property = "contractsRepositoryProxyPort")
	private Integer contractsRepositoryProxyPort;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary folder after running
	 * tests.
	 */
	@Parameter(property = "deleteStubsAfterTest", defaultValue = "true")
	private boolean deleteStubsAfterTest;

	/**
	 * Map of properties that can be passed to custom
	 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}.
	 */
	@Parameter(property = "contractsProperties")
	private Map<String, String> contractsProperties = new HashMap<>();

	/**
	 * If {@code true} then will convert contracts to a YAML representation.
	 */
	@Parameter(property = "convertToYaml", defaultValue = "false")
	private boolean convertToYaml;

	@Component(role = MavenResourcesFiltering.class, hint = "default")
	private MavenResourcesFiltering mavenResourcesFiltering;

	/**
	 * When enabled, this flag will tell stub runner to throw an exception when no stubs /
	 * contracts were found.
	 */
	@Parameter(property = "failOnNoContracts", defaultValue = "true")
	private boolean failOnNoContracts;

	/**
	 * If set to true then stubs are created only when contracts have changed since last
	 * build.
	 */
	@Parameter(property = "incrementalContractStubs", defaultValue = "true")
	private boolean incrementalContractStubs = true;

	@Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
	private MojoExecution mojoExecution;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Override
	public void execute() throws MojoExecutionException {
		if (this.skip) {
			getLog().info(String.format(
					"Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip=%s",
					this.skip));
			return;
		}
		String groupId = this.project.getGroupId();
		String artifactId = this.project.getArtifactId();
		String version = this.project.getVersion();
		String rootPath = "META-INF/" + groupId + "/" + artifactId + "/" + version;
		// download contracts, unzip them and pass as output directory
		ContractVerifierConfigProperties config = new ContractVerifierConfigProperties();
		config.setExcludeBuildFolders(this.excludeBuildFolders);
		File contractsDirectory = locationOfContracts(config);
		contractsDirectory = contractSubfolderIfPresent(contractsDirectory);

		if (this.incrementalContractStubs && !inputFilesChangeDetected(contractsDirectory, mojoExecution, session)) {
			getLog().info("Nothing to generate - all stubs are up to date");
			return;
		}

		File contractsDslDir = contractsDslDir(contractsDirectory);
		LeftOverPrevention leftOverPrevention = new LeftOverPrevention(this.stubsDirectory, mojoExecution, session);

		File copiedContracts = copyContracts(rootPath, config, contractsDirectory);
		if (this.convertToYaml) {
			contractsDslDir = copiedContracts;
			convertBackedUpDslsToYaml(rootPath, config, contractsDirectory, contractsDslDir);
		}
		config.setContractsDslDir(contractsDslDir);
		config.setStubsOutputDir(stubsOutputDir(rootPath));
		logSetup(config, contractsDslDir);
		RecursiveFilesConverter converter = new RecursiveFilesConverter(config.getStubsOutputDir(),
				config.getContractsDslDir(), config.getExcludedFiles(), config.getIncludedContracts(),
				config.isExcludeBuildFolders());
		converter.processFiles();
		leftOverPrevention.deleteLeftOvers();
	}

	private void convertBackedUpDslsToYaml(String rootPath, ContractVerifierConfigProperties config,
			File contractsDirectory, File contractsDslDir) throws MojoExecutionException {
		copyOriginals(rootPath, config, contractsDirectory);
		ToYamlConverter.replaceContractWithYaml(contractsDslDir);
		getLog().info("Replaced DSL files with their YAML representation at [" + contractsDslDir + "]");
	}

	private File copyOriginals(String rootPath, ContractVerifierConfigProperties config, File contractsDirectory)
			throws MojoExecutionException {
		File outputFolderWithOriginals = new File(this.stubsDirectory, rootPath + ORIGINAL_PATH);
		new CopyContracts(this.project, this.mavenSession, this.mavenResourcesFiltering, config)
				.copy(contractsDirectory, outputFolderWithOriginals);
		return outputFolderWithOriginals;
	}

	private File copyContracts(String rootPath, ContractVerifierConfigProperties config, File contractsDirectory)
			throws MojoExecutionException {
		File outputFolderWithContracts = this.stubsDirectory.getPath().endsWith("contracts") ? this.stubsDirectory
				: new File(this.stubsDirectory, rootPath + CONTRACTS_PATH);
		new CopyContracts(this.project, this.mavenSession, this.mavenResourcesFiltering, config)
				.copy(contractsDirectory, outputFolderWithContracts);
		return outputFolderWithContracts;
	}

	private void logSetup(ContractVerifierConfigProperties config, File contractsDslDir) {
		if (getLog().isDebugEnabled()) {
			getLog().debug("The contracts dir equals [" + contractsDslDir + "]");
		}
		getLog().info("Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings");
		getLog().info(String.format("     Spring Cloud Contract Verifier contracts directory: %s",
				config.getContractsDslDir()));
		getLog().info(String.format("Stub Server stubs mappings directory: %s", config.getStubsOutputDir()));
	}

	private File contractSubfolderIfPresent(File contractsDirectory) {
		File contractsSubFolder = new File(contractsDirectory, "contracts");
		if (contractsSubFolder.exists()) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("The subfolder [contracts] exists, will pick it as a source of contracts");
			}
			contractsDirectory = contractsSubFolder;
		}
		return contractsDirectory;
	}

	private File locationOfContracts(ContractVerifierConfigProperties config) {
		return new MavenContractsDownloader(this.project, this.contractDependency, this.contractsPath,
				this.contractsRepositoryUrl, this.contractsMode, getLog(), this.contractsRepositoryUsername,
				this.contractsRepositoryPassword, this.contractsRepositoryProxyHost, this.contractsRepositoryProxyPort,
				this.deleteStubsAfterTest, this.contractsProperties, this.failOnNoContracts)
						.downloadAndUnpackContractsIfRequired(config, this.contractsDirectory);
	}

	private File stubsOutputDir(String rootPath) {
		return isInsideProject() ? new File(this.stubsDirectory, rootPath + MAPPINGS_PATH) : this.destination;
	}

	private File contractsDslDir(File contractsDirectory) {
		return isInsideProject() ? contractsDirectory : this.source;
	}

	private boolean isInsideProject() {
		return this.mavenSession.getRequest().isProjectPresent();
	}

}
