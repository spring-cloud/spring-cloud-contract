package com.example;

import java.io.IOException;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(SpringRunner.class)
@SpringBootTest(properties="app.baseUrl=http://localhost:6067", webEnvironment=WebEnvironment.NONE)
@DirtiesContext
public class WiremockServerApplicationTests {

	@ClassRule
	public static WireMockClassRule wiremock = new WireMockClassRule(WireMockSpring.options().port(6067));

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

	@Test
	public void randomData() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
		expected.expectCause(instanceOf(ClientProtocolException.class));
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

	@Test
	public void emptyResponse() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
		expected.expectCause(instanceOf(NoHttpResponseException.class));
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

	@Test
	public void malformed() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
		// It's a different exception type than Jetty, but it's in the right ballpark
		expected.expectCause(instanceOf(IOException.class));
		expected.expectMessage("chunk");
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

}
