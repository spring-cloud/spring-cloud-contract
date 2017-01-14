package com.example;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

@ActiveProfiles("classrule")
@DirtiesContext
//tag::wiremock_test1[]
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WiremockForDocsClassRuleTests {

	// Start WireMock on some dynamic port
	// for some reason `dynamicPort()` is not working properly
	@ClassRule
	public static WireMockClassRule wiremock = new WireMockClassRule(
			WireMockSpring.options().dynamicPort());
//end::wiremock_test1[]
	@Before
	public void setup() {
		this.service.setBase("http://localhost:" + wiremock.port());
	}
//tag::wiremock_test2[]
	// A service that calls out over HTTP to localhost:${wiremock.port}
	@Autowired
	private Service service;

	// Using the WireMock APIs in the normal way:
	@Test
	public void contextLoads() throws Exception {
		// Stubbing WireMock
		wiremock.stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		// We're asserting if WireMock responded properly
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

}
//end::wiremock_test2[]