/*
 * Copyright 2013-present the original author or authors.
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

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Small helper for the project-per-folder integration tests.
 */
abstract class AbstractProjectIntegrationTests extends AbstractMojoIntegrationTests {

	private Path targetDir;

	@BeforeEach
	void setUpTargetDir() throws Exception {
		Path projectDir = Path.of(MojoExtension.getBasedir());
		this.targetDir = projectDir.resolve("target");
		deleteDirectoryIfExists(this.targetDir);
		Files.createDirectories(this.targetDir);
	}

	@AfterEach
	void cleanTargetDir() throws Exception {
		deleteDirectoryIfExists(this.targetDir);
	}

	void runConvertMojo(ConvertMojo mojo) throws Exception {
		configureRepositorySystem(mojo);
		setupBuildPaths(mojo, this.targetDir);
		Path stubsDir = this.targetDir.resolve("stubs");
		MojoExtension.setVariableValueToObject(mojo, "stubsDirectory", stubsDir.toFile());
		MojoExtension.setVariableValueToObject(mojo, "incrementalContractStubs", false);
		mojo.execute();
		then(Files.exists(stubsDir)).as("generated stubs directory should exist at %s", stubsDir).isTrue();
	}

	void runGenerateTestsMojo(GenerateTestsMojo mojo) throws Exception {
		configureRepositorySystem(mojo);
		setupBuildPaths(mojo, this.targetDir);
		Path generatedSources = this.targetDir.resolve("generated-test-sources/contracts");
		Path generatedResources = this.targetDir.resolve("generated-test-resources/contracts");
		MojoExtension.setVariableValueToObject(mojo, "generatedTestSourcesDir", generatedSources.toFile());
		MojoExtension.setVariableValueToObject(mojo, "generatedTestResourcesDir", generatedResources.toFile());
		MojoExtension.setVariableValueToObject(mojo, "incrementalContractTests", false);
		mojo.execute();
		then(Files.exists(generatedSources)).as("generated test sources should exist at %s", generatedSources).isTrue();
		then(Files.exists(generatedResources)).as("generated test resources should exist at %s", generatedResources)
			.isTrue();
	}

	void configureRemoteRepo(AbstractMojo mojo, WireMockRepositoryServer server) throws Exception {
		MojoExtension.setVariableValueToObject(mojo, "contractsRepositoryUrl", server.baseUrl());
	}

	Path repositoryRoot() {
		return Path.of("src/test/resources/m2repo/repository").toAbsolutePath();
	}

	private void configureRepositorySystem(AbstractMojo mojo) throws Exception {
		RepositorySystem repoSystem = new RepositorySystemSupplier().get();
		DefaultRepositorySystemSession repoSession = MavenRepositorySystemUtils.newSession();
		LocalRepository localRepo = new LocalRepository(this.targetDir.resolve("local-repo").toFile());
		repoSession.setLocalRepositoryManager(repoSystem.newLocalRepositoryManager(repoSession, localRepo));
		repoSession.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));
		MojoExtension.setVariableValueToObject(mojo, "repositorySystem", repoSystem);
		MojoExtension.setVariableValueToObject(mojo, "repoSession", repoSession);
	}

}
