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

package org.springframework.cloud.contract.verifier.messaging.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

class KafkaStubMessages implements MessageVerifier<Message<?>> {

	private static final Log log = LogFactory.getLog(KafkaStubMessages.class);

	final KafkaTemplate kafkaTemplate;

	private final Receiver receiver;

	KafkaStubMessages(KafkaTemplate kafkaTemplate, EmbeddedKafkaBroker broker, KafkaProperties kafkaProperties,
			KafkaStubMessagesInitializer initializer) {
		this.kafkaTemplate = kafkaTemplate;
		Map<String, Consumer> topicToConsumer = initializer.initialize(broker, kafkaProperties);
		this.receiver = new Receiver(topicToConsumer);
	}

	@Override
	public void send(Message<?> message, String destination, YamlContract contract) {
		String defaultTopic = this.kafkaTemplate.getDefaultTopic();
		try {
			this.kafkaTemplate.setDefaultTopic(destination);
			if (log.isDebugEnabled()) {
				log.debug("Will send a message [" + message + "] to destination [" + destination + "]");
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
	public Message receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		return this.receiver.receive(destination, timeout, timeUnit, contract);
	}

	@Override
	public Message receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

	@Override
	public void send(Object payload, Map headers, String destination, YamlContract contract) {
		Message<?> message = MessageBuilder.createMessage(payload, new MessageHeaders(headers));
		send(message, destination, contract);
	}

}

class Receiver {

	private static final Log log = LogFactory.getLog(Receiver.class);

	private final MessagingMessageConverter messagingMessageConverter = new MessagingMessageConverter();

	private final Map<String, Consumer> consumers;

	Receiver(Map<String, Consumer> consumers) {
		this.consumers = consumers;
	}

	Message receive(String topic, long timeout, TimeUnit timeUnit, YamlContract contract) {
		Consumer consumer = this.consumers.get(topic);
		if (consumer == null) {
			throw new IllegalStateException("No consumer set up for topic [" + topic + "]");
		}
		ConsumerRecord<?, ?> record = KafkaTestUtils.getSingleRecord(consumer, topic, Duration.ofMillis(timeout));
		if (log.isDebugEnabled()) {
			log.debug("Got a single record for destination [" + topic + "]");
		}
		return toMessage(consumer, record);
	}

	Message toMessage(Consumer consumer, ConsumerRecord<?, ?> record) {
		Map<String, Object> headersMap = toMap(record.headers());

		// Leverage spring-kafka to add the headers
		messagingMessageConverter.commonHeaders(null, consumer, headersMap, record.key(), record.topic(),
				record.partition(), record.offset(),
				record.timestampType() != null ? record.timestampType().name() : null, record.timestamp());
		// commonHeaders() maps the record key under 'kafka_receivedMessageKey' - put
		// under 'kafka_messageKey' as well to satisfy both client/server usages as there
		// is not currently a way to set a header name based on client/server
		headersMap.put(KafkaHeaders.KEY, record.key());

		// TODO explore using MessagingMessageConverter to do all of the conversion
		// (ideally delete this entire method)
		Object textPayload = record.value();
		// sometimes it's a message sometimes just payload
		if (textPayload instanceof String && ((String) textPayload).contains("payload")
				&& ((String) textPayload).contains("headers")) {
			try {
				Object object = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse((String) textPayload);
				JSONObject jo = (JSONObject) object;
				String payload = (String) jo.get("payload");
				JSONObject headersInJson = (JSONObject) jo.get("headers");
				headersMap.putAll(headersInJson);
				return MessageBuilder.createMessage(unquoted(payload), new MessageHeaders(headersMap));
			}
			catch (ParseException ex) {
				throw new IllegalStateException(ex);
			}
		}
		return MessageBuilder.createMessage(unquoted(textPayload), new MessageHeaders(headersMap));
	}

	private Map<String, Object> toMap(Headers headers) {
		Map<String, Object> map = new HashMap<>();
		for (Header header : headers) {
			map.put(header.key(), header.value());
		}
		return map;
	}

	private Object unquoted(Object value) {
		String textPayload = value instanceof byte[] ? new String((byte[]) value) : value.toString();
		if (textPayload.startsWith("\"") && textPayload.endsWith("\"")) {
			return textPayload.substring(1, textPayload.length() - 1).replace("\\\"", "\"");
		}
		return textPayload;
	}

}
