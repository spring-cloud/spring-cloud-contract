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

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Disabled("jakarta")
public class WiremockServerRestAssuredApplicationTests {

	@Autowired
	private RequestSpecification documentationSpec;

	@LocalServerPort
	private int port;

	/*
	 * @Test public void contextLoads() throws Exception {
	 * given().port(this.port).when().get("/resource").then().assertThat().statusCode(is(
	 * 200)) .body(equalTo("Hello World")); }
	 *
	 * @Test public void statusIsMaintained() throws Exception {
	 * given(this.documentationSpec.port(this.port)).filter(document("status")).when().get
	 * ("/status").then() .assertThat().statusCode(is(202)).body(equalTo("Hello World"));
	 * }
	 */

	@Test
	void foo() {

	}

	@Configuration
	@EnableWebMvc
	@RestController
	@ImportAutoConfiguration(DispatcherServletAutoConfiguration.class)
	protected static class TestConfiguration {

		@ResponseBody
		@RequestMapping("/resource")
		public String resource() {
			return "Hello World";
		}

		@ResponseBody
		@RequestMapping("/status")
		public ResponseEntity<String> status() {
			return ResponseEntity.status(HttpStatus.ACCEPTED_202).body("Hello World");
		}

		@Bean
		TomcatServletWebServerFactory tomcatServletWebServerFactory() {
			return new TomcatServletWebServerFactory(0);
		}

	}

}
