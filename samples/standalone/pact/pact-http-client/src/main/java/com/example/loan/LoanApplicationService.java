package com.example.loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.loan.model.FraudCheckStatus;
import com.example.loan.model.FraudServiceRequest;
import com.example.loan.model.FraudServiceResponse;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import com.example.loan.model.Response;

@Service
public class LoanApplicationService {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 =
			"application/vnd.fraud.v1+json";

	private final RestTemplate restTemplate;

	private int port = 6565;

	@Autowired
	public LoanApplicationService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	public LoanApplicationResult loanApplication(LoanApplication loanApplication) {
		FraudServiceRequest request =
				new FraudServiceRequest(loanApplication);

		FraudServiceResponse response =
				sendRequestToFraudDetectionService(request);

		return buildResponseFromFraudResult(response);
	}

	private FraudServiceResponse sendRequestToFraudDetectionService(
			FraudServiceRequest request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, FRAUD_SERVICE_JSON_VERSION_1);

		// tag::client_call_server[]
		ResponseEntity<FraudServiceResponse> response =
				restTemplate.exchange("http://localhost:" + port + "/fraudcheck", HttpMethod.PUT,
						new HttpEntity<>(request, httpHeaders),
						FraudServiceResponse.class);
		// end::client_call_server[]

		return response.getBody();
	}

	private LoanApplicationResult buildResponseFromFraudResult(FraudServiceResponse response) {
		LoanApplicationStatus applicationStatus = null;
		if (FraudCheckStatus.OK == response.getFraudCheckStatus()) {
			applicationStatus = LoanApplicationStatus.LOAN_APPLIED;
		} else if (FraudCheckStatus.FRAUD == response.getFraudCheckStatus()) {
			applicationStatus = LoanApplicationStatus.LOAN_APPLICATION_REJECTED;
		}

		return new LoanApplicationResult(applicationStatus, response.getRejectionReason());
	}

	public int countAllFrauds() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, FRAUD_SERVICE_JSON_VERSION_1);
		ResponseEntity<Response> response =
				restTemplate.exchange("http://localhost:" + port + "/frauds", HttpMethod.GET,
						new HttpEntity<>(httpHeaders),
						Response.class);
		return response.getBody().getCount();
	}

	public int countDrunks() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, FRAUD_SERVICE_JSON_VERSION_1);
		ResponseEntity<Response> response =
				restTemplate.exchange("http://localhost:" + port + "/drunks", HttpMethod.GET,
						new HttpEntity<>(httpHeaders),
						Response.class);
		return response.getBody().getCount();
	}

	public void setPort(int port) {
		this.port = port;
	}

}
