package com.example.loan;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;

import static org.assertj.core.api.Assertions.assertThat;

// tag::autoconfigure_stubrunner[]
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(repositoryRoot = "classpath:m2repo/repository/",
		ids = { "org.springframework.cloud.contract.verifier.stubs:contextPathFraudDetectionServer" },
		stubsMode = StubRunnerProperties.StubsMode.REMOTE)
@DirtiesContext
public class LoanApplicationServiceTests {
// end::autoconfigure_stubrunner[]

	@Autowired private LoanApplicationService service;
	@Autowired private StubFinder stubFinder;
	@LocalServerPort Integer port;

	@Before
	public void setPort() {
		this.service.setFraudUrl(this.stubFinder.findStubUrl("contextPathFraudDetectionServer").toString() + "/fraud-path/");
	}

	@Test
	public void shouldStartThisAppWithContextPath() {
		String response = new RestTemplate()
				.getForObject("http://localhost:" + this.port + "/my-path/foo", String.class);

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
