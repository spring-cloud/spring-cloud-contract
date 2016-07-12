package org.springframework.cloud.contract

import org.springframework.cloud.contract.frauddetection.Application
import org.springframework.cloud.contract.frauddetection.LoanApplicationService
import org.springframework.cloud.contract.frauddetection.model.Client
import org.springframework.cloud.contract.frauddetection.model.LoanApplication
import org.springframework.cloud.contract.frauddetection.model.LoanApplicationResult
import org.springframework.cloud.contract.frauddetection.model.LoanApplicationStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(loader = SpringApplicationContextLoader, classes = Application)
class LoanApplicationServiceSpec extends Specification {

	@Autowired
	LoanApplicationService sut

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
