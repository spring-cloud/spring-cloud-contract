/*
 *  Copyright 2013-2017 the original author or authors.
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.springframework.amqp.support.converter.DefaultClassMapper.DEFAULT_CLASSID_FIELD_NAME;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.util.Assert;

/**
 * {@link MessageVerifier} implementation to integrate with plain spring-amqp/spring-rabbit.
 * It is meant to be used without interacting with a running bus.
 *
 * It relies on the RabbitTemplate to be a spy to be able to capture send messages.
 *
 * Messages are not sent to the bus - but are handed over to a {@link SimpleMessageListenerContainer} which
 * allows us to test the full deserialization and listener invocation.
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
public class SpringAmqpStubMessages implements
		MessageVerifier<Message> {

	private static final Logger log = LoggerFactory.getLogger(SpringAmqpStubMessages.class);

	private final RabbitTemplate rabbitTemplate;

	private final MessageListenerAccessor messageListenerAccessor;

	@Autowired
	public SpringAmqpStubMessages(RabbitTemplate rabbitTemplate, MessageListenerAccessor messageListenerAccessor) {
		Assert.notNull(rabbitTemplate);
		Assert.isTrue(mockingDetails(rabbitTemplate).isSpy() || mockingDetails(rabbitTemplate).isMock()); //we get send messages by capturing arguments on the spy
		this.rabbitTemplate = rabbitTemplate;
		this.messageListenerAccessor = messageListenerAccessor;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination) {
		Message message = org.springframework.amqp.core.MessageBuilder
				.withBody(((String) payload).getBytes())
				.andProperties(
						MessagePropertiesBuilder.newInstance()
								.setContentType((String) headers.get("contentType"))
								.copyHeaders(headers).build())
				.build();
		if (headers != null && headers.containsKey(DEFAULT_CLASSID_FIELD_NAME)) {
			message.getMessageProperties().setHeader(DEFAULT_CLASSID_FIELD_NAME, headers.get(DEFAULT_CLASSID_FIELD_NAME));
		}
		send(message, destination);
	}

	@Override
	public void send(Message message, String destination) {
		List<SimpleMessageListenerContainer> listenerContainers = this.messageListenerAccessor.getListenerContainersForDestination(destination);
		if (listenerContainers.isEmpty()) {
			throw new IllegalStateException("no listeners found for destination " + destination);
		}
		for (SimpleMessageListenerContainer listenerContainer : listenerContainers) {
			MessageListener messageListener = (MessageListener) listenerContainer.getMessageListener();
			messageListener.onMessage(message);
		}
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit) {
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.rabbitTemplate).send(eq(destination), anyString(), messageCaptor.capture(), any(CorrelationData.class));

		if (messageCaptor.getAllValues().isEmpty()) {
			log.info("no messages found on destination {}", destination);
			return null;
		} else if (messageCaptor.getAllValues().size() > 1) {
			log.info("multiple messages found on destination {} returning last one - {}", destination);
			return messageCaptor.getValue();
		}
		return messageCaptor.getValue();
	}

	@Override
	public Message receive(String destination) {
		return receive(destination, 5, TimeUnit.SECONDS);
	}

}
