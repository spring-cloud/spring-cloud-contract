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

package com.example.loan;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// tag::autoconfigure_stubrunner[]
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(repositoryRoot = "classpath:m2repo/repository/", ids = {
		"org.springframework.cloud.contract.verifier.stubs:contextPathFraudDetectionServer" },
		stubsMode = StubRunnerProperties.StubsMode.REMOTE)
public class LoanApplicationServiceTests {

	// end::autoconfigure_stubrunner[]

	@LocalServerPort
	Integer port;

	@Autowired
	private LoanApplicationService service;

	@Autowired
	private StubFinder stubFinder;

	@Before
	public void setPort() {
		this.service.setFraudUrl(
				this.stubFinder.findStubUrl("contextPathFraudDetectionServer").toString()
						+ "/fraud-path/");
	}

	@Test
	public void shouldStartThisAppWithContextPath() {
		String response = new RestTemplate().getForObject(
				"http://localhost:" + this.port + "/my-path/foo", String.class);

		assertThat(response).isNotEmpty();
	}

	@Test
	public void shouldSuccessfullyApplyForLoan() {
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

	// tag::client_tdd[]
	@Test
	public void shouldBeRejectedDueToAbnormalLoanAmount() {
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
	// end::client_tdd[]

}
