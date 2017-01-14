package com.example;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;


@RunWith(SpringRunner.class)
@SpringBootTest("app.baseUrl=https://localhost:7443")
@DirtiesContext
@ActiveProfiles("ssl")
public class WiremockHttpsServerApplicationTests {

	@ClassRule
	public static WireMockClassRule wiremock = new WireMockClassRule(
			WireMockSpring.options().httpsPort(7443).port(SocketUtils.findAvailableTcpPort()));

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

}
