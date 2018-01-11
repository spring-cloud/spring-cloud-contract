package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.WiremockServerWebTestClientApplicationTests.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

import wiremock.org.eclipse.jetty.http.HttpStatus;

@RunWith(SpringRunner.class)
@WebFluxTest
@ContextConfiguration(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets/webtestclient")
@AutoConfigureWebTestClient
@DirtiesContext
public class WiremockServerWebTestClientApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void contextLoads() throws Exception {
		this.client.get().uri("/resource").exchange().expectBody(String.class)
				.isEqualTo("Hello World").consumeWith(document("resource"));
	}

	@Test
	public void statusIsMaintained() throws Exception {
		this.client.get().uri("/status").exchange().expectStatus().isAccepted()
				.expectBody(String.class).isEqualTo("Hello World")
				.consumeWith(document("status"));
	}

	@Configuration
	@RestController
	protected static class TestConfiguration {

		@RequestMapping("/resource")
		public String resource() {
			return "Hello World";
		}

		@RequestMapping("/status")
		public ResponseEntity<String> status() {
			return ResponseEntity.status(HttpStatus.ACCEPTED_202).body("Hello World");
		}

	}

}
