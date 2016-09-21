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
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter;
import org.springframework.cloud.contract.verifier.wiremock.RecursiveFilesConverter;
import org.springframework.util.StringUtils;

/**
 * Convert Spring Cloud Contract Verifier contracts into WireMock stubs mappings.
 * <p>
 * This goal allows you to generate `stubs-jar` or execute `spring-cloud-contract:run` with generated WireMock mappings.
 */
@Mojo(name = "convert", requiresProject = false,
		defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class ConvertMojo extends AbstractMojo {

	/**
	 * Directory containing Spring Cloud Contract Verifier contracts written using the GroovyDSL
	 */
	@Parameter(defaultValue = "${basedir}/src/test/resources/contracts")
	private File contractsDirectory;

	/**
	 * Directory where the generated WireMock stubs from Groovy DSL should be placed.
	 * You can then mention them in your packaging task to create jar with stubs
	 */
	@Parameter(defaultValue = "${project.build.directory}/stubs")
	private File outputDirectory;

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

	@Parameter
	private DownloadContracts downloadContracts;

	@Component(role = MavenResourcesFiltering.class, hint = "default")
	private MavenResourcesFiltering mavenResourcesFiltering;

	public void execute() throws MojoExecutionException, MojoFailureException {

		if (this.skip) {
			getLog().info(String.format(
					"Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip=%s",
					this.skip));
			return;
		}
		// download contracts, unzip them and pass as output directory
		File contractsDirectory = this.contractsDirectory;
		if (this.downloadContracts != null) {
			contractsDirectory = unpackedDownloadedContracts();
		}
		getLog().info("Directory with contract is present at [" + contractsDirectory + "]");

		new CopyContracts(this.project, this.mavenSession, this.mavenResourcesFiltering)
				.copy(contractsDirectory, this.outputDirectory);

		final ContractVerifierConfigProperties config = new ContractVerifierConfigProperties();
		config.setContractsDslDir(isInsideProject() ? this.contractsDirectory : this.source);
		config.setStubsOutputDir(
				isInsideProject() ? new File(this.outputDirectory, "mappings") : this.destination);

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

	private File unpackedDownloadedContracts() {
		AetherStubDownloader stubDownloader = new AetherStubDownloader(
				new StubRunnerOptionsBuilder()
						.withStubRepositoryRoot(this.downloadContracts.getRepositoryUrl())
						.withWorkOffline(false)
						.build());
		StubConfiguration stubConfiguration = stubConfiguration();
		getLog().info("Download contracts section present. Will download stubs for [" + stubConfiguration + "]");
		Map.Entry<StubConfiguration, File> unpackedContractStubs = stubDownloader
				.downloadAndUnpackStubJar(null, stubConfiguration);
		if (unpackedContractStubs.getValue() == null) {
			throw new IllegalStateException("The stubs failed to be downloaded!");
		}
		return unpackedContractStubs.getValue();
	}

	private StubConfiguration stubConfiguration() {
		String groupId = this.downloadContracts.getGroupId();
		String artifactId = this.downloadContracts.getArtifactId();
		String version = this.downloadContracts.getVersion();
		String classifier = this.downloadContracts.getClassifier();
		String ivy = this.downloadContracts.getIvy();
		if (StringUtils.hasText(ivy)) {
			return new StubConfiguration(ivy);
		}
		return new StubConfiguration(groupId, artifactId, version, classifier);
	}

	private boolean isInsideProject() {
		return this.mavenSession.getRequest().isProjectPresent();
	}

}
