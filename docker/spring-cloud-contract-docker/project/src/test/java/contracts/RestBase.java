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

package contracts;

import java.time.Duration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Marcin Grzejszczak
 */
@SpringBootTest(classes = RestBase.Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureMessageVerifier
public abstract class RestBase {

	private static final Logger log = LoggerFactory.getLogger(RestBase.class);

	@Value("${APPLICATION_BASE_URL}")
	String url;

	@Value("${APPLICATION_USERNAME:}")
	String username;

	@Value("${APPLICATION_PASSWORD:}")
	String password;

	@BeforeEach
	public void setup() {
		RestAssured.baseURI = this.url;
		if (StringUtils.hasText(this.username)) {
			RestAssured.authentication = RestAssured.basic(this.username, this.password);
		}
	}

	public void triggerMessage(String label) {
		String url = this.url + "/springcloudcontract/" + label;
		log.info("Will send a request to [{}] in order to trigger a message", url);
		restTemplate().postForObject(url, "", String.class);
	}

	private RestTemplate restTemplate() {
		RestTemplateBuilder builder = new RestTemplateBuilder()
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(5));
		if (StringUtils.hasText(this.username)) {
			builder = builder.basicAuthentication(this.username, this.password);
		}
		return builder.build();
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

}
