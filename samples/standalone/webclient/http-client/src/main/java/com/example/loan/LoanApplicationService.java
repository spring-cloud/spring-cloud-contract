/*
 * Copyright 2013-2020 the original author or authors.
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

package com.example.loan;

import com.example.loan.model.FraudCheckStatus;
import com.example.loan.model.FraudServiceRequest;
import com.example.loan.model.FraudServiceResponse;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoanApplicationService {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 = "application/vnd.fraud.v1+json";

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfiguration;

	public LoanApplicationService(ServiceConfiguration serviceConfiguration) {
		this.restTemplate = new RestTemplate();
		this.serviceConfiguration = serviceConfiguration;
	}

	public LoanApplicationResult loanApplication(LoanApplication loanApplication) {
		FraudServiceRequest request = new FraudServiceRequest(loanApplication);

		FraudServiceResponse response = sendRequestToFraudDetectionService(request);

		return buildResponseFromFraudResult(response);
	}

	private FraudServiceResponse sendRequestToFraudDetectionService(
			FraudServiceRequest request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, FRAUD_SERVICE_JSON_VERSION_1);

		// tag::client_call_server[]
		ResponseEntity<FraudServiceResponse> response = this.restTemplate.exchange(
				"http://localhost:" + getPort() + "/fraudcheck", HttpMethod.PUT,
				new HttpEntity<>(request, httpHeaders), FraudServiceResponse.class);
		// end::client_call_server[]

		return response.getBody();
	}

	private LoanApplicationResult buildResponseFromFraudResult(
			FraudServiceResponse response) {
		LoanApplicationStatus applicationStatus = null;
		if (FraudCheckStatus.OK == response.getFraudCheckStatus()) {
			applicationStatus = LoanApplicationStatus.LOAN_APPLIED;
		}
		else if (FraudCheckStatus.FRAUD == response.getFraudCheckStatus()) {
			applicationStatus = LoanApplicationStatus.LOAN_APPLICATION_REJECTED;
		}

		return new LoanApplicationResult(applicationStatus,
				response.getRejectionReason());
	}

	public int getPort() {
		return this.serviceConfiguration.getPort();
	}

	public void setPort(int port) {
		this.serviceConfiguration.setPort(port);
	}

}

@ConfigurationProperties("service")
class ServiceConfiguration {

	private int port = 8080;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}