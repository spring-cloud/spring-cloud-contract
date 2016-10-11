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
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource", String.class))
				.isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simpleGetWithEmptyPath() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-empty-path.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/", String.class))
				.isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simpleGetWithContentType() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-content-type.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource", String.class))
				.isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simpleGetWithoutContentType() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-without-content-type.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource", String.class))
				.isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simplePost() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/poster.json").build();
		assertThat(this.restTemplate.postForObject("http://example.org/poster", "greeting",
				String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void postWithHeader() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/*.json").ignoreExpectOrder(true).build();
		assertThat(this.restTemplate.exchange("http://example.org/poster", HttpMethod.POST,
				RequestEntity.EMPTY, String.class).getBody()).isEqualTo("Accepted World");
	}

	@Test
	public void simpleGetWithAllStubs() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings").ignoreExpectOrder(true).build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource", String.class))
				.isEqualTo("Hello World");
	}

	@Test
	public void simpleGetWithAllStubsInDirectoryWithPeriod() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/io.stubs/mappings").ignoreExpectOrder(true).build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource", String.class))
				.isEqualTo("Hello World");
	}

}
