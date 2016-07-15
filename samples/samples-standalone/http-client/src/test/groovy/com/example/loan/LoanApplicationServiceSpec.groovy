package com.example.loan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

import com.example.loan.model.Client
import com.example.loan.model.LoanApplication
import com.example.loan.model.LoanApplicationResult
import com.example.loan.model.LoanApplicationStatus

@ContextConfiguration(loader = SpringBootContextLoader, classes = Application)
@AutoConfigureStubRunner
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

	// tag::client_tdd[]
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
	// end::client_tdd[]

}
