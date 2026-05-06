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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jspecify.annotations.Nullable;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.kafka.KafkaMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * A {@link MessageVerifierSender} that Avro-serializes the contract payload before
 * sending it to a Kafka topic. The schema is read from {@link AvroMetadata} stored under
 * the {@code "avro"} key in the contract metadata. Missing or invalid configuration
 * throws an exception rather than silently skipping the send.
 *
 * <p>
 * The {@link KafkaTemplate} provided at construction time must be configured with
 * {@code KafkaAvroSerializer} as its value serializer, pointing to the Schema Registry
 * URL declared via {@code spring.cloud.contract.avro.schema-registry-url}. When using
 * Spring Boot auto-configuration this is handled automatically.
 *
 * @author Emanuel Trandafir
 * @since 4.2.0
 */
public class KafkaAvroMessageVerifierSender implements MessageVerifierSender<Object> {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public KafkaAvroMessageVerifierSender(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void send(Object message, String destination, @Nullable YamlContract contract) {
		send(message, Map.of(), destination, contract);
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, @Nullable YamlContract contract) {
		if (contract == null || contract.metadata == null) {
			throw new IllegalArgumentException(
					"Contract or its metadata is null — cannot perform Avro serialization for destination ["
							+ destination + "]");
		}
		AvroMetadata avroMetadata = KafkaMetadata.fromMetadata(contract.metadata).getAvro();
		if (avroMetadata.getSchema() == null) {
			throw new IllegalArgumentException(
					"No Avro schema configured in contract metadata — cannot perform Avro serialization for destination ["
							+ destination + "]");
		}
		try {
			Schema schema = parseSchema(avroMetadata.getSchema());
			GenericRecord record = buildRecord(schema, payload);
			ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(destination, record);
			if (headers != null) {
				headers.forEach((key, value) -> producerRecord.headers()
					.add(key, value.toString().getBytes(StandardCharsets.UTF_8)));
			}
			this.kafkaTemplate.send(producerRecord);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to load Avro schema [" + avroMetadata.getSchema() + "]", ex);
		}
	}

	private Schema parseSchema(String schemaValue) throws IOException {
		if (schemaValue.trim().startsWith("{")) {
			return new Schema.Parser().parse(schemaValue);
		}
		InputStream inputStream;
		if (schemaValue.startsWith("classpath:")) {
			String path = schemaValue.substring("classpath:".length());
			inputStream = new ClassPathResource(path).getInputStream();
		}
		else {
			inputStream = new FileSystemResource(schemaValue).getInputStream();
		}
		try (InputStream is = inputStream) {
			return new Schema.Parser().parse(is);
		}
	}

	@SuppressWarnings("unchecked")
	private GenericRecord buildRecord(Schema schema, Object payload) {
		if (!(payload instanceof Map)) {
			throw new IllegalArgumentException(
					"Payload must be a Map to build a GenericRecord, got: " + payload.getClass());
		}
		Map<String, Object> payloadMap = (Map<String, Object>) payload;
		GenericRecordBuilder builder = new GenericRecordBuilder(schema);
		schema.getFields()
			.stream()
			.filter(field -> payloadMap.containsKey(field.name()))
			.forEach(field -> builder.set(field, payloadMap.get(field.name())));
		return builder.build();
	}

}
