/*
 *  Copyright 2013-2016 the original author or authors.
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
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter;
import org.springframework.cloud.contract.verifier.wiremock.RecursiveFilesConverter;

/**
 * Convert Spring Cloud Contract Verifier contracts into WireMock stubs mappings.
 * <p>
 * This goal allows you to generate `stubs-jar` or execute `spring-cloud-contract:run` with generated WireMock mappings.
 */
@Mojo(name = "convert", requiresProject = false,
		defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class ConvertMojo extends AbstractMojo {

	public static final String DEFAULT_STUBS_DIR = "${project.build.directory}/stubs/";
	public static final String MAPPINGS_PATH = "mappings";

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	/**
	 * Directory containing Spring Cloud Contract Verifier contracts written using the GroovyDSL
	 */
	@Parameter(defaultValue = "${basedir}/src/test/resources/contracts")
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
	 * If {@code true} then JAR with contracts will be taken from local maven repository
	 */
	@Parameter(property = "contractsWorkOffline", defaultValue = "false")
	private boolean contractsWorkOffline;

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
		// download contracts, unzip them and pass as output directory
		ContractVerifierConfigProperties config = new ContractVerifierConfigProperties();
		File contractsDirectory = new MavenContractsDownloader(this.project, this.contractDependency,
				this.contractsPath, this.contractsRepositoryUrl, this.contractsWorkOffline, getLog(),
				this.aetherStubDownloaderFactory, this.repoSession).downloadAndUnpackContractsIfRequired(config, this.contractsDirectory);
		getLog().info("Directory with contract is present at [" + contractsDirectory + "]");

		new CopyContracts(this.project, this.mavenSession, this.mavenResourcesFiltering)
				.copy(contractsDirectory, this.stubsDirectory);

		config.setContractsDslDir(isInsideProject() ? contractsDirectory : this.source);
		config.setStubsOutputDir(
				isInsideProject() ? new File(this.stubsDirectory, MAPPINGS_PATH) : this.destination);

		getLog().info(
				"Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings");
		getLog().info(String.format(
				"     Spring Cloud Contract Verifier contracts directory: %s",
				config.getContractsDslDir()));
		getLog().info(String.format("WireMock stubs mappings directory: %s",
				config.getStubsOutputDir()));


		RecursiveFilesConverter converter = new RecursiveFilesConverter(
				new DslToWireMockClientConverter(), config);
		converter.processFiles();
	}

	private boolean isInsideProject() {
		return this.mavenSession.getRequest().isProjectPresent();
	}

}
