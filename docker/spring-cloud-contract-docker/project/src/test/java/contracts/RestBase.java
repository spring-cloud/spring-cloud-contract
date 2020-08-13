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
import java.util.concurrent.TimeUnit;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.amqp.AmqpMetadata;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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

	@Value("${MESSAGING_TRIGGER_CONNECT_TIMEOUT:5000}")
	Integer connectTimeout;

	@Value("${MESSAGING_TRIGGER_READ_TIMEOUT:5000}")
	Integer readTimeout;

	@Value("${MESSAGING_TYPE:}")
	String messagingType;

	@Autowired
	MessageVerifier messageVerifier;

	@BeforeEach
	public void setup(TestInfo testInfo) {
		RestAssured.baseURI = this.url;
		if (StringUtils.hasText(this.username)) {
			RestAssured.authentication = RestAssured.basic(this.username, this.password);
		}
		YamlContract contract = ContractVerifierUtil.contract(this, testInfo.getDisplayName());
		setupAmqpIfPresent(contract);
	}

	private void setupAmqpIfPresent(YamlContract contract) {
		AmqpMetadata amqpMetadata = AmqpMetadata.fromMetadata(contract.metadata);
		if (isMessagingType("rabbit") && hasDeclaredOutputQueue(amqpMetadata) || isMessagingType("kafka")) {
			log.info("First will try to receive a message to generate a queue");
			this.messageVerifier.receive(contract.outputMessage.sentTo, 100, TimeUnit.MILLISECONDS, contract);
		}
	}

	private boolean hasDeclaredOutputQueue(AmqpMetadata amqpMetadata) {
		return StringUtils.hasText(amqpMetadata.getOutputMessage().getDeclareQueueWithName());
	}

	private boolean isMessagingType(String rabbit) {
		return rabbit.equalsIgnoreCase(this.messagingType);
	}

	public void triggerMessage(String label) {
		String url = this.url + "/springcloudcontract/" + label;
		log.info("Will send a request to [{}] in order to trigger a message", url);
		restTemplate().postForObject(url, "", String.class);
	}

	private RestTemplate restTemplate() {
		RestTemplateBuilder builder = new RestTemplateBuilder()
				.setConnectTimeout(Duration.ofMillis(this.connectTimeout))
				.setReadTimeout(Duration.ofMillis(this.readTimeout));
		if (StringUtils.hasText(this.username)) {
			builder = builder.basicAuthentication(this.username, this.password);
		}
		return builder.build();
	}

	@Configuration
	@Import(MessagingAutoConfig.class)
	@EnableAutoConfiguration
	protected static class Config {

	}

}
