/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.messaging.amqp;

import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.SimpleAmqpHeaderMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.MessagingMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.integration.ContractVerifierIntegrationConfiguration;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.stream.ContractVerifierStreamAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration setting up {@link MessageVerifier} for use with plain spring-rabbit/spring-amqp
 *
 * @author Mathias Düsterhöft
 * @since 1.0.1
 */
@Configuration
@ConditionalOnClass({Message.class, RabbitTemplate.class, Mockito.class})
@ConditionalOnMissingClass("org.springframework.integration.core.MessageSource")
@AutoConfigureBefore(ContractVerifierIntegrationConfiguration.class)
@AutoConfigureAfter(ContractVerifierStreamAutoConfiguration.class)
public class ContractVerifierAmqpAutoConfiguration {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Autowired(required = false)
	private MessageListenerAdapter messageListenerAdapter;

	@Bean
	@ConditionalOnMissingBean
	public MessageVerifier<Message> contractVerifierMessageExchange() {
		return new SpringAmqpStubMessages(this.rabbitTemplate, this.messageListenerAdapter);
	}

	@Bean
	@ConditionalOnMissingBean
	public ContractVerifierMessaging<Message> contractVerifierMessaging(
			MessageVerifier<Message> exchange) {
		return new ContractVerifierHelper(exchange, this.rabbitTemplate.getMessageConverter());
	}
}

class ContractVerifierHelper extends ContractVerifierMessaging<Message> {

	private final MessageConverter messageConverter;

	public ContractVerifierHelper(MessageVerifier<Message> exchange, MessageConverter messageConverter) {
		super(exchange);
		this.messageConverter = messageConverter;
	}

	@Override
	protected ContractVerifierMessage convert(Message message) {
		MessagingMessageConverter messageConverter = new MessagingMessageConverter(this.messageConverter, new SimpleAmqpHeaderMapper());
		org.springframework.messaging.Message<?> messagingMessage = (org.springframework.messaging.Message<?>) messageConverter.fromMessage(message);
		return new ContractVerifierMessage(messagingMessage.getPayload(), messagingMessage.getHeaders());
	}
}

