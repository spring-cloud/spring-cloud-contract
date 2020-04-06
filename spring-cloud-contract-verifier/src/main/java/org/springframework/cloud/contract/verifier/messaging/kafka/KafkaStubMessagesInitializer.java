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

import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

/**
 * Logic used to initialize {@link KafkaStubMessages}. This interface might have a
 * different implementation for the producer side and for the consumer side. That's
 * because you can't poll for a single message by different consumers.
 *
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public interface KafkaStubMessagesInitializer {

	/**
	 * @param broker - embedded Kafka broker
	 * @param kafkaProperties - kafka properties
	 * @return topic to initialized consumer mapping
	 */
	Map<String, Consumer> initialize(EmbeddedKafkaBroker broker,
			KafkaProperties kafkaProperties);

}
