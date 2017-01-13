package com.example.loan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = {"com.example:pact-http-server:+:stubs"}, workOffline = true)
@DirtiesContext
public class LoanApplicationServiceTests {

	@Autowired
	private LoanApplicationService service;

	@Autowired
	private Environment environment;

	@Before
	public void setup() {
		this.service.setPort(this.environment.getProperty("stubrunner.runningstubs.pact-http-server.port", Integer.class));
	}

	@Test
	public void shouldSuccessfullyApplyForLoan() {
		// given:
		LoanApplication application = new LoanApplication(new Client("1234567890"),
				123.123);
		// when:
		LoanApplicationResult loanApplication = service.loanApplication(application);
		// then:
		assertThat(loanApplication.getLoanApplicationStatus())
				.isEqualTo(LoanApplicationStatus.LOAN_APPLIED);
		assertThat(loanApplication.getRejectionReason()).isNull();
	}

	@Test
	public void shouldBeRejectedDueToAbnormalLoanAmount() {
		// given:
		LoanApplication application = new LoanApplication(new Client("1234567890"),
				99999);
		// when:
		LoanApplicationResult loanApplication = service.loanApplication(application);
		// then:
		assertThat(loanApplication.getLoanApplicationStatus())
				.isEqualTo(LoanApplicationStatus.LOAN_APPLICATION_REJECTED);
		assertThat(loanApplication.getRejectionReason()).isEqualTo("Amount too high");
	}

	@Test
	public void shouldSuccessfullyGetAllFrauds() {
		// when:
		int count = service.countAllFrauds();
		// then:
		assertThat(count).isEqualTo(200);
	}

	@Test
	public void shouldSuccessfullyGetAllDrunks() {
		// when:
		int count = service.countDrunks();
		// then:
		assertThat(count).isEqualTo(100);
	}

}
