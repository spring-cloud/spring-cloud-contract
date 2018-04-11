package com.example.loan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// tag::autoconfigure_stubrunner[]
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = {"com.example:http-server-dsl:+:stubs:6565"},
		stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@DirtiesContext
public class LoanApplicationServiceTests {
// end::autoconfigure_stubrunner[]

	@Autowired
	private LoanApplicationService service;

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

	// tag::client_tdd[]
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
	// end::client_tdd[]

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

	@Test
	public void shouldSuccessfullyGetCookies() {
		// when:
		String cookies = service.getCookies();
		// then:
		assertThat(cookies).isEqualTo("foo bar");
	}

	@Test
	public void shouldSuccessfullyWorkWithMultipart() {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.add("file1", new ByteArrayResource(("content").getBytes()) {
			@Override
			public String getFilename() {
				return "file1";
			}
		});
		parameters.add("file2", new ByteArrayResource(("content").getBytes()) {
			@Override
			public String getFilename() {
				return "file2";
			}
		});
		parameters.add("test", new ByteArrayResource(("{\n  \"status\": \"test\"\n}").getBytes()) {
			@Override
			public String getFilename() {
				return "test";
			}
		});
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "multipart/form-data");
		headers.set("Accept", "text/plain");
		String result = new RestTemplate().postForObject(
				"http://localhost:6565/tests",
				new HttpEntity<MultiValueMap<String, Object>>(parameters, headers),
				String.class);

		assertThat(result).isEqualTo("{\"status\":\"ok\"}");
	}

}
