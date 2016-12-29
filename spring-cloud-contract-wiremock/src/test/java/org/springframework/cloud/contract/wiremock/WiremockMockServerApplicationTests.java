package org.springframework.cloud.contract.wiremock;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class WiremockMockServerApplicationTests {

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void simpleGet() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simplePutShouldFail() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource.json")
				.build();
		RequestEntity<Void> postRequest = RequestEntity.post(URI.create("http://example.org/resource"))
				.accept(MediaType.TEXT_PLAIN)
				.build();
		ResponseEntity<String> response;
		try {
			response = this.restTemplate.exchange(postRequest, String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for POST request: " + postRequest + "\n\tresponse: " + response);
		}
	}

	@Test
	public void simpleGetWithBodyFile() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-body-file.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("{\"message\":\"Hello World\"}");
		server.verify();
	}

	@Test
	public void simpleGetWithBodyFileCustomLocation() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-body-file.json")
				.files("classpath:/custom/").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("{\"message\":\"Hello Custom\"}");
		server.verify();
	}

	@Test
	public void simpleGetWithBodyFileCustomLocationDirectory() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-body-file.json")
				.files("file:src/test/resources/custom").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("{\"message\":\"Hello Custom\"}");
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
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simpleGetWithoutContentType() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-without-content-type.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void simplePost() throws Exception {
		MockRestServiceServer server = WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/poster.json").build();
		assertThat(this.restTemplate.postForObject("http://example.org/poster",
				"greeting", String.class)).isEqualTo("Hello World");
		server.verify();
	}

	@Test
	public void postWithHeader() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") // order determined by content...
				.stubs("classpath:/mappings/poster.json",
						"classpath:/mappings/accept.json")
				.build();
		assertThat(
				this.restTemplate
						.exchange(
								RequestEntity.post(new URI("http://example.org/poster"))
										.accept(MediaType.TEXT_PLAIN).build(),
								String.class)
						.getBody()).isEqualTo("Accepted World");
	}

	@Test
	public void postWithHeaderContains() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") // order determined by content...
				.stubs("classpath:/mappings/poster.json",
						"classpath:/mappings/header-contains.json")
				.build();
		assertThat(this.restTemplate.exchange(
				RequestEntity.post(new URI("http://example.org/poster"))
						.accept(MediaType.valueOf("application/v.foo")).build(),
				String.class).getBody()).isEqualTo("Foo World");
	}

	@Test
	public void postWithHeaderMatches() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") // order determined by content...
				.stubs("classpath:/mappings/poster.json",
						"classpath:/mappings/header-matches.json")
				.build();
		assertThat(this.restTemplate.exchange(
				RequestEntity.post(new URI("http://example.org/poster"))
						.accept(MediaType.valueOf("application/v.bar")).build(),
				String.class).getBody()).isEqualTo("Bar World");
	}

	@Test
	public void postWithMoreExactHeaderMatch() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") // order determined by content...
				.stubs("classpath:/mappings/header-matches.json",
						"classpath:/mappings/header-matches-precise.json")
				.build();
		assertThat(this.restTemplate
				.exchange(RequestEntity.post(new URI("http://example.org/poster"))
						.accept(MediaType.valueOf("application/v.bar"))
						.header("X-Precise", "true").build(), String.class)
				.getBody()).isEqualTo("Precise World");
	}

	@Test
	public void postWithMoreExactHeaderMatchButOrdered() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") // order matters...
				.stubs("classpath:/mappings/header-matches.json",
						"classpath:/mappings/header-matches-precise.json")
				.ignoreExpectOrder(false).build();
		assertThat(this.restTemplate
				.exchange(RequestEntity.post(new URI("http://example.org/poster"))
						.accept(MediaType.valueOf("application/v.bar"))
						.header("X-Precise", "true").build(), String.class)
				.getBody()).isEqualTo("Bar World");
		// The first one matches, not the most precise!
	}

	@Test
	public void getWithPriortyOrder() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/resource-with-low-priority.json",
						"classpath:/mappings/resource-with-high-priority.json").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello High");
	}

	@Test
	public void simpleGetWithAllStubs() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello World");
	}

	@Test
	public void simpleGetWithAllStubsInDirectoryWithPeriod() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/io.stubs/mappings").build();
		assertThat(this.restTemplate.getForObject("http://example.org/resource",
				String.class)).isEqualTo("Hello World");
	}

	// Issue #135
	@Test
	public void postWithBodyMatchingJsonPaths() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-jsonpath.json").build();

		assertThat(this.restTemplate.postForObject("http://example.org/body",
				new Things(Collections.singletonList(new Thing("RequiredThing"))), String.class))
				.isEqualTo("Hello Body");
	}

	@Test
	public void postWithRequestThatHasNonMatchingJsonPaths() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-jsonpath.json").build();

		String response;
		try {
			response = this.restTemplate.postForObject("http://example.org/body",
					new Things(Collections.singletonList(new Thing("AbsentThing"))), String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for a request that shouldn't be matched : " + response);
		}
	}

	@Test
	public void postWithBodyMatchingXPath() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-xpath.json").build();

		assertThat(this.restTemplate.exchange(
				RequestEntity.post(URI.create("http://example.org/body"))
					.contentType(MediaType.APPLICATION_XML)
					.body("<things><thing><name>RequiredThing</name></thing></things>"),
				String.class).getBody()).isEqualTo("Hello Body");
	}

	@Test
	public void postWithRequestThatHasNonMatchingXPaths() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-xpath.json").build();

		ResponseEntity<String> response;
		try {
			response = this.restTemplate.exchange(
					RequestEntity.post(URI.create("http://example.org/body"))
							.contentType(MediaType.APPLICATION_XML)
							.body("<foo/>"),
					String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for a request that shouldn't be matched : " + response);
		}
	}

	@Test
	public void postWithBodyMatchingJson() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-equaltojson.json").build();

		assertThat(this.restTemplate.postForObject("http://example.org/body",
				new Things(Collections.singletonList(new Thing("RequiredThing"))), String.class))
				.isEqualTo("Hello Body");
	}

	@Test
	public void postWithRequestThatHasNonMatchingJson() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-equaltojson.json").build();

		String response;
		try {
			response = this.restTemplate.postForObject("http://example.org/body",
					new Things(Collections.singletonList(new Thing("AbsentThing"))), String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for a request that shouldn't be matched : " + response);
		}
	}

	@Test
	public void postWithBodyMatchingXml() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-equaltoxml.json").build();

		assertThat(this.restTemplate.exchange(
				RequestEntity.post(URI.create("http://example.org/body"))
						.contentType(MediaType.APPLICATION_XML)
						.body("<things><thing><name>RequiredThing</name></thing></things>"),
				String.class).getBody()).isEqualTo("Hello Body");
	}

	@Test
	public void postWithRequestThatHasNonMatchingXml() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-equaltoxml.json").build();

		ResponseEntity<String> response;
		try {
			response = this.restTemplate.exchange(
					RequestEntity.post(URI.create("http://example.org/body"))
							.contentType(MediaType.APPLICATION_XML)
							.body("<things><thing><name>AbsentThing</name></thing></things>"),
					String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for a request that shouldn't be matched : " + response);
		}
	}

	@Test
	public void postWithBodyMatchingRegex() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-regex.json").build();

		assertThat(this.restTemplate.exchange(
				RequestEntity.post(URI.create("http://example.org/body"))
						.contentType(MediaType.APPLICATION_XML)
						.body("<things><thing><name>RequiredThing</name></thing></things>"),
				String.class).getBody()).isEqualTo("Hello Body");
	}

	@Test
	public void postWithRequestThatHasNonMatchingRegex() throws Exception {
		WireMockRestServiceServer.with(this.restTemplate) //
				.baseUrl("http://example.org") //
				.stubs("classpath:/mappings/body-matches-regex.json").build();

		ResponseEntity<String> response;
		try {
			response = this.restTemplate.exchange(
					RequestEntity.post(URI.create("http://example.org/body"))
							.contentType(MediaType.APPLICATION_XML)
							.body("<things><thing><name>AbsentThing</name></thing></things>"),
					String.class);
		} catch (AssertionError e) {
			response = null;
		}
		if (null != response) {
			fail("There was a response for a request that shouldn't be matched : " + response);
		}
	}

	public static class Things {
		public List<Thing> things;

		@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
		public Things(List<Thing> things) {
			this.things = things;
		}
	}

	public static class Thing {
		public String name;

		@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
		public Thing(String name) {
			this.name = name;
		}
	}

}
