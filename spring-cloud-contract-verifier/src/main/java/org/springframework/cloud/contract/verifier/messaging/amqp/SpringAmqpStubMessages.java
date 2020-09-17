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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessageMetadata;
import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.util.Assert;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.springframework.amqp.support.converter.DefaultClassMapper.DEFAULT_CLASSID_FIELD_NAME;

/**
 * {@link MessageVerifier} implementation to integrate with plain
 * spring-amqp/spring-rabbit. It is meant to be used without interacting with a running
 * bus.
 *
 * It relies on the RabbitTemplate to be a spy to be able to capture send messages.
 *
 * Messages are not sent to the bus - but are handed over to a
 * {@link SimpleMessageListenerContainer} which allows us to test the full deserialization
 * and listener invocation.
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
public class SpringAmqpStubMessages implements MessageVerifier<Message> {

	private static final Log log = LogFactory.getLog(SpringAmqpStubMessages.class);

	private final RabbitTemplate rabbitTemplate;

	private final MessageListenerAccessor messageListenerAccessor;

	private RabbitProperties rabbitProperties;

	@Deprecated
	public SpringAmqpStubMessages(RabbitTemplate rabbitTemplate, MessageListenerAccessor messageListenerAccessor) {
		Assert.notNull(rabbitTemplate, "RabbitTemplate must be set");
		Assert.isTrue(mockingDetails(rabbitTemplate).isSpy() || mockingDetails(rabbitTemplate).isMock(),
				"StubRunner AMQP will work only if RabbiTemplate is a spy");
		this.rabbitTemplate = rabbitTemplate;
		this.messageListenerAccessor = messageListenerAccessor;
	}

	@Autowired
	public SpringAmqpStubMessages(RabbitTemplate rabbitTemplate, MessageListenerAccessor messageListenerAccessor,
			RabbitProperties rabbitProperties) {
		Assert.notNull(rabbitTemplate, "RabbitTemplate must be set");
		Assert.isTrue(mockingDetails(rabbitTemplate).isSpy() || mockingDetails(rabbitTemplate).isMock(),
				"StubRunner AMQP will work only if RabbiTemplate is a spy");
		this.rabbitTemplate = rabbitTemplate;
		this.messageListenerAccessor = messageListenerAccessor;
		this.rabbitProperties = rabbitProperties;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		Message message = org.springframework.amqp.core.MessageBuilder.withBody(((String) payload).getBytes())
				.andProperties(MessagePropertiesBuilder.newInstance().setContentType(header(headers, "contentType"))
						.copyHeaders(headers).build())
				.build();
		if (headers != null && headers.containsKey(DEFAULT_CLASSID_FIELD_NAME)) {
			message.getMessageProperties().setHeader(DEFAULT_CLASSID_FIELD_NAME,
					headers.get(DEFAULT_CLASSID_FIELD_NAME));
		}
		if (headers != null && headers.containsKey(AmqpHeaders.RECEIVED_ROUTING_KEY)) {
			message.getMessageProperties().setReceivedRoutingKey(header(headers, AmqpHeaders.RECEIVED_ROUTING_KEY));
		}
		send(message, destination, contract);
	}

	public void mergeMessagePropertiesFromMetadata(YamlContract contract, Message message) {
		if (contract != null && contract.metadata.containsKey(AmqpMetadata.METADATA_KEY)) {
			AmqpMetadata amqpMetadata = AmqpMetadata.fromMetadata(contract.metadata);
			ContractVerifierMessageMetadata messageMetadata = ContractVerifierMessageMetadata
					.fromMetadata(contract.metadata);
			boolean isInput = isInputMessage(messageMetadata);
			MessageProperties fromMetadata = isInput ? amqpMetadata.getInput().getMessageProperties()
					: amqpMetadata.getOutputMessage().getMessageProperties();
			MetadataUtil.merge(message.getMessageProperties(), fromMetadata);
		}
	}

	public boolean isInputMessage(ContractVerifierMessageMetadata messageMetadata) {
		return messageMetadata.getMessageType() == ContractVerifierMessageMetadata.MessageType.INPUT;
	}

	private String header(Map<String, Object> headers, String headerName) {
		if (headers == null) {
			return "";
		}
		Object value = headers.get(headerName);
		if (value instanceof String) {
			return (String) value;
		}
		else if (value instanceof Iterable) {
			Iterable values = ((Iterable) value);
			return values.iterator().hasNext() ? (String) values.iterator().next() : "";
		}
		return value.toString();
	}

	@Override
	public void send(Message message, String destination, YamlContract contract) {
		mergeMessagePropertiesFromMetadata(contract, message);
		final String routingKey = message.getMessageProperties().getReceivedRoutingKey();
		List<SimpleMessageListenerContainer> listenerContainers = this.messageListenerAccessor
				.getListenerContainersForDestination(destination, routingKey);
		if (listenerContainers.isEmpty()) {
			throw new IllegalStateException("no listeners found for destination " + destination);
		}
		for (SimpleMessageListenerContainer listenerContainer : listenerContainers) {
			Object messageListener = listenerContainer.getMessageListener();
			if (isChannelAwareListener(listenerContainer, messageListener)) {
				try {
					((ChannelAwareMessageListener) messageListener).onMessage(message,
							createChannel(listenerContainer, transactionalChannel()));
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				((MessageListener) messageListener).onMessage(message);
			}
		}
	}

	Channel createChannel(SimpleMessageListenerContainer listenerContainer, boolean transactional) {
		return listenerContainer.getConnectionFactory().createConnection().createChannel(transactional);
	}

	boolean isChannelAwareListener(SimpleMessageListenerContainer listenerContainer, Object messageListener) {
		return messageListener instanceof ChannelAwareMessageListener
				&& listenerContainer.getConnectionFactory() != null;
	}

	private boolean transactionalChannel() {
		if (this.rabbitProperties == null) {
			// backward compatibility
			return true;
		}
		return this.rabbitProperties.getPublisherConfirmType() == null
				|| this.rabbitProperties.getPublisherConfirmType() == CachingConnectionFactory.ConfirmType.NONE;
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
		verify(this.rabbitTemplate, atLeastOnce()).send(eq(destination), routingKeyCaptor.capture(),
				messageCaptor.capture(), ArgumentMatchers.any());
		if (messageCaptor.getAllValues().isEmpty()) {
			log.info("no messages found on destination [" + destination + "]");
			return null;
		}
		else if (messageCaptor.getAllValues().size() > 1) {
			log.info("multiple messages found on destination [" + destination + "] returning last one");
			return messageCaptor.getValue();
		}
		Message message = messageCaptor.getValue();
		if (message == null) {
			log.info("no messages found on destination [" + destination + "]");
			return null;
		}
		if (!routingKeyCaptor.getValue().isEmpty()) {
			log.info("routing key passed [" + routingKeyCaptor.getValue() + "]");
			message.getMessageProperties().setReceivedRoutingKey(routingKeyCaptor.getValue());
		}
		return message;
	}

	@Override
	public Message receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

}
