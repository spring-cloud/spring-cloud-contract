package com.example.fraud;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.cloud.contract.wiremock.RestDocsContracts;
import org.springframework.cloud.contract.wiremock.WireMockSnippet;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.fraud.model.FraudCheck;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@DirtiesContext
public class StubGeneratorTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WireMockSnippet snippet;

	private JacksonTester<FraudCheck> json;

	@Test
	public void shouldMarkClientAsFraud() throws Exception {
		FraudCheck fraudCheck = new FraudCheck();
		fraudCheck.setClientId("1234567890");
		fraudCheck.setLoanAmount(BigDecimal.valueOf(99999.0));
		mockMvc.perform(MockMvcRequestBuilders.put("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.content(json.write(fraudCheck).getJson()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.fraudCheckStatus")
						.value("FRAUD"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.rejectionReason")
						.value("Amount too high"))
				.andDo(RestDocsContracts.content(snippet).jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount > 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.contract("markClientAsFraud"));
	}

	@Test
	public void shouldMarkClientAsNotFraud() throws Exception {
		FraudCheck fraudCheck = new FraudCheck();
		fraudCheck.setClientId("1234567890");
		fraudCheck.setLoanAmount(BigDecimal.valueOf(1));
		mockMvc.perform(MockMvcRequestBuilders.put("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.content(json.write(fraudCheck).getJson()))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.fraudCheckStatus").value("OK"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.rejectionReason")
						.doesNotExist())
				.andDo(RestDocsContracts.content(snippet).jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount <= 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.contract("markClientAsNotFraud"));
	}

}