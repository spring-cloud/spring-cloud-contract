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

import java.io.File
import java.net.URI
import java.nio.file.Files

import com.example.loan.model.Client
import com.example.loan.model.LoanApplication
import com.example.loan.model.LoanApplicationStatus
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

// tag::autoconfigure_stubrunner[]
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = ["com.example:http-server-kotlin:+:stubs:6565"],
		stubsMode = StubRunnerProperties.StubsMode.LOCAL)
class LoanApplicationServiceTests {
// end::autoconfigure_stubrunner[]

	@Autowired
	lateinit var service: LoanApplicationService

	@Test
	fun shouldSuccessfullyApplyForLoan() {
		// given:
		val application = LoanApplication(Client("1234567890"), 123.123)
		// when:
		val loanApplication = service.loanApplication(application)
		// then:
		assertThat(loanApplication?.loanApplicationStatus).isEqualTo(LoanApplicationStatus.LOAN_APPLIED)
		assertThat(loanApplication?.rejectionReason).isNull()
	}

	// tag::client_tdd[]
	@Test
	fun shouldBeRejectedDueToAbnormalLoanAmount() {
		// given:
		val application = LoanApplication(Client("1234567890"), 99999.0)
		// when:
		val loanApplication = service.loanApplication(application)
		// then:
		assertThat(loanApplication?.loanApplicationStatus)
				.isEqualTo(LoanApplicationStatus.LOAN_APPLICATION_REJECTED)
		assertThat(loanApplication?.rejectionReason).isEqualTo("Amount too high")
	}
	// end::client_tdd[]

	@Test
	fun shouldSuccessfullyGetAllFrauds() {
		// when:
		val count = service.countAllFrauds()
		// then:
		assertThat(count).isGreaterThanOrEqualTo(200)
	}

	@Test
	fun shouldSuccessfullyGetAllDrunks() {
		// when:
		val count = service.countDrunks()
		// then:
		assertThat(count).isEqualTo(100)
	}

	@Test
	@Suppress("UsePropertyAccessSyntax")
	fun shouldSuccessfullyGetCookies() {
		// when:
		val cookies = service.getCookies()
		// then:
		assertThat(cookies).isEqualTo("foo bar")
	}

	@Test
	fun shouldSuccessfullyWorkWithMultipart() {
		// given:
		val request = RestAssured.given()
				.baseUri("http://localhost:6565/")
				.header("Content-Type", "multipart/form-data")
				.multiPart("file1", "filename1", "content1".toByteArray())
				.multiPart("file2", "filename1", "content2".toByteArray())
				.multiPart("test", "filename1", "{\n  \"status\": \"test\"\n}".toByteArray(),
						"application/json")

		// when:
		val response = RestAssured.given().spec(request).post("/tests")

		// then:
		assertThat(response.statusCode).isEqualTo(200)
		assertThat(response.header("Content-Type")).matches("application/json.*")
		// and:
		val parsedJson = JsonPath.parse(response.body.asString())
		assertThatJson(parsedJson).field("['status']").isEqualTo("ok")
	}

	@Test
	fun shouldSuccessfullyWorkWithBinary() {
		// given:
		val request = File(javaClass.getResource("/binary/request.pdf").file)
		val response = File(javaClass.getResource("/binary/response.pdf").file)


		// when:
		val exchange: ResponseEntity<ByteArray> = RestTemplate().exchange(
						RequestEntity.put(URI.create("http://localhost:6565/1"))
								.header("Content-Type", "application/octet-stream")
								.body(Files.readAllBytes(request.toPath())),
						ByteArray::class.java)

		// then:
		assertThat(exchange.statusCodeValue).isEqualTo(200)
		assertThat(exchange.headers["Content-Type"]?.get(0))
				.isEqualTo("application/octet-stream")
		// and:
		assertThat(exchange.body).isEqualTo(Files.readAllBytes(response.toPath()))
	}

}
