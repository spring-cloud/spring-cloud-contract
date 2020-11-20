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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WiremockTestsApplication.class, properties = {
		"app.baseUrl=http://localhost:${wiremock.server.port}", "wiremock.reset-mappings-after-each-test=true" },
		webEnvironment = WebEnvironment.NONE)
@ComponentTestAnnotation
@FixMethodOrder
@DirtiesContext
public class MetaAnnotationWithResetAfterEachTestApplicationTests {

	@Autowired
	private WireMockServer wireMockServer;

	@Value("localhost:${wiremock.server.port}")
	private String hostname;

	@Test
	public void _01_test() throws Exception {
		this.wireMockServer
				.givenThat(WireMock.get("/should_register_mapping").willReturn(WireMock.aResponse().withBody("bar")));

		String result = new RestTemplate().getForObject("http://" + this.hostname + "/should_register_mapping",
				String.class);

		then(result).isEqualTo("bar");
	}

	@Test
	public void _02_test() throws Exception {
		String result = new RestTemplate().getForObject("http://" + this.hostname + "/should_register_mapping",
				String.class);

		// taken from test/resources/mappings/resource-without-content-type.json
		then(result).isEqualTo("Hello World");
	}

	@Test
	public void _03_test() throws Exception {
		WireMock.givenThat(WireMock.get("/should_register_mapping").willReturn(WireMock.aResponse().withBody("bar")));

		String result = new RestTemplate().getForObject("http://" + this.hostname + "/should_register_mapping",
				String.class);

		then(result).isEqualTo("bar");
	}

	@Test
	public void _04_test() throws Exception {
		String result = new RestTemplate().getForObject("http://" + this.hostname + "/should_register_mapping",
				String.class);

		// taken from test/resources/mappings/resource-without-content-type.json
		then(result).isEqualTo("Hello World");
	}

}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@interface ComponentTestAnnotation {

}
