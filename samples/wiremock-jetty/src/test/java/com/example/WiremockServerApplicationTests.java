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

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "app.baseUrl=http://localhost:6061", webEnvironment = WebEnvironment.NONE)
public class WiremockServerApplicationTests {

	@ClassRule
	public static WireMockClassRule wiremock = new WireMockClassRule(
			WireMockSpring.options().port(6061));

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Autowired
	private Service service;

	@Test
	public void hello() throws Exception {
		stubFor(get(urlEqualTo("/resource")).willReturn(aResponse()
				.withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

	@Test
	public void randomData() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
		this.expected.expectCause(instanceOf(ClientProtocolException.class));
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

	@Test
	public void emptyResponse() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
		this.expected.expectCause(instanceOf(NoHttpResponseException.class));
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

	@Test
	public void malformed() throws Exception {
		stubFor(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
		this.expected.expectCause(instanceOf(ClientProtocolException.class));
		assertThat(this.service.go()).isEqualTo("Oops!");
	}

}
