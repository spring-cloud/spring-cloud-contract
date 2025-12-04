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
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class GenerateTestsMojoIntegrationTests extends AbstractMojoIntegrationTests {

	@Test
	@InjectMojo(goal = "generateTests", pom = "pom.xml")
	@Basedir("src/test/projects/basic")
	void shouldGenerateTestsFromContracts(GenerateTestsMojo mojo) throws Exception {
		// given
		Path projectDir = Path.of(MojoExtension.getBasedir());
		Path targetDir = projectDir.resolve("target");
		deleteDirectoryIfExists(targetDir);
		Files.createDirectories(targetDir);
		Path generatedSources = targetDir.resolve("generated-test-sources/contracts");
		Path generatedResources = targetDir.resolve("generated-test-resources/contracts");
		setupBuildPaths(mojo, targetDir);
		MojoExtension.setVariableValueToObject(mojo, "generatedTestSourcesDir", generatedSources.toFile());
		MojoExtension.setVariableValueToObject(mojo, "generatedTestResourcesDir", generatedResources.toFile());
		MojoExtension.setVariableValueToObject(mojo, "incrementalContractTests", false);

		// when
		mojo.execute();

		// then
		then(Files.exists(generatedSources)).isTrue();
		then(Files.exists(generatedResources)).isTrue();
		List<Path> generatedJava;
		try (Stream<Path> paths = Files.walk(generatedSources)) {
			generatedJava = paths.filter(path -> path.toString().endsWith(".java")).toList();
		}
		then(generatedJava).isNotEmpty();
		String generatedContent = Files.readString(generatedJava.get(0));
		then(generatedContent).contains("/users");
		MavenProject project = (MavenProject) MojoExtension.getVariableValueFromObject(mojo, "project");
		then(project.getTestCompileSourceRoots()).contains(generatedSources.toString());
	}

}
