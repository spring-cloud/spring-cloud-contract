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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

class KafkaStubMessages implements MessageVerifier<Message<?>> {

	private static final Log log = LogFactory.getLog(KafkaStubMessages.class);

	private final KafkaTemplate kafkaTemplate;

	private final EmbeddedKafkaBroker broker;

	private final Map<String, Receiver> receivers = new HashMap<>();

	KafkaStubMessages(KafkaTemplate kafkaTemplate, EmbeddedKafkaBroker broker) {
		this.kafkaTemplate = kafkaTemplate;
		this.broker = broker;
		for (String topic : this.broker.getTopics()) {
			Receiver receiver = new Receiver(broker);
			receiver.prepareListener(topic);
			this.receivers.put(topic, receiver);
		}
	}

	@Override
	public void send(Message<?> message, String destination) {
		String defaultTopic = this.kafkaTemplate.getDefaultTopic();
		try {
			this.kafkaTemplate.setDefaultTopic(destination);
			if (log.isDebugEnabled()) {
				log.debug("Will send a message [" + message + "] to destination ["
						+ destination + "]");
			}
			this.kafkaTemplate.send(message).get(5, TimeUnit.SECONDS);
			this.kafkaTemplate.flush();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		finally {
			this.kafkaTemplate.setDefaultTopic(defaultTopic);
		}
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit) {
		return this.receivers.getOrDefault(destination, new Receiver(this.broker))
				.receive(destination, timeout, timeUnit);
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

class Receiver {

	private final EmbeddedKafkaBroker broker;

	private static final Map<String, Consumer> CONSUMER_CACHE = new ConcurrentHashMap<>();

	private static final Log log = LogFactory.getLog(Receiver.class);

	Receiver(EmbeddedKafkaBroker broker) {
		this.broker = broker;
	}

	void prepareListener(String destination) {
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("sender",
				"false", broker);
		DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
				consumerProperties);
		Consumer<String, String> consumer = consumerFactory.createConsumer();
		consumer.subscribe(Collections.singleton(destination));
		CONSUMER_CACHE.put(destination, consumer);
		if (log.isDebugEnabled()) {
			log.debug("Prepared consumer for destination [" + destination + "]");
		}
	}

	Message receive(String topic, long timeout, TimeUnit timeUnit) {
		Consumer consumer = CONSUMER_CACHE.get(topic);
		if (consumer == null) {
			throw new IllegalStateException(
					"No consumer set up for topic [" + topic + "]");
		}
		ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer,
				topic, timeUnit.toMillis(timeout));
		if (log.isDebugEnabled()) {
			log.debug("Got a single record for destination [" + topic + "]");
		}
		return new Record(record).toMessage();
	}

}

class Record {

	private final ConsumerRecord record;

	Record(ConsumerRecord record) {
		this.record = record;
	}

	private Map<String, Object> toMap(Headers headers) {
		Map<String, Object> map = new HashMap<>();
		for (Header header : headers) {
			map.put(header.key(), header.value());
		}
		return map;
	}

	Message toMessage() {
		Object textPayload = record.value();
		// sometimes it's a message sometimes just payload
		MessageHeaders headers = new MessageHeaders(toMap(record.headers()));
		if (textPayload instanceof String && ((String) textPayload).contains("payload")
				&& ((String) textPayload).contains("headers")) {
			try {
				Object object = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE)
						.parse((String) textPayload);
				JSONObject jo = (JSONObject) object;
				String payload = (String) jo.get("payload");
				JSONObject headersInJson = (JSONObject) jo.get("headers");
				Map newHeaders = new HashMap(headers);
				newHeaders.putAll(headersInJson);
				return MessageBuilder.createMessage(unquoted(payload),
						new MessageHeaders(newHeaders));
			}
			catch (ParseException ex) {
				throw new IllegalStateException(ex);
			}
		}
		return MessageBuilder.createMessage(unquoted(textPayload), headers);
	}

	private Object unquoted(Object value) {
		String textPayload = value instanceof byte[] ? new String((byte[]) value)
				: value.toString();
		if (textPayload.startsWith("\"") && textPayload.endsWith("\"")) {
			return textPayload.substring(1, textPayload.length() - 1).replace("\\\"",
					"\"");
		}
		return textPayload;
	}

}
