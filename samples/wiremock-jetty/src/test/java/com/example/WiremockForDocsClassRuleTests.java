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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("classrule")
// tag::wiremock_test1[]

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WiremockForDocsClassRuleTests {

	// Start WireMock on some dynamic port
	// for some reason `dynamicPort()` is not working properly
	public static WireMockServer wiremock = new WireMockServer(WireMockSpring.options().dynamicPort());

	@BeforeAll
	static void setupClass() {
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

	// A service that calls out over HTTP to wiremock's port
	@Autowired
	private Service service;

	// end::wiremock_test1[]
	// tag::wiremock_test2[]
	@BeforeEach
	public void setup() {
		this.service.setBase("http://localhost:" + wiremock.port());
	}

	// Using the WireMock APIs in the normal way:
	@Test
	public void contextLoads() throws Exception {
		// Stubbing WireMock
		wiremock.stubFor(get(urlEqualTo("/resource")).willReturn(aResponse()
				.withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		// We're asserting if WireMock responded properly
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

}
// end::wiremock_test2[]
