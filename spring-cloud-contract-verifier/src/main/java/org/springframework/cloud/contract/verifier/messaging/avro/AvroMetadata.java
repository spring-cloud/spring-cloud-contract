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

/**
 * Avro serialization metadata for a Kafka contract message.
 *
 * <p>
 * Example contract YAML:
 * <pre>
 * metadata:
 *   kafka:
 *     avro:
 *       schema: classpath:avro/Book.avsc
 * </pre>
 *
 * <p>
 * The Schema Registry URL is configured globally via
 * {@code spring.cloud.contract.avro.schema-registry-url}.
 *
 * @author Emanuel Trandafir
 * @since 4.2.0
 */
public class AvroMetadata {

	/**
	 * Classpath or filesystem path to the Avro schema file ({@code .avsc}), e.g.
	 * {@code classpath:avro/Book.avsc}. May also be an inline JSON schema string.
	 */
	private String schema;

	public String getSchema() {
		return this.schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

}
