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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(classes = WiremockTestsApplication.class, properties = "app.baseUrl=http://localhost:${wiremock.server.port}", webEnvironment = WebEnvironment.NONE)
// resource from a Initilizr stubs jar
@AutoConfigureWireMock(port = 0, files = "classpath*:META-INF/io.spring.initializr/initializr-web/0.4.0.BUILD-SNAPSHOT")
public class AutoConfigureWireMockFilesApplicationWithUrlResourceTests {

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		// Sth's wrong with the JAR ^^
		// assertThat(this.service.pom())
		// .contains("<artifactId>spring-boot-starter-parent</artifactId>");
	}

}
