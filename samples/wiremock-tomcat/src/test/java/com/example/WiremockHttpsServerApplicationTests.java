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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureHttpClient;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest("app.baseUrl=https://localhost:6443")
@AutoConfigureHttpClient
public class WiremockHttpsServerApplicationTests {

	public static WireMockServer wiremock = new WireMockServer(
			WireMockSpring.options().httpsPort(6443));

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
	public void contextLoads() throws Exception {
		wiremock.stubFor(get(urlEqualTo("/resource")).willReturn(aResponse()
				.withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

}
