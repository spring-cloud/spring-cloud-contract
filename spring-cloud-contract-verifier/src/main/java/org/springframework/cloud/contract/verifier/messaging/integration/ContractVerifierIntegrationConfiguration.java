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

package org.springframework.cloud.contract.verifier.messaging.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierAutoConfiguration;
import org.springframework.cloud.contract.verifier.messaging.stream.ContractVerifierStreamAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Message.class)
@AutoConfigureBefore(NoOpContractVerifierAutoConfiguration.class)
@AutoConfigureAfter(ContractVerifierStreamAutoConfiguration.class)
public class ContractVerifierIntegrationConfiguration {

	@Bean
	@ConditionalOnMissingBean(MessageVerifierSender.class)
	public MessageVerifierSender<Message<?>> integrationContractVerifierMessageSender(
			ApplicationContext applicationContext) {
		SpringIntegrationStubMessages springIntegrationStubMessages = new SpringIntegrationStubMessages(
				applicationContext);
		return new MessageVerifierSender<>() {
			@Override
			public void send(Message<?> message, String destination, @Nullable YamlContract contract) {
				springIntegrationStubMessages.send(message, destination, contract);
			}

			@Override
			public <T> void send(T payload, Map<String, Object> headers, String destination,
					@Nullable YamlContract contract) {
				springIntegrationStubMessages.send(payload, headers, destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(MessageVerifierReceiver.class)
	public MessageVerifierReceiver<Message<?>> integrationContractVerifierMessageReceiver(
			ApplicationContext applicationContext) {
		SpringIntegrationStubMessages springIntegrationStubMessages = new SpringIntegrationStubMessages(
				applicationContext);
		return new MessageVerifierReceiver<>() {
			@Override
			public Message<?> receive(String destination, long timeout, TimeUnit timeUnit,
					@Nullable YamlContract contract) {
				return springIntegrationStubMessages.receive(destination, timeout, timeUnit, contract);
			}

			@Override
			public Message<?> receive(String destination, YamlContract contract) {
				return springIntegrationStubMessages.receive(destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(ContractVerifierMessaging.class)
	public ContractVerifierMessaging<Message<?>> integrationContractVerifierMessaging(
			MessageVerifierSender<Message<?>> sender, MessageVerifierReceiver<Message<?>> receiver) {
		return new ContractVerifierHelper(sender, receiver);
	}

}

class ContractVerifierHelper extends ContractVerifierMessaging<Message<?>> {

	ContractVerifierHelper(MessageVerifierSender<Message<?>> sender, MessageVerifierReceiver<Message<?>> receiver) {
		super(sender, receiver);
	}

	@Override
	protected ContractVerifierMessage convert(Message<?> receive) {
		return new ContractVerifierMessage(receive.getPayload(), receive.getHeaders());
	}

}
