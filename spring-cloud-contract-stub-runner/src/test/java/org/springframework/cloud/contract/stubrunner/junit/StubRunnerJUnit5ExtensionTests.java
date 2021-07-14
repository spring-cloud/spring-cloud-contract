/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.junit;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.cloud.contract.stubrunner.junit4.StubRunnerRuleJUnitTest;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class StubRunnerJUnit5ExtensionTests {

	// tag::extension[]
	// Visible for Junit
	@RegisterExtension
	static StubRunnerExtension stubRunnerExtension = new StubRunnerExtension().repoRoot(repoRoot())
			.stubsMode(StubRunnerProperties.StubsMode.REMOTE)
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")
			.downloadStub("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")
			.withMappingsOutputFolder("target/outputmappingsforrule");

	@BeforeAll
	@AfterAll
	static void setupProps() {
		System.clearProperty("stubrunner.repository.root");
		System.clearProperty("stubrunner.classifier");
	}

	private static String repoRoot() {
		try {
			return StubRunnerRuleJUnitTest.class.getResource("/m2repo/repository/").toURI().toString();
		}
		catch (Exception e) {
			return "";
		}
	}
	// end::extension[]

	@Test
	void should_start_WireMock_servers() {
		assertThat(stubRunnerExtension.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance"))
				.isNotNull();
		assertThat(stubRunnerExtension.findStubUrl("loanIssuance")).isNotNull();
		assertThat(stubRunnerExtension.findStubUrl("loanIssuance")).isEqualTo(
				stubRunnerExtension.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance"));
		assertThat(stubRunnerExtension
				.findStubUrl("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")).isNotNull();
	}

	@Test
	void should_output_mappings_to_output_folder() {
		// when
		URL url = stubRunnerExtension.findStubUrl("fraudDetectionServer");

		// then
		assertThat(new File("target/outputmappingsforrule", "fraudDetectionServer_" + url.getPort())).exists();
	}

}
