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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

class ContractVerifierKafkaStubMessagesInitializer
		implements KafkaStubMessagesInitializer {

	private static final Log log = LogFactory
			.getLog(ContractVerifierKafkaStubMessagesInitializer.class);

	@Override
	public Map<String, Consumer> initialize(EmbeddedKafkaBroker broker,
			KafkaProperties kafkaProperties) {
		Map<String, Consumer> map = new HashMap<>();
		for (String topic : broker.getTopics()) {
			map.put(topic, prepareListener(broker, topic, kafkaProperties));
		}
		return map;
	}

	private Consumer prepareListener(EmbeddedKafkaBroker broker, String destination,
			KafkaProperties kafkaProperties) {
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				kafkaProperties.getConsumer().getGroupId(), "false", broker);
		consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
				consumerProperties);
		Consumer<String, String> consumer = consumerFactory.createConsumer();
		broker.consumeFromAnEmbeddedTopic(consumer, destination);
		if (log.isDebugEnabled()) {
			log.debug("Prepared consumer for destination [" + destination + "]");
		}
		return consumer;
	}

}
