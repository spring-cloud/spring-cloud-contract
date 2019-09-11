/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

class KafkaStubMessages implements MessageVerifier<Message<?>> {

	private final KafkaTemplate kafkaTemplate;

	private final EmbeddedKafkaBroker broker;

	private final KafkaProperties kafkaProperties;

	KafkaStubMessages(KafkaTemplate kafkaTemplate, EmbeddedKafkaBroker broker, KafkaProperties kafkaProperties) {
		this.kafkaTemplate = kafkaTemplate;
		this.broker = broker;
		this.kafkaProperties = kafkaProperties;
	}

	@Override
	public void send(Message<?> message, String destination) {
		withConsumer(consumer -> {
			String defaultTopic = this.kafkaTemplate.getDefaultTopic();
			try {
				this.kafkaTemplate.setDefaultTopic(destination);
				this.kafkaTemplate.send(message).get(5, TimeUnit.SECONDS);
				this.kafkaTemplate.flush();
				return message;
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
			finally {
				this.kafkaTemplate.setDefaultTopic(defaultTopic);
			}
		});
	}

	private Message<?> withConsumer(
			java.util.function.Function<Consumer, Message<?>> lambda) {
		Map<String, Object> props = new HashMap<>();
		props.put(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				broker.getBrokersAsString());
		props.put(
				ConsumerConfig.GROUP_ID_CONFIG,
				kafkaProperties.getConsumer().getGroupId());
		props.put(
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				JsonDeserializer.class);
		props.put(
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				JsonDeserializer.class);
		ConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(
				props);
		try (Consumer<Integer, String> consumer = cf.createConsumer()) {
			this.broker.consumeFromAllEmbeddedTopics(consumer);
			return lambda.apply(consumer);
		}
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit) {
		return withConsumer(consumer -> {
			ConsumerRecord<Integer, String> reply = KafkaTestUtils
					.getSingleRecord(consumer, destination);
			Headers headers = reply.headers();
			String payload = reply.value();
			return MessageBuilder.createMessage(payload, headers(headers));
		});
	}

	private MessageHeaders headers(Headers headers) {
		Map<String, Object> map = new HashMap();
		for (Header header : headers) {
			map.put(header.key(), header.value());
		}
		return new MessageHeaders(map);
	}

	@Override
	public Message receive(String destination) {
		return receive(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	public void send(Object payload, Map headers, String destination) {
		Message<?> message = MessageBuilder.createMessage(payload,
				new MessageHeaders(headers));
		send(message, destination);
	}

}