package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class WiremockMockServerApplicationTests {

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void simpleGet() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("https://example.org") //
				.stubs("classpath:/mappings/resource.json").build();
		assertThat(this.restTemplate.getForObject("https://example.org/resource", String.class))
				.isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simplePost() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("https://example.org") //
				.stubs("classpath:/mappings/poster.json").build();
		assertThat(this.restTemplate.postForObject("https://example.org/poster", "greeting",
				String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void postWithHeader() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("https://example.org") //
				.stubs("classpath:/mappings/*.json").ignoreExpectOrder(true).build();
		assertThat(this.restTemplate.exchange("https://example.org/poster", HttpMethod.POST,
				RequestEntity.EMPTY, String.class).getBody()).isEqualTo("Accepted World");
	}

	@Test
	public void simpleGetWithAllStubs() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("https://example.org") //
				.stubs("classpath:/mappings").ignoreExpectOrder(true).build();
		assertThat(this.restTemplate.getForObject("https://example.org/resource", String.class))
				.isEqualTo("Hello World");
	}

}
