/**
 *
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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;

import static io.takari.maven.testing.TestMavenRuntime.newParameter;
import static io.takari.maven.testing.TestResources.assertFilesNotPresent;
import static io.takari.maven.testing.TestResources.assertFilesPresent;
import static org.assertj.core.api.BDDAssertions.then;

public class PluginUnitTest {

	@Rule
	public final TestResources resources = new TestResources();

	@Rule
	public final TestMavenRuntime maven = new TestMavenRuntime();

	@Test
	public void shouldGenerateWireMockStubsInDefaultLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert");
		assertFilesPresent(basedir, "target/stubs/mappings/Sample.json");
		assertFilesNotPresent(basedir, "target/stubs/mappings/Messaging.json");
	}

	@Test
	public void shouldGenerateWireMockFromStubsDirectory() throws Exception {
		File basedir = this.resources.getBasedir("withStubs");
		this.maven.executeMojo(basedir, "convert", newParameter("contractsDirectory", "src/test/resources/stubs"));
		assertFilesPresent(basedir, "target/stubs/mappings/Sample.json");
	}

	@Test
	public void shouldCopyContracts() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert");
		assertFilesPresent(basedir, "target/stubs/contracts/Sample.groovy");
		assertFilesPresent(basedir, "target/stubs/contracts/Messaging.groovy");
	}

	@Test
	public void shouldGenerateWireMockStubsInSelectedLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert", newParameter("outputDirectory", "target/foo"));
		assertFilesPresent(basedir, "target/foo/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateContractSpecificationInDefaultLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "SPOCK"));
		assertFilesPresent(basedir,
				"target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierSpec.groovy");
	}

	@Test
	public void shouldGenerateContractTestsInDefaultLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "generateTests");
		assertFilesPresent(basedir,
				"target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
	}

	@Test
	public void shouldGenerateContractTestsWithCustomImports() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "generateTests", newParameter("imports", ""));
		assertFilesPresent(basedir,
				"target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
	}

	@Test
	public void shouldGenerateContractTestsWithoutArraySize() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "generateTests");
		assertFilesPresent(basedir,
				"target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
		File test = new File(basedir, "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
		then(FileUtils.readFileToString(test)).doesNotContain("hasSize(4)");
	}

	@Test
	public void shouldGenerateContractTestsWithArraySize() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "generateTests", newParameter("assertJsonSize", "true"));
		assertFilesPresent(basedir,
				"target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
		File test = new File(basedir, "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/ContractVerifierTest.java");
		then(FileUtils.readFileToString(test)).contains("hasSize(4)");
	}

	@Test
	public void shouldGenerateStubs() throws Exception {
		File basedir = this.resources.getBasedir("generatedStubs");
		this.maven.executeMojo(basedir, "generateStubs");
		assertFilesPresent(basedir, "target/sample-project-0.1-stubs.jar");
	}

	@Test
	public void shouldGenerateStubsWithMappingsOnly() throws Exception {
		File basedir = this.resources.getBasedir("generatedStubs");
		this.maven.executeMojo(basedir, "generateStubs", newParameter("attachContracts", "false"));
		assertFilesPresent(basedir, "target/sample-project-0.1-stubs.jar");
		// FIXME: add assertion for jar content
	}

	@Test
	public void shouldGenerateStubsWithCustomClassifier() throws Exception {
		File basedir = this.resources.getBasedir("generatedStubs");
		this.maven.executeMojo(basedir, "generateStubs", newParameter("classifier", "foo"));
		assertFilesPresent(basedir, "target/sample-project-0.1-foo.jar");
	}


}
