package com.example.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@RunWith(SpringRunner.class)
@SpringBootTest(properties="service.port=${wiremock.server.port}")
@AutoConfigureWireMock(port=0)
public class LoanApplicationServiceTests {

	@Autowired
	private LoanApplicationService service;

	@Value("classpath:META-INF/com.example/http-server-restdocs/0.0.1.M1/mappings/markClientAsFraud.json")
	private Resource markClientAsFraud;

	@Value("classpath:META-INF/com.example/http-server-restdocs/0.0.1.M1/mappings/markClientAsNotFraud.json")
	private Resource markClientAsNotFraud;

	@Autowired
	private WireMockServer server;

	@Test
	public void shouldSuccessfullyApplyForLoan() throws Exception {
		server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				markClientAsNotFraud.getInputStream(), Charset.forName("UTF-8"))));
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
	public void shouldBeRejectedDueToAbnormalLoanAmount() throws Exception {
		server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				markClientAsFraud.getInputStream(), Charset.forName("UTF-8"))));
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

}
