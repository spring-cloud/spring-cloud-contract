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

package com.example.loan

import com.example.loan.model.Client
import com.example.loan.model.LoanApplication
import com.example.loan.model.LoanApplicationStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = ["server.context-path=/app"])
@AutoConfigureStubRunner(ids = ["com.example:http-server-kotlin:+:stubs:6565"],
		stubsMode = StubRunnerProperties.StubsMode.LOCAL)
class LoanApplicationServiceContextPathTests {

	@Autowired
	lateinit var service: LoanApplicationService

	@Test
	fun shouldSuccessfullyApplyForLoan() {
		// given:
		val application = LoanApplication(Client("1234567890"), 123.123)
		// when:
		val loanApplication = service.loanApplication(application)
		// then:
		assertThat(loanApplication?.loanApplicationStatus)
				.isEqualTo(LoanApplicationStatus.LOAN_APPLIED)
		assertThat(loanApplication?.rejectionReason).isNull()
	}

	@Test
	fun shouldBeRejectedDueToAbnormalLoanAmount() {
		// given:
		val application = LoanApplication(Client("1234567890"), 99999.0)
		// when:
		val loanApplication = service.loanApplication(application)
		// then:
		assertThat(loanApplication?.loanApplicationStatus)
				.isEqualTo(LoanApplicationStatus.LOAN_APPLICATION_REJECTED)
		assertThat(loanApplication?.rejectionReason).isEqualTo("Amount too high")
	}

}
