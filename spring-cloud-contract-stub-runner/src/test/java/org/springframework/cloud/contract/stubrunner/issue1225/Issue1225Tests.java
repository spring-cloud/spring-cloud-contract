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

package org.springframework.cloud.contract.stubrunner.issue1225;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that stub runner specific auto-configuration can be loaded up in combination with
 * other slice tests
 *
 * @author Biju Kunjummen
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"ping.url=http://localhost:${stubrunner.runningstubs.loanIssuance.port}" })
@AutoConfigureStubRunner(ids = {
		"org.springframework.cloud.contract.verifier.stubs:loanIssuance:+:stubs",
		"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer:+:stubs" })
@ActiveProfiles("test")
public class Issue1225Tests {

	@StubRunnerPort("loanIssuance")
	private int stubRunnerLoanIssuancePort;

	@Autowired
	private PingProxyController pingProxyController;

	@Test
	public void shouldInjectTheStubPortsAsEarlyAsPossible() {
		assertThat(this.stubRunnerLoanIssuancePort).isPositive();
		assertThat(this.pingProxyController.pingUrl)
				.contains(":" + this.stubRunnerLoanIssuancePort);
	}

	@ComponentScan
	@SpringBootConfiguration
	static class Config {

	}

}

@RestController
class PingProxyController {

	String pingUrl;

	PingProxyController(@Value("${ping.url}") String pingUrl) {
		this.pingUrl = pingUrl;
	}

}
