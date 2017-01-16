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

package org.springframework.cloud.contract.stubrunner.junit;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class StubRunnerRuleCustomPortJUnitTest {

	@BeforeClass
	@AfterClass
	public static void setupProps() {
			System.clearProperty("stubrunner.repository.root");
			System.clearProperty("stubrunner.classifier");
	}

	// tag::classrule_with_port[]
	@ClassRule public static StubRunnerRule rule = new StubRunnerRule()
			.repoRoot(repoRoot())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")
			.withPort(12345)
			.downloadStub("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer:12346");
	// end::classrule_with_port[]

	@Test
	public void should_start_wiremock_servers() throws Exception {
		// expect: 'WireMocks are running'
			then(rule.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isEqualTo(rule.findStubUrl("org.springframework.cloud.contract.verifier.stubs", "loanIssuance"));
			then(rule.findStubUrl("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")).isNotNull();
		// and:
			then(rule.findAllRunningStubs().isPresent("loanIssuance")).isTrue();
			then(rule.findAllRunningStubs().isPresent("org.springframework.cloud.contract.verifier.stubs", "fraudDetectionServer")).isTrue();
			then(rule.findAllRunningStubs().isPresent("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")).isTrue();
		// and: 'Stubs were registered'
			then(httpGet(rule.findStubUrl("loanIssuance").toString() + "/name")).isEqualTo("loanIssuance");
			then(httpGet(rule.findStubUrl("fraudDetectionServer").toString() + "/name")).isEqualTo("fraudDetectionServer");
		// and: 'The port is fixed'
		// tag::test_with_port[]
			then(rule.findStubUrl("loanIssuance")).isEqualTo(URI.create("http://localhost:12345").toURL());
			then(rule.findStubUrl("fraudDetectionServer")).isEqualTo(URI.create("http://localhost:12346").toURL());
		// end::test_with_port[]
	}

	private static String repoRoot()  {
		try {
			return StubRunnerRuleCustomPortJUnitTest.class.getResource("/m2repo/repository/").toURI().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private String httpGet(String url) throws Exception {
		try(InputStream stream = URI.create(url).toURL().openStream()) {
			return StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
		}
	}
}
