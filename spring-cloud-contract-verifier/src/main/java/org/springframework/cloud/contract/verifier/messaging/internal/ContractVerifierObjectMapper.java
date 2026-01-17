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

package org.springframework.cloud.contract.verifier.messaging.internal;

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.json.JsonMapper;

/**
 * Wrapper over {@link JsonMapper} that won't try to parse String but will directly return
 * it.
 * @author Marcin Grzejszczak
 */
public class ContractVerifierObjectMapper {

	private final JsonMapper objectMapper;

	public ContractVerifierObjectMapper() {
		this(new JsonMapper());
	}

	public ContractVerifierObjectMapper(JsonMapper mapper) {
		this.objectMapper = usesAvro() ? ignoreAvroFields(mapper) : mapper;
	}

	public String writeValueAsString(Object payload) {
		if (payload instanceof String) {
			return payload.toString();
		}
		else if (payload instanceof byte[]) {
			return new String((byte[]) payload);
		}
		return this.objectMapper.writeValueAsString(payload);
	}

	public byte[] writeValueAsBytes(Object payload) {
		if (payload instanceof String) {
			return payload.toString().getBytes();
		}
		else if (payload instanceof byte[]) {
			return (byte[]) payload;
		}
		return this.objectMapper.writeValueAsBytes(payload);
	}

	private static boolean usesAvro() {
		return ClassUtils.isPresent("org.apache.avro.specific.SpecificRecordBase", null);
	}

	private static JsonMapper ignoreAvroFields(JsonMapper mapper) {
		try {
			return mapper.rebuild().addMixIn(
					ClassUtils.forName("org.apache.avro.specific.SpecificRecordBase",
							null), IgnoreAvroMixin.class).build();
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
	interface IgnoreAvroMixin {
	}

}
