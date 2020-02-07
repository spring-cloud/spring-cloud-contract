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

package com.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(properties = "app.baseUrl=http://localhost:6061", webEnvironment = WebEnvironment.NONE)
public class WiremockServerApplicationTests {

	public static WireMockServer wiremock = new WireMockServer(
			WireMockSpring.options().port(6061));

	@BeforeAll
	static void setup() {
		wiremock.start();
	}

	@AfterEach
	void after() {
		wiremock.resetAll();
	}

	@AfterAll
	static void clean() {
		wiremock.shutdown();
	}

	@Autowired
	private Service service;

	@Test
	public void hello() throws Exception {
		wiremock.stubFor(get(urlEqualTo("/resource")).willReturn(aResponse()
				.withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

	@Test
	public void randomData() throws Exception {
		wiremock.stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

		assertThatThrownBy(() -> this.service.go()).hasCauseInstanceOf(ClientProtocolException.class);
	}

	@Test
	public void emptyResponse() throws Exception {
		wiremock.stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

		assertThatThrownBy(() -> this.service.go()).hasCauseInstanceOf(NoHttpResponseException.class);
	}

	@Test
	public void malformed() throws Exception {
		wiremock.stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

		assertThatThrownBy(() -> this.service.go()).hasCauseInstanceOf(ClientProtocolException.class);
	}

}
