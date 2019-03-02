/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.junit.runner.RunWith;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.WiremockServerWebTestClientApplicationTests.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@RunWith(SpringRunner.class)
@WebFluxTest
@ContextConfiguration(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets/webtestclient")
@AutoConfigureWebTestClient
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
