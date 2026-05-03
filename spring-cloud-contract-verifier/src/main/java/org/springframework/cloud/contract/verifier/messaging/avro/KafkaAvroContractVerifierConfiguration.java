/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.avro;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration for Avro support in Spring Cloud Contract. Activates when
 * {@code org.apache.avro.specific.SpecificRecordBase} is on the classpath.
 *
 * @author Emanuel Trandafir
 * @since 4.2.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.apache.avro.specific.SpecificRecordBase")
public class KafkaAvroContractVerifierConfiguration {

	@Bean
	@ConditionalOnMissingBean
	ContractVerifierObjectMapper avroContractVerifierObjectMapper(
			ObjectProvider<JsonMapper> jsonMapper) {
		JsonMapper mapper = jsonMapper.getIfAvailable(JsonMapper::new).rebuild()
				.addMixIn(SpecificRecordBase.class, IgnoreAvroMixin.class).build();
		return new ContractVerifierObjectMapper(mapper);
	}

	@JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
	interface IgnoreAvroMixin {
	}

	@Bean
	@ConditionalOnMissingBean(name = "avroKafkaTemplate")
	KafkaTemplate<String, Object> avroKafkaTemplate(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.cloud.contract.avro.schema-registry-url}") String schemaRegistryUrl) {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
		props.put("schema.registry.url", schemaRegistryUrl);
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
	}

	@Bean
	@ConditionalOnMissingBean
	KafkaAvroMessageVerifierSender kafkaAvroMessageVerifierSender(
			@Qualifier("avroKafkaTemplate") KafkaTemplate<String, Object> avroKafkaTemplate) {
		return new KafkaAvroMessageVerifierSender(avroKafkaTemplate);
	}

}
