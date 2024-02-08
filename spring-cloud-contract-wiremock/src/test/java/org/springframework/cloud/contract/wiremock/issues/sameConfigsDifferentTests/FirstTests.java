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

package org.springframework.cloud.contract.wiremock.issues.sameConfigsDifferentTests;

import java.nio.charset.Charset;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "service.port=${wiremock.server.port}", classes = FirstTests.Config.class)
@AutoConfigureWireMock(port = 0)
public class FirstTests {

	@Value("classpath:example-mappings/shouldMarkClientAsFraud.json")
	private Resource markClientAsFraud;

	@Autowired
	private WireMockServer server;

	@Test
	public void shouldBeRejectedDueToAbnormalLoanAmount() throws Exception {
		server.addStubMapping(StubMapping
				.buildFrom(StreamUtils.copyToString(markClientAsFraud.getInputStream(), Charset.forName("UTF-8"))));
		// given:
		LoanApplication loanApplication = new LoanApplication(new Client("1234567890"), 99999);

		// when:
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/vnd.fraud.v1+json");

		ResponseEntity<FraudServiceResponse> response = new RestTemplate().exchange(
				"http://localhost:" + server.port() + "/fraudcheck", HttpMethod.PUT,
				new HttpEntity<>(new FraudServiceRequest(loanApplication), httpHeaders), FraudServiceResponse.class);
		// then:
		assertThat(response.getBody().getFraudCheckStatus()).isEqualTo(FraudCheckStatus.FRAUD);
		assertThat(response.getBody().getRejectionReason()).isEqualTo("Amount too high");
	}

	@EnableAutoConfiguration
	@Configuration
	static class Config {

	}

}
