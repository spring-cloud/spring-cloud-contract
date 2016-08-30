/*
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

package org.springframework.cloud

import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import org.junit.ClassRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.frauddetection.Application
import org.springframework.cloud.frauddetection.LoanApplicationService
import org.springframework.cloud.frauddetection.model.Client
import org.springframework.cloud.frauddetection.model.LoanApplication
import org.springframework.cloud.frauddetection.model.LoanApplicationResult
import org.springframework.cloud.frauddetection.model.LoanApplicationStatus
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification

@ContextConfiguration(loader = SpringBootContextLoader, classes = Application)
class LoanApplicationServiceSpec extends Specification {

	public static int port = org.springframework.util.SocketUtils.findAvailableTcpPort()

	@ClassRule
	@Shared
	WireMockClassRule wireMockRule = new WireMockClassRule(port)

	@Autowired
	LoanApplicationService sut

	def setup() {
		sut.port = port
	}

	def 'should successfully apply for loan'() {
		given:
			LoanApplication application =
					new LoanApplication(client: new Client(pesel: '1234567890'), amount: 123.123)
		when:
			LoanApplicationResult loanApplication = sut.loanApplication(application)
		then:
			loanApplication.loanApplicationStatus == LoanApplicationStatus.LOAN_APPLIED
			loanApplication.rejectionReason == null
	}

	def 'should be rejected due to abnormal loan amount'() {
		given:
			LoanApplication application =
					new LoanApplication(client: new Client(pesel: '1234567890'), amount: 99_999)
		when:
			LoanApplicationResult loanApplication = sut.loanApplication(application)
		then:
			loanApplication.loanApplicationStatus == LoanApplicationStatus.LOAN_APPLICATION_REJECTED
			loanApplication.rejectionReason == 'Amount too high'
	}


}
