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

import static io.takari.maven.testing.TestMavenRuntime.newParameter;
import static io.takari.maven.testing.TestResources.assertFilesNotPresent;
import static io.takari.maven.testing.TestResources.assertFilesPresent;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;

public class PluginUnitTest {

	@Rule
	public final TestResources resources = new TestResources();

	@Rule
	public final TestMavenRuntime maven = new TestMavenRuntime();

	@Test
	public void shouldGenerateWireMockStubsInDefaultLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert");
		assertFilesPresent(basedir, "target/stubs/mappings/Sample.json".replace("/", File.separator));
		assertFilesNotPresent(basedir, "target/stubs/mappings/Messaging.json".replace("/", File.separator));
	}

	@Test
	public void shouldGenerateWireMockFromStubsDirectory() throws Exception {
		File basedir = this.resources.getBasedir("withStubs");
		this.maven.executeMojo(basedir, "convert", newParameter("contractsDirectory", "src/test/resources/stubs"));
		assertFilesPresent(basedir, "target/stubs/mappings/Sample.json".replace("/", File.separator));
	}

	@Test
	public void shouldCopyContracts() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert");
		assertFilesPresent(basedir, "target/stubs/contracts/Sample.groovy".replace("/", File.separator));
		assertFilesPresent(basedir, "target/stubs/contracts/Messaging.groovy".replace("/", File.separator));
	}

	@Test
	public void shouldGenerateWireMockStubsInSelectedLocation() throws Exception {
		File basedir = this.resources.getBasedir("basic");
		this.maven.executeMojo(basedir, "convert", newParameter("stubsDirectory", "target/foo"));
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

	@Test
	public void shouldGenerateStubsByDownloadingContractsFromARepo() throws Exception {
		File basedir = this.resources.getBasedir("basic-remote-contracts");
		this.maven.executeMojo(basedir, "convert", newParameter("contractsRepositoryUrl", "file://" + PluginUnitTest.class.getClassLoader().getResource("m2repo/repository").getFile().replace("/", File.separator)));
		assertFilesPresent(basedir, "target/stubs/mappings/com/example/server/client1/contracts/shouldMarkClientAsFraud.json");
	}

	@Test
	public void shouldGenerateStubsByDownloadingContractsFromARepoWhenCustomPathIsProvided() throws Exception {
		File basedir = this.resources.getBasedir("complex-remote-contracts");
		this.maven.executeMojo(basedir, "convert", newParameter("contractsRepositoryUrl", "file://" + PluginUnitTest.class.getClassLoader().getResource("m2repo/repository").getFile().replace("/", File.separator)));
		assertFilesPresent(basedir, "target/stubs/mappings/com/example/server/client1/contracts/shouldMarkClientAsFraud.json");
		assertFilesNotPresent(basedir, "target/stubs/mappings/com/foo/bar/baz/shouldBeIgnoredByPlugin.json");
		assertFilesNotPresent(basedir, "target/stubs/contracts/com/foo/bar/baz/shouldBeIgnoredByPlugin.groovy");
	}

	@Test
	public void shouldGenerateTestsByDownloadingContractsFromARepo() throws Exception {
		File basedir = this.resources.getBasedir("basic-remote-contracts");
		this.maven.executeMojo(basedir, "generateTests", newParameter("contractsRepositoryUrl", "file://" + PluginUnitTest.class.getClassLoader().getResource("m2repo/repository").getFile().replace("/", File.separator)));
		assertFilesPresent(basedir, "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/example/server/client1/ContractsTest.java");
	}

	@Test
	public void shouldGenerateTestsByDownloadingContractsFromARepoWhenCustomPathIsProvided() throws Exception {
		File basedir = this.resources.getBasedir("complex-remote-contracts");
		this.maven.executeMojo(basedir, "generateTests", newParameter("contractsRepositoryUrl", "file://" + PluginUnitTest.class.getClassLoader().getResource("m2repo/repository").getFile().replace("/", File.separator)));
		assertFilesPresent(basedir, "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/example/server/client1/ContractsTest.java");
		assertFilesNotPresent(basedir, "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/foo/bar/BazTest.java");
		assertFilesNotPresent(basedir, "target/stubs/contracts/com/foo/bar/baz/shouldBeIgnoredByPlugin.groovy");
	}

	@Test
	public void shouldGenerateContractTestsWithBaseClassResolvedFromConvention() throws Exception {
		File basedir = this.resources.getBasedir("basic-generated-baseclass");

		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "JUNIT"));

		String path = "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/hello/V1Test.java";
		assertFilesPresent(basedir, path);
		File test = new File(basedir, path);
		then(FileUtils.readFileToString(test)).contains("extends HelloV1Base").contains("import hello.HelloV1Base");
	}

	@Test
	public void shouldGenerateContractTestsWithBaseClassResolvedFromConventionForSpock() throws Exception {
		File basedir = this.resources.getBasedir("basic-generated-baseclass");

		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "SPOCK"));

		String path = "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/hello/V1Spec.groovy";
		assertFilesPresent(basedir, path);
		File test = new File(basedir, path);
		then(FileUtils.readFileToString(test)).contains("extends HelloV1Base").contains("import hello.HelloV1Base");
	}

	@Test
	public void shouldGenerateContractTestsWithBaseClassResolvedFromMapping() throws Exception {
		File basedir = this.resources.getBasedir("basic-baseclass-from-mappings");

		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "JUNIT"));

		String path = "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/hello/V1Test.java";
		assertFilesPresent(basedir, path);
		File test = new File(basedir, path);
		then(FileUtils.readFileToString(test)).contains("extends TestBase").contains("import com.example.TestBase");
	}

	@Test
	public void shouldGenerateContractTestsWithBaseClassResolvedFromMappingNameForSpock() throws Exception {
		File basedir = this.resources.getBasedir("basic-baseclass-from-mappings");

		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "SPOCK"));

		String path = "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/hello/V1Spec.groovy";
		assertFilesPresent(basedir, path);
		File test = new File(basedir, path);
		then(FileUtils.readFileToString(test)).contains("extends TestBase").contains("import com.example.TestBase");
	}

	@Test
	public void shouldGenerateContractTestsWithAFileContainingAListOfContracts() throws Exception {
		File basedir = this.resources.getBasedir("multiple-contracts");

		this.maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "JUNIT"));

		String path = "target/generated-test-sources/contracts/org/springframework/cloud/contract/verifier/tests/com/hello/V1Test.java";
		assertFilesPresent(basedir, path);
		File test = new File(basedir, path);
		then(FileUtils.readFileToString(test))
				.contains("public void validate_should_post_a_user() throws Exception {")
				.contains("public void validate_withList_1() throws Exception {");
	}

	@Test
	public void shouldGenerateStubsWithAFileContainingAListOfContracts() throws Exception {
		File basedir = this.resources.getBasedir("multiple-contracts");

		this.maven.executeMojo(basedir, "convert", newParameter("stubsDirectory", "target/foo"));

		String firstFile = "target/foo/mappings/com/hello/v1/should post a user.json";
		File test = new File(basedir, firstFile);
		assertFilesPresent(basedir, "target/foo/mappings/com/hello/v1/1_WithList.json");
		then(FileUtils.readFileToString(test)).contains("/users/1");
		String secondFile = "target/foo/mappings/com/hello/v1/1_WithList.json";
		File test2 = new File(basedir, secondFile);
		assertFilesPresent(basedir, "target/foo/mappings/com/hello/v1/should post a user.json");
		then(FileUtils.readFileToString(test2)).contains("/users/2");
	}

	@Test
	public void shouldGenerateStubsForCommonRepoWithTargetFolder() throws Exception {
		File basedir = this.resources.getBasedir("common-repo");

		this.maven.executeMojo(basedir, "convert");

		assertFilesNotPresent(basedir, "target/generated-test-sources/contracts/");
		// there will be no stubs cause all files are copied to `target` folder
		assertFilesNotPresent(basedir, "target/stubs/mappings/");
		assertFilesPresent(basedir, "target/stubs/contracts/consumer1/Messaging.groovy");
		assertFilesPresent(basedir, "target/stubs/contracts/pom.xml");
	}
}
