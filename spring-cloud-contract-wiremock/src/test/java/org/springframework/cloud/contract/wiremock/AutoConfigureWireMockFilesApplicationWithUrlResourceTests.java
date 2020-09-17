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

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AutoConfigureWireMockFilesApplicationWithUrlResourceTests.Config.class,
		properties = "app.baseUrl=http://localhost:${wiremock.server.port}",
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
// tag::load_all_stubs[]
@AutoConfigureWireMock(port = 0, stubs = "classpath*:/META-INF/**/mappings/**/*.json")
// end::load_all_stubs[]
public class AutoConfigureWireMockFilesApplicationWithUrlResourceTests {

	@Value("${wiremock.server.port}")
	int wiremockPort;

	@Test
	public void should_work_for_analytics() throws Exception {
		String count = new RestTemplate().getForObject("http://localhost:" + this.wiremockPort + "/issues/count",
				String.class);

		BDDAssertions.then(count).isEqualTo("5");
	}

	@Test
	public void should_work_for_webhook() throws Exception {
		String response = new RestTemplate().getForObject("http://localhost:" + this.wiremockPort + "/", String.class);

		BDDAssertions.then(response).contains("spring-cloud/spring-cloud-netflix");
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

	}

}
