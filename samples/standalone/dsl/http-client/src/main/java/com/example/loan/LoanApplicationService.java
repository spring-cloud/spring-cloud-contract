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
import com.example.loan.model.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoanApplicationService {

	private final RestTemplate restTemplate;

	private int port = 6565;
	
	private String prefix = "";

	@Autowired
	public LoanApplicationService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	public LoanApplicationResult loanApplication(LoanApplication loanApplication) {
		FraudServiceRequest request = new FraudServiceRequest(loanApplication);

		FraudServiceResponse response = sendRequestToFraudDetectionService(request);

		return buildResponseFromFraudResult(response);
	}

	private FraudServiceResponse sendRequestToFraudDetectionService(
			FraudServiceRequest request) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		// tag::client_call_server[]
		ResponseEntity<FraudServiceResponse> response = restTemplate.exchange(
				"http://localhost:" + port + fraudCheck(), HttpMethod.PUT,
				new HttpEntity<>(request, httpHeaders), FraudServiceResponse.class);
		// end::client_call_server[]

		return response.getBody();
	}

	private String fraudCheck() {
		return "/" + prefix + "fraudcheck";
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

	public int countAllFrauds() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<Response> response = restTemplate.exchange(
				"http://localhost:" + port + "/" + prefix + "frauds", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), Response.class);
		return response.getBody().getCount();
	}

	public int countDrunks() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<Response> response = restTemplate.exchange(
				"http://localhost:" + port + "/"+ prefix + "drunks", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), Response.class);
		return response.getBody().getCount();
	}

	public String getCookies() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Cookie", "name=foo");
		httpHeaders.add("Cookie", "name2=bar");
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/" + prefix + "frauds/name", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), String.class);
		return response.getBody();
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
