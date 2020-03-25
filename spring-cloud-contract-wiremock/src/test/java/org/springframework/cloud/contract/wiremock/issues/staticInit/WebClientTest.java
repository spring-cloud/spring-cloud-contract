/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.issues.staticInit;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest(
		properties = "my-client.url=http://localhost:${wiremock.server.port}/resources")
@AutoConfigureWireMock(port = 0, httpsPort = 0, files = "src/test/resources/__files")
class WebClientTest {

	private static Logger log = LoggerFactory.getLogger(WebClientTest.class);

	@Autowired
	WebClient underTest;

	@Autowired
	Stubber stubber;

	@Test
	void shouldRespondForGetJustFine() {
		String actual = underTest.get();

		BDDAssertions.then(actual).isEqualTo("Everything seems fine!");
	}

	@TestConfiguration
	static class WebClientTestConfiguration {

		@Bean
		Stubber stubberWithoutDeps() {
			return new Stubber();
		}

	}

	static class Stubber {

		Stubber() {
			log.info("Setting up GET");
			stubFor(get(urlEqualTo("/resources"))
					.willReturn(aResponse().withBody("Everything seems fine!")));
		}

		Stubber(WireMockServer wireMockServer) {
			log.info("Setting up GET");
			wireMockServer.stubFor(get(urlEqualTo("/resources"))
					.willReturn(aResponse().withBody("Everything seems fine!")));
		}

	}

}
