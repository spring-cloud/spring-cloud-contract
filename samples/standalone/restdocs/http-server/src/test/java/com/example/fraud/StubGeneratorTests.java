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

package com.example.fraud;

import java.math.BigDecimal;

import com.example.fraud.model.FraudCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class StubGeneratorTests {

	@Autowired
	private MockMvc mockMvc;

	private JacksonTester<FraudCheck> json;

	@Before
	public void setup() {
		ObjectMapper objectMappper = new ObjectMapper();
		// Possibly configure the mapper
		JacksonTester.initFields(this, objectMappper);
	}

	@Test
	public void shouldMarkClientAsFraud() throws Exception {
		FraudCheck fraudCheck = new FraudCheck();
		fraudCheck.setClientId("1234567890");
		fraudCheck.setLoanAmount(BigDecimal.valueOf(99999.0));
		mockMvc.perform(MockMvcRequestBuilders.put("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.content(json.write(fraudCheck).getJson()))
				.andExpect(jsonPath("$.fraudCheckStatus").value("FRAUD"))
				.andExpect(jsonPath("$.rejectionReason").value("Amount too high"))
				.andDo(verify().jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount > 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.stub("markClientAsFraud"));
	}

	@Test
	public void shouldMarkClientAsNotFraud() throws Exception {
		FraudCheck fraudCheck = new FraudCheck();
		fraudCheck.setClientId("1234567890");
		fraudCheck.setLoanAmount(BigDecimal.valueOf(123.123));
		mockMvc.perform(MockMvcRequestBuilders.put("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.content(json.write(fraudCheck).getJson()))
				.andExpect(jsonPath("$.fraudCheckStatus").value("OK"))
				.andExpect(jsonPath("$.rejectionReason").doesNotExist())
				.andDo(verify().jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount <= 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.stub("markClientAsNotFraud"));
	}

}
