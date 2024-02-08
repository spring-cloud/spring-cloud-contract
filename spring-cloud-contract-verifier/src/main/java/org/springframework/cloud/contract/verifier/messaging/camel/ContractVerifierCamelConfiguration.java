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

package org.springframework.cloud.contract.verifier.messaging.camel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.jetbrains.annotations.Nullable;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.jms.ContractVerifierJmsConfiguration;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Message.class)
@Import(CamelAutoConfiguration.class)
@ConditionalOnProperty(name = "stubrunner.camel.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore({ NoOpContractVerifierAutoConfiguration.class, ContractVerifierJmsConfiguration.class })
public class ContractVerifierCamelConfiguration {

	@Bean
	@ConditionalOnMissingBean(MessageVerifierSender.class)
	MessageVerifierSender<Message> camelContractVerifierMessageSender(CamelContext camelContext,
			ProducerTemplate producerTemplate, ConsumerTemplate consumerTemplate) {
		CamelStubMessages camelStubMessages = new CamelStubMessages(camelContext, producerTemplate, consumerTemplate);
		return new MessageVerifierSender<>() {
			@Override
			public void send(Message message, String destination, @Nullable YamlContract contract) {
				camelStubMessages.send(message, destination, contract);
			}

			@Override
			public <T> void send(T payload, Map<String, Object> headers, String destination,
					@Nullable YamlContract contract) {
				camelStubMessages.send(payload, headers, destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(MessageVerifierReceiver.class)
	MessageVerifierReceiver<Message> camelContractVerifierMessageReceiver(CamelContext camelContext,
			ProducerTemplate producerTemplate, ConsumerTemplate consumerTemplate) {
		CamelStubMessages camelStubMessages = new CamelStubMessages(camelContext, producerTemplate, consumerTemplate);
		return new MessageVerifierReceiver<>() {
			@Override
			public Message receive(String destination, long timeout, TimeUnit timeUnit,
					@Nullable YamlContract contract) {
				return camelStubMessages.receive(destination, timeout, timeUnit, contract);
			}

			@Override
			public Message receive(String destination, YamlContract contract) {
				return camelStubMessages.receive(destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(ContractVerifierMessaging.class)
	public ContractVerifierMessaging<Message> camelContractVerifierMessaging(MessageVerifierSender<Message> sender,
			MessageVerifierReceiver<Message> receiver) {
		return new ContractVerifierCamelHelper(sender, receiver);
	}

}

class ContractVerifierCamelHelper extends ContractVerifierMessaging<Message> {

	ContractVerifierCamelHelper(MessageVerifierSender<Message> sender, MessageVerifierReceiver<Message> receiver) {
		super(sender, receiver);
	}

	@Override
	protected ContractVerifierMessage convert(Message receive) {
		if (receive == null) {
			return null;
		}
		return new ContractVerifierMessage(receive.getBody(), receive.getHeaders());
	}

}
