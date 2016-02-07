package com.blogspot.toomuchcoding.frauddetection;

import com.blogspot.toomuchcoding.frauddetection.model.FraudCheckStatus;
import com.blogspot.toomuchcoding.frauddetection.model.FraudServiceRequest;
import com.blogspot.toomuchcoding.frauddetection.model.FraudServiceResponse;
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplication;
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplicationResult;
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplicationStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoanApplicationService {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 =
			"application/vnd.fraud.v1+json";

	private final RestTemplate restTemplate;

	private int port = 8080;

	public LoanApplicationService() {
		this.restTemplate = new RestTemplate();
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

		ResponseEntity<FraudServiceResponse> response =
				restTemplate.exchange("http://localhost:" + port + "/fraudcheck", HttpMethod.PUT,
						new HttpEntity<>(request, httpHeaders),
						FraudServiceResponse.class);

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

	public void setPort(int port) {
		this.port = port;
	}

}
