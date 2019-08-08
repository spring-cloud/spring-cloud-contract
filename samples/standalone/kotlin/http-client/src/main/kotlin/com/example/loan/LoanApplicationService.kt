/*
 * Copyright 2013-2019 the original author or authors.
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

package com.example.loan

import com.example.loan.model.FraudCheckStatus
import com.example.loan.model.FraudServiceRequest
import com.example.loan.model.FraudServiceResponse
import com.example.loan.model.LoanApplication
import com.example.loan.model.LoanApplicationResult
import com.example.loan.model.LoanApplicationStatus
import com.example.loan.model.Response
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class LoanApplicationService {

	private var port = 6565
	private val restTemplate: RestTemplate

	@Autowired
	constructor(builder: RestTemplateBuilder) : this(builder.build())

	constructor(restTemplate: RestTemplate) {
		this.restTemplate = restTemplate
	}

	fun loanApplication(loanApplication: LoanApplication): LoanApplicationResult? {
		val request = FraudServiceRequest(loanApplication)
		val response = sendRequestToFraudDetectionService(request)
		return buildResponseFromFraudResult(response)
	}

	private fun sendRequestToFraudDetectionService(request: FraudServiceRequest): FraudServiceResponse? {
		val httpHeaders = HttpHeaders()
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

		// tag::client_call_server[]
		val response = restTemplate.exchange("http://localhost:$port/fraudcheck",
				HttpMethod.PUT, HttpEntity(request, httpHeaders), FraudServiceResponse::class.java)
		// end::client_call_server[]

		return response.body
	}

	private fun buildResponseFromFraudResult(response: FraudServiceResponse?): LoanApplicationResult? {
		return response?.let {
			val applicationStatus = when(response.fraudCheckStatus) {
				FraudCheckStatus.OK -> LoanApplicationStatus.LOAN_APPLIED
				FraudCheckStatus.FRAUD -> LoanApplicationStatus.LOAN_APPLICATION_REJECTED
			}
			LoanApplicationResult(applicationStatus, response.rejectionReason)
		}
	}

	fun countAllFrauds(): Int? {
		val httpHeaders = HttpHeaders()
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		val response = restTemplate.exchange("http://localhost:$port/frauds",
				HttpMethod.GET, HttpEntity<String>(httpHeaders), Response::class.java)
		return response.body?.count
	}

	fun countDrunks(): Int? {
		val httpHeaders = HttpHeaders()
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		val response = restTemplate.exchange("http://localhost:$port/drunks",
				HttpMethod.GET, HttpEntity<String>(httpHeaders), Response::class.java)
		return response.body?.count
	}

	fun getCookies(): String? {
		val httpHeaders = HttpHeaders()
		httpHeaders.add("Cookie", "name=foo")
		httpHeaders.add("Cookie", "name2=bar")
		val response = restTemplate.exchange("http://localhost:$port/frauds/name",
				HttpMethod.GET, HttpEntity<String>(httpHeaders), String::class.java)
		return response.body
	}

	fun setPort(port: Int) {
		this.port = port
	}

}
