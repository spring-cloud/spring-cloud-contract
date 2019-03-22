/*
 * Copyright 2013-2019 the original author or authors.
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

package com.example.loan;

import java.nio.charset.Charset;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "service.port=${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
public class LoanApplicationServiceTests {

	@Autowired
	private LoanApplicationService service;

	@Value("classpath:META-INF/com.example/http-server-webclient/0.0.1-SNAPSHOT/mappings/markClientAsFraud.json")
	private Resource markClientAsFraud;

	@Value("classpath:META-INF/com.example/http-server-webclient/0.0.1-SNAPSHOT/mappings/markClientAsNotFraud.json")
	private Resource markClientAsNotFraud;

	@Autowired
	private WireMockServer server;

	@Test
	public void shouldSuccessfullyApplyForLoan() throws Exception {
		this.server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				this.markClientAsNotFraud.getInputStream(), Charset.forName("UTF-8"))));
		// given:
		LoanApplication application = new LoanApplication(new Client("1234567890"),
				123.123);
		// when:
		LoanApplicationResult loanApplication = this.service.loanApplication(application);
		// then:
		assertThat(loanApplication.getLoanApplicationStatus())
				.isEqualTo(LoanApplicationStatus.LOAN_APPLIED);
		assertThat(loanApplication.getRejectionReason()).isNull();
	}

	@Test
	public void shouldBeRejectedDueToAbnormalLoanAmount() throws Exception {
		this.server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				this.markClientAsFraud.getInputStream(), Charset.forName("UTF-8"))));
		// given:
		LoanApplication application = new LoanApplication(new Client("1234567890"),
				99999);
		// when:
		LoanApplicationResult loanApplication = this.service.loanApplication(application);
		// then:
		assertThat(loanApplication.getLoanApplicationStatus())
				.isEqualTo(LoanApplicationStatus.LOAN_APPLICATION_REJECTED);
		assertThat(loanApplication.getRejectionReason()).isEqualTo("Amount too high");
	}

}
