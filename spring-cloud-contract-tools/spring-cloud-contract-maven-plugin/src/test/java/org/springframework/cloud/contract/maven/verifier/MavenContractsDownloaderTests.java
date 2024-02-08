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

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.cloud.contract.stubrunner.ContractDownloader;
import org.springframework.cloud.contract.stubrunner.InclusionPropertiesAccessor;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;

class MavenContractsDownloaderTests {

	@TempDir
	File defaultFolder;

	@TempDir
	File fileForDependencyOne;

	@TempDir
	File fileForDependencyTwo;

	@Test
	void shouldNotReadFolderFromCacheWhenExecutedTwiceFor2DifferentArtifactsInTheSameProject() {
		MavenProject mavenProject = new MavenProject();

		Dependency one = dependency(1);
		MavenContractsDownloader mavenContractsDownloader = contractsDownloader(mavenProject, one,
				this.fileForDependencyOne);
		File dependencyOneFile = mavenContractsDownloader
				.downloadAndUnpackContractsIfRequired(new ContractVerifierConfigProperties(), this.defaultFolder);
		BDDAssertions.then(dependencyOneFile).as("Location for dependency 1 should be computed since it's not cached")
				.isEqualTo(this.fileForDependencyOne);

		mavenContractsDownloader = contractsDownloader(mavenProject, one, this.fileForDependencyOne);
		File fileForDependencyOneAgain = mavenContractsDownloader
				.downloadAndUnpackContractsIfRequired(new ContractVerifierConfigProperties(), this.defaultFolder);
		BDDAssertions.then(dependencyOneFile).as("Location for dependency 1 should be taken from cache")
				.isEqualTo(fileForDependencyOneAgain);

		Dependency two = dependency(2);
		mavenContractsDownloader = contractsDownloader(mavenProject, two, this.fileForDependencyTwo);
		File dependencyTwoFile = mavenContractsDownloader
				.downloadAndUnpackContractsIfRequired(new ContractVerifierConfigProperties(), this.defaultFolder);

		BDDAssertions.then(dependencyTwoFile).as("Location for dependency 2 should be computed again")
				.isNotEqualTo(dependencyOneFile).isEqualTo(this.fileForDependencyTwo);
	}

	private MavenContractsDownloader contractsDownloader(MavenProject mavenProject, Dependency one, File file) {
		return new MavenContractsDownloader(mavenProject, one, "", "", StubRunnerProperties.StubsMode.LOCAL,
				new SilentLog(), "", "", "", null, false, new HashMap<>(), false) {
			@Override
			ContractDownloader contractDownloader() {
				return new ContractDownloader(null, null, null, null, null, null) {

					int counterForDependencyOne;

					@Override
					public File unpackAndDownloadContracts() {
						if (file == fileForDependencyOne) {
							this.counterForDependencyOne = this.counterForDependencyOne + 1;
						}
						if (file == fileForDependencyOne && this.counterForDependencyOne == 2) {
							throw new AssertionError("Second call for dependency 1 should come from cache");
						}
						return file;
					}

					@Override
					public InclusionProperties createNewInclusionProperties(File contractsDirectory) {
						return new InclusionPropertiesAccessor("a", "b");
					}
				};
			}
		};
	}

	private Dependency dependency(int number) {
		Dependency dependency = new Dependency();
		dependency.setGroupId("a" + number);
		dependency.setArtifactId("b" + number);
		dependency.setVersion("c" + number);
		dependency.setClassifier("d" + number);
		return dependency;
	}

}
