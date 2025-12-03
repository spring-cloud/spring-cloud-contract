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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ConvertMojoIntegrationTests extends AbstractMojoIntegrationTests {

	@Test
	@InjectMojo(goal = "convert", pom = "pom.xml")
	@Basedir("src/test/projects/basic")
	void shouldConvertContractsToWiremockStubs(ConvertMojo mojo) throws Exception {
		// given
		Path projectDir = Path.of(MojoExtension.getBasedir());
		Path targetDir = projectDir.resolve("target");
		deleteDirectoryIfExists(targetDir);
		Files.createDirectories(targetDir);
		setupBuildPaths(mojo, targetDir);
		MojoExtension.setVariableValueToObject(mojo, "stubsDirectory", targetDir.resolve("stubs").toFile());
		MojoExtension.setVariableValueToObject(mojo, "incrementalContractStubs", false);

		// when
		mojo.execute();

		// then
		Path stubsRoot = targetDir.resolve("stubs");
		then(Files.exists(stubsRoot)).isTrue();
		List<Path> stubs;
		try (Stream<Path> paths = Files.walk(stubsRoot)) {
			stubs = paths.filter(path -> path.toString().endsWith(".json")).toList();
		}
		then(stubs).isNotEmpty();
		String stubContents = Files.readString(stubs.get(0));
		then(stubContents).contains("/users").contains("\"response\"");
	}

}
