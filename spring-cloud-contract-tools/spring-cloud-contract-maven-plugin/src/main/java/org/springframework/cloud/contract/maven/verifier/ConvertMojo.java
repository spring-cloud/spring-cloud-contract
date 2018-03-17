/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.eclipse.aether.RepositorySystemSession;
import org.springframework.cloud.contract.maven.verifier.stubrunner.AetherStubDownloaderFactory;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverter;

/**
 * Convert Spring Cloud Contract Verifier contracts into WireMock stubs mappings.
 * <p>
 * This goal allows you to generate `stubs-jar` or execute `spring-cloud-contract:run` with generated WireMock mappings.
 */
@Mojo(name = "convert", requiresProject = false,
		defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class  ConvertMojo extends AbstractMojo {

	static final String DEFAULT_STUBS_DIR = "${project.build.directory}/stubs/";
	static final String MAPPINGS_PATH = "/mappings";

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * Directory containing Spring Cloud Contract Verifier contracts written using the GroovyDSL
	 */
	@Parameter(property = "spring.cloud.contract.verifier.contractsDirectory",
			defaultValue = "${project.basedir}/src/test/resources/contracts")
	private File contractsDirectory;

	/**
	 * Directory where the generated WireMock stubs from Groovy DSL should be placed.
	 * You can then mention them in your packaging task to create jar with stubs
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

	@Parameter(defaultValue = "${project}", readonly = true) private MavenProject project;

	/**
	 * The URL from which a JAR containing the contracts should get downloaded. If not provided
	 * but artifactid / coordinates notation was provided then the current Maven's build repositories will be
	 * taken into consideration
	 */
	@Parameter(property = "contractsRepositoryUrl")
	private String contractsRepositoryUrl;

	@Parameter(property = "contractDependency")
	private Dependency contractDependency;

	/**
	 * The path in the JAR with all the contracts where contracts for this particular service lay.
	 * If not provided will be resolved to {@code groupid/artifactid}. Example:
	 * </p>
	 * If {@code groupid} is {@code com.example} and {@code artifactid} is {@code service} then the resolved path will be
	 * {@code /com/example/artifactid}
	 */
	@Parameter(property = "contractsPath")
	private String contractsPath;

	/**
	 * Picks the mode in which stubs will be found and registered
	 */
	@Parameter(property = "contractsMode", defaultValue = "CLASSPATH")
	private StubRunnerProperties.StubsMode contractsMode;

	/**
	 * If {@code true} then any file laying in a path that contains {@code build} or {@code target}
	 * will get excluded in further processing.
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
	 * If {@code true} then will not assert whether a stub / contract
	 * JAR was downloaded from local or remote location
	 */
	@Parameter(property = "contractsSnapshotCheckSkip", defaultValue = "false")
	private boolean contractsSnapshotCheckSkip;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary
	 * folder after running tests
	 */
	@Parameter(property = "deleteStubsAfterTest", defaultValue = "true")
	private boolean deleteStubsAfterTest;

	@Component(role = MavenResourcesFiltering.class, hint = "default")
	private MavenResourcesFiltering mavenResourcesFiltering;

	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;

	@Inject
	public ConvertMojo(AetherStubDownloaderFactory aetherStubDownloaderFactory) {
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
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
		File contractsDirectory = new MavenContractsDownloader(this.project, this.contractDependency,
				this.contractsPath, this.contractsRepositoryUrl, this.contractsMode, getLog(),
				this.aetherStubDownloaderFactory, this.repoSession, this.contractsRepositoryUsername,
				this.contractsRepositoryPassword, this.contractsRepositoryProxyHost, this.contractsRepositoryProxyPort,
				this.contractsSnapshotCheckSkip, this.deleteStubsAfterTest)
				.downloadAndUnpackContractsIfRequired(config, this.contractsDirectory);
		getLog().info("Directory with contract is present at [" + contractsDirectory + "]");

		new CopyContracts(this.project, this.mavenSession, this.mavenResourcesFiltering, config)
				.copy(contractsDirectory, this.stubsDirectory, rootPath);

		config.setContractsDslDir(isInsideProject() ?
				contractsDirectory : this.source);
		config.setStubsOutputDir(
				isInsideProject() ? new File(this.stubsDirectory, rootPath + MAPPINGS_PATH) : this.destination);

		getLog().info(
				"Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings");
		getLog().info(String.format(
				"     Spring Cloud Contract Verifier contracts directory: %s",
				config.getContractsDslDir()));
		getLog().info(String.format("WireMock stubs mappings directory: %s",
				config.getStubsOutputDir()));


		RecursiveFilesConverter converter = new RecursiveFilesConverter(config);
		converter.processFiles();
	}

	private boolean isInsideProject() {
		return this.mavenSession.getRequest().isProjectPresent();
	}

}
