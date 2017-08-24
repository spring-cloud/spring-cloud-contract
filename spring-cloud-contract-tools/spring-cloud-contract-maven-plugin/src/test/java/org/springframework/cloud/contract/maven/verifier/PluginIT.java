/**
 *
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

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestProperties;
import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({ "3.3.3" })
public class PluginIT {

	@Rule
	public final TestResources resources = new TestResources();

	private final TestProperties properties = new TestProperties();

	private final MavenRuntime maven;

	@Before
	public void doNotRunForWindows() {
		Assume.assumeFalse(isWindows());
	}

	private boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public PluginIT(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
		this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
	}

	@Test
	public void should_build_project_Spring_Boot_Groovy_with_Accurest() throws Exception {
		File basedir = this.resources.getBasedir("spring-boot-groovy");
		this.maven.forProject(basedir)
				.execute("package")
				.assertErrorFreeLog()
				.assertLogText("Generating server tests source code for Spring Cloud Contract Verifier contract verification")
				.assertLogText("Generated 1 test classes.")
				.assertLogText("Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings")
				.assertLogText("Creating new stub")
				.assertLogText("Running hello.ContractVerifierSpec")
				.assertErrorFreeLog();
	}

	@Test
	public void should_build_project_Spring_Boot_Java_with_Accurest() throws Exception {
		File basedir = this.resources.getBasedir("spring-boot-java");
		this.maven.forProject(basedir)
				.execute("package")
				.assertErrorFreeLog()
				.assertLogText("Generating server tests source code for Spring Cloud Contract Verifier contract verification")
				.assertLogText("Generated 1 test classes.")
				.assertLogText("Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings")
				.assertLogText("Creating new stub")
				.assertLogText("Running hello.ContractVerifierTest")
				.assertErrorFreeLog();
	}

	@Test
	public void should_build_project_with_plugin_extension() throws Exception {
		File basedir = this.resources.getBasedir("plugin-extension");
		this.maven.forProject(basedir)
				.execute("package")
				.assertErrorFreeLog()
				.assertLogText("Generating server tests source code for Spring Cloud Contract Verifier contract verification")
				.assertLogText("Generated 1 test classes.")
				.assertLogText("Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings")
				.assertLogText("Creating new stub")
				.assertLogText("Running hello.ContractVerifierTest")
				.assertErrorFreeLog();
	}

	@Test
	public void should_convert_Accurest_Contracts_to_WireMock_Stubs_mappings() throws Exception {
		File basedir = this.resources.getBasedir("pomless");
		this.properties.getPluginVersion();
		this.maven.forProject(basedir)
				.withCliOption("-X")
				.execute(String.format("org.springframework.cloud:spring-cloud-contract-maven-plugin:%s:convert",
						this.properties.getPluginVersion()))
				.assertLogText("Converting from Spring Cloud Contract Verifier contracts to WireMock stubs mappings")
				.assertLogText("Creating new stub")
				.assertErrorFreeLog();
	}
}