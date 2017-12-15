/*
 *  Copyright 2017 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Biju Kunjummen
 */

@RunWith(SpringRunner.class)
@StubRunnerTest(mappingsOutputFolder = "target/outputmappings/")
@DirtiesContext
@ActiveProfiles("test")
public class StubRunnerSliceTest {

	@Autowired
	private StubFinder stubFinder;

	@Autowired
	private Environment environment;

	@Value("${stubrunner.runningstubs.fraudDetectionServer.port}")
	private Integer foo;

	@Test
	public void testStubRunnerLoad() {
		assertThat(stubFinder.findStubUrl(
				"org.springframework.cloud.contract.verifier.stubs", "loanIssuance"))
						.isNotNull();
		assertThat(stubFinder.findStubUrl("loanIssuance")).isNotNull();
		assertThat(stubFinder.findStubUrl("loanIssuance")).isEqualTo(stubFinder
				.findStubUrl("org.springframework.cloud.contract.verifier.stubs",
						"loanIssuance"));
		assertThat(stubFinder.findStubUrl("loanIssuance"))
				.isEqualTo(stubFinder.findStubUrl(
						"org.springframework.cloud.contract.verifier.stubs:loanIssuance"));
		assertThat(stubFinder.findStubUrl(
				"org.springframework.cloud.contract.verifier.stubs:loanIssuance:0.0.1-SNAPSHOT"))
						.isEqualTo(stubFinder.findStubUrl(
								"org.springframework.cloud.contract.verifier.stubs:loanIssuance:0.0.1-SNAPSHOT:stubs"));
		assertThat(stubFinder.findStubUrl(
				"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer"))
						.isNotNull();
	}

	@Configuration
	static class Config {
	}

}
