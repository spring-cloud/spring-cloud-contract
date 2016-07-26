package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockRestServiceServer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
@DirtiesContext
public class WiremockMockServerApplicationTests {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/stubs/**/*.json");
		assertThat(this.service.go()).isEqualTo("Hello World");
		server.verify();
	}

}
