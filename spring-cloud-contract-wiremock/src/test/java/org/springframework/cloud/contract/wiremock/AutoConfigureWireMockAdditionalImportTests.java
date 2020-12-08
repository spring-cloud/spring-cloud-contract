/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import java.util.Collections;
import java.util.List;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

// issue 1541
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfiguration.class, properties = "base-url=http://localhost:${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
@Import(ExtraConfig.class)
public class AutoConfigureWireMockAdditionalImportTests {

	@AfterEach
	public void resetWiremock() {
		WireMock.reset();
	}

	@Nested
	@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfiguration.class, properties = "base-url=http://localhost:${wiremock.server.port}")
	@AutoConfigureWireMock(port = 0)
	class SecondControllerTest {

		@Test
		void test(@Autowired WebTestClient webTestClient) {
			// arrange
			WireMock.stubFor(WireMock.get(WireMock.urlMatching("/find-all"))
					.willReturn(ResponseDefinitionBuilder.okForJson(Collections.singletonList(new TestItem("my-name")))));

			// act
			List<TestItem> responseBody = webTestClient.get().uri("find-all")
					.exchange().expectStatus().is2xxSuccessful()
					.expectBody(new ParameterizedTypeReference<List<TestItem>>() { }).returnResult().getResponseBody();

			// assert
			Assertions.assertThat(responseBody.get(0).getName()).isEqualTo("my-name");
		}
	}
}

@Component
class ExtraConfig {

}

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
class TestConfiguration {

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	TestController testController(WebClient webClient, @Value("${base-url}") String baseUrl) {
		return new TestController(webClient, baseUrl);
	}

}

@RestController
class TestController {

	private final WebClient webClient;

	private final String baseUrl;

	public TestController(WebClient webClient, String baseUrl) {
		this.webClient = webClient;
		this.baseUrl = baseUrl;
		System.out.println("Creating with URL [" + this.baseUrl + "] HASH [" + this.hashCode() + "]");
	}

	@GetMapping("find-all")
	public Mono<List<TestItem>> findAll() {
		System.out.println("Will send a request to [" + this.baseUrl + "] HASH [" + this.hashCode() + "]");
		return webClient
				.get()
				.uri(baseUrl + "/find-all")
				.retrieve().bodyToMono(new ParameterizedTypeReference<List<TestItem>>() {});
	}

}

class TestItem {

	private String name;

	public TestItem() { }

	public TestItem(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
