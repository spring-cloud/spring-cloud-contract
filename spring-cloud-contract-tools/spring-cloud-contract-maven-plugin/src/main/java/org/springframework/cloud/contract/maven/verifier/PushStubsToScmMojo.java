/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.cloud.contract.stubrunner.ContractProjectUpdater;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.util.StringUtils;

/**
 * The generated stubs get
 */
@Mojo(name = "pushStubsToScm")
public class PushStubsToScmMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", readonly = true,
			required = true)
	private File projectBuildDirectory;

	@Parameter(property = "stubsDirectory",
			defaultValue = "${project.build.directory}/stubs")
	private File outputDirectory;

	/**
	 * Set this to "true" to bypass the whole Verifier execution
	 */
	@Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Set this to "true" to bypass only JAR creation
	 */
	@Parameter(property = "spring.cloud.contract.verifier.publish-stubs-to-scm.skip", defaultValue = "false")
	private boolean taskSkip;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

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
	 * The URL from which a contracts should get downloaded. If not provided
	 * but artifactid / coordinates notation was provided then the current Maven's build repositories will be
	 * taken into consideration
	 */
	@Parameter(property = "contractsRepositoryUrl")
	private String contractsRepositoryUrl;

	/**
	 * Picks the mode in which stubs will be found and registered
	 */
	@Parameter(property = "contractsMode", defaultValue = "CLASSPATH")
	private StubRunnerProperties.StubsMode contractsMode;


	/**
	 * If set to {@code false} will NOT delete stubs from a temporary
	 * folder after running tests
	 */
	@Parameter(property = "deleteStubsAfterTest", defaultValue = "true")
	private boolean deleteStubsAfterTest;

	/**
	 * Map of properties that can be passed to custom {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	@Parameter(property = "contractsProperties")
	private Map<String, String> contractsProperties = new HashMap<>();

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.skip || this.taskSkip) {
			getLog().info(
					"Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip="
							+ this.skip + ", spring.cloud.contract.verifier.publish-stubs-to-scm.skip=" + this.taskSkip);
			return;
		}
		if (!StringUtils.hasText(this.contractsRepositoryUrl) ||
				!this.contractsRepositoryUrl.startsWith("git")) {
			getLog().info("Skipping pushing stubs to scm since your [contractsRepositoryUrl] property doesn't start with [git]");
		}
		String projectName = this.project.getGroupId() + ":" + this.project.getArtifactId() + ":" + this.project.getVersion();
		getLog().info("Pushing Stubs to SCM for project [" + projectName + "]");
		new ContractProjectUpdater(buildOptions()).updateContractProject(projectName, this.outputDirectory.toPath());
	}

	StubRunnerOptions buildOptions() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(this.contractsRepositoryUrl)
				.withStubsMode(this.contractsMode)
				.withUsername(this.contractsRepositoryUsername)
				.withPassword(this.contractsRepositoryPassword)
				.withDeleteStubsAfterTest(this.deleteStubsAfterTest)
				.withProperties(this.contractsProperties);
		return builder.build();
	}

}
