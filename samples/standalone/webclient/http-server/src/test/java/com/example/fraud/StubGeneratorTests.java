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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.cloud.contract.wiremock.restdocs.WireMockWebTestClient.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureWebTestClient
@AutoConfigureJsonTesters
@DirtiesContext
public class StubGeneratorTests {

	@Autowired
	private WebTestClient client;

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
		client.put().uri("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.body(BodyInserters.fromObject(json.write(fraudCheck).getJson()))
				.exchange().expectBody().jsonPath("$.fraudCheckStatus").isEqualTo("FRAUD")
				.jsonPath("$.rejectionReason").isEqualTo("Amount too high")
				.consumeWith(verify().jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount > 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.stub("markClientAsFraud"));
	}

	@Test
	public void shouldMarkClientAsNotFraud() throws Exception {
		FraudCheck fraudCheck = new FraudCheck();
		fraudCheck.setClientId("1234567890");
		fraudCheck.setLoanAmount(BigDecimal.valueOf(123.123));
		client.put().uri("/fraudcheck")
				.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
				.body(BodyInserters.fromObject(json.write(fraudCheck).getJson()))
				.exchange().expectBody().jsonPath("$.fraudCheckStatus").isEqualTo("OK")
				.jsonPath("$.rejectionReason").doesNotExist()
				.consumeWith(verify().jsonPath("$.clientId")
						.jsonPath("$[?(@.loanAmount <= 1000)]")
						.contentType(MediaType.valueOf("application/vnd.fraud.v1+json"))
						.stub("markClientAsNotFraud"));
	}

}