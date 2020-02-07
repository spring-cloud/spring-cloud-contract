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

package com.example.loan;

import java.net.URI;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;


@SpringBootTest
@AutoConfigureStubRunner(ids = "com.example:http-server-restdocs")
public class XmlServiceUsingStubRunnerTests {

	@Value("${stubrunner.runningstubs.http-server-restdocs.port}")
	int port;

	@Test
	public void shouldSuccessfullyReturnFullResponse() throws Exception {
		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate()
				.exchange(RequestEntity
						.post(URI.create("http://localhost:" + this.port + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("foo")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("FULL");
	}

	@Test
	public void shouldSuccessfullyReturnEmptyResponse() throws Exception {
		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate()
				.exchange(RequestEntity
						.post(URI.create("http://localhost:" + this.port + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("EMPTY");
	}

}
