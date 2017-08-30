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
import java.io.IOException;
import java.util.Collections;
import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.RepositorySystemSession;
import org.springframework.cloud.contract.maven.verifier.stubrunner.LocalStubRunner;
import org.springframework.cloud.contract.maven.verifier.stubrunner.RemoteStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubRunner;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.util.StringUtils;

/**
 *
 */
@SuppressWarnings("FieldCanBeLocal")
@Mojo(name = "run", requiresProject = false, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class RunMojo extends AbstractMojo {

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

	@Parameter(defaultValue = ConvertMojo.DEFAULT_STUBS_DIR)
	private File stubsDirectory;

	@Parameter(property = "stubsDirectory", defaultValue = "${basedir}")
	private File destination;

	/**
	 * HTTP port for the WireMock server that serves stubs
	 */
	@Parameter(property = "spring.cloud.contract.verifier.http.port", defaultValue = "8080")
	private int httpPort;

	/**
	 * Set this to "true" to bypass verifier execution.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Set this to "true" to bypass verifier test generation.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.skipTestOnly", defaultValue = "false")
	private boolean skipTestOnly;

	/**
	 * List of stubs to be downloaded and ran in a colon separated Ivy notation
	 */
	@Parameter(property = "spring.cloud.contract.verifier.stubs")
	private String stubs;

	/**
	 * Minimal port at which the stub should start
	 */
	@Parameter(property = "spring.cloud.contract.verifier.http.minPort", defaultValue = "10000")
	private int minPort;

	/**
	 * Maximal port at which the stub should start
	 */
	@Parameter(property = "spring.cloud.contract.verifier.http.maxPort", defaultValue = "15000")
	private int maxPort;

	/**
	 * Should the plugin wait for the user to press the key after starting the stubs
	 */
	@Parameter(property = "spring.cloud.contract.verifier.wait-for-key-pressed", defaultValue = "true")
	private boolean waitForKeyPressed;

	/**
	 * Classifier used by stubs artifacts.
	 */
	@Parameter(defaultValue = "stubs")
	private String stubsClassifier = "stubs";

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession mavenSession;

	private final LocalStubRunner localStubRunner;

	private final RemoteStubRunner remoteStubRunner;

	@Inject
	public RunMojo(LocalStubRunner localStubRunner, RemoteStubRunner remoteStubRunner) {
		this.localStubRunner = localStubRunner;
		this.remoteStubRunner = remoteStubRunner;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.skip || this.skipTestOnly) {
			getLog().info("Skipping verifier execution: spring.cloud.contract.verifier.skip=" + this.skip);
			return;
		}
		BatchStubRunner batchStubRunner = null;
		StubRunnerOptionsBuilder optionsBuilder = new StubRunnerOptionsBuilder()
				.withStubsClassifier(this.stubsClassifier);
		if (StringUtils.isEmpty(this.stubs)) {
			StubRunnerOptions options = optionsBuilder
					.withMinMaxPort(this.httpPort, this.httpPort)
					.build();
			StubRunner stubRunner = this.localStubRunner.run(resolveStubsDirectory().getAbsolutePath(), options);
			batchStubRunner = new BatchStubRunner(Collections.singleton(stubRunner));
		} else {
			StubRunnerOptions options = optionsBuilder
					.withStubs(this.stubs)
					.withMinMaxPort(this.minPort, this.maxPort)
					.build();
			batchStubRunner = this.remoteStubRunner.run(options, this.repoSession);
		}
		pressAnyKeyToContinue();
		if (batchStubRunner != null) {
			try {
				batchStubRunner.close();
			} catch (IOException e) {
				throw new MojoExecutionException("Fail to close batch stub runner", e);
			}
		}
	}

	private File resolveStubsDirectory() {
		if (isInsideProject()) {
			return this.stubsDirectory;
		} else {
			return this.destination;
		}
	}

	private void pressAnyKeyToContinue() {
		if (!this.waitForKeyPressed) {
			return;
		}
		getLog().info("Press ENTER to continue...");
		try {
			System.in.read();
		} catch (Exception ignored) {
		}
	}

	private boolean isInsideProject() {
		return this.mavenSession.getRequest().isProjectPresent();
	}

}
