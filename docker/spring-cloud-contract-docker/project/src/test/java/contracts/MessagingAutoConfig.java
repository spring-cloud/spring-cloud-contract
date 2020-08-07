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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
public class MessagingAutoConfig {

	@Bean
	@ConditionalOnProperty("MESSAGING_TYPE")
	MessageVerifier manualMessageVerifier(@Value("${MESSAGING_TYPE:}") String messagingType, Environment environment, ConsumerTemplate consumerTemplate) {
		return new MessageVerifier() {
			@Override
			public Object receive(String destination, long timeout, TimeUnit timeUnit) {
				Exchange exchange = consumerTemplate.receive(messagingType() + "://" + destination + additionalOptions(destination), timeUnit.toMillis(timeout));
				if (exchange == null) {
					return null;
				}
				return exchange.getMessage().getBody();
			}

			private String messagingType() {
				if (messagingType.equalsIgnoreCase("kafka")) {
					return "kafka";
				}
				return "rabbitmq";
			}

			private String additionalOptions(String destination) {
				if (messagingType.equalsIgnoreCase("kafka")) {
					return "";
				}
				return "?queue=" + destination + "&addresses=" + environment.getRequiredProperty("RABBITMQ_HOST") + ":" + environment.getProperty("RABBITMQ_PORT", "5672");
			}

			@Override
			public Object receive(String destination) {
				return receive(destination, 5, TimeUnit.SECONDS);
			}

			@Override
			public void send(Object message, String destination) {
				throw new UnsupportedOperationException("Currently supports only receiving");
			}

			@Override
			public void send(Object payload, Map headers, String destination) {
				throw new UnsupportedOperationException("Currently supports only receiving");
			}
		};
	}

}
