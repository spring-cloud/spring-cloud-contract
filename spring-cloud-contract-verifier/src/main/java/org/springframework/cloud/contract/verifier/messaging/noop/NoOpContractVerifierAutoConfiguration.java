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

package org.springframework.cloud.contract.verifier.messaging.noop;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Verifier auto configuration containing default beans for messaging.
 *
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class NoOpContractVerifierAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(MessageVerifierSender.class)
	public MessageVerifierSender<Object> noOpContractVerifierMessageSender() {
		NoOpStubMessages<Object> noOpStubMessages = new NoOpStubMessages<>();
		return new MessageVerifierSender<>() {

			@Override
			public void send(Object message, String destination, @Nullable YamlContract contract) {
				noOpStubMessages.send(message, destination, contract);
			}

			@Override
			public <T> void send(T payload, Map<String, Object> headers, String destination,
					@Nullable YamlContract contract) {
				noOpStubMessages.send(payload, headers, destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(MessageVerifierReceiver.class)
	public MessageVerifierReceiver<Object> noOpContractVerifierMessageReceiver() {
		NoOpStubMessages<Object> noOpStubMessages = new NoOpStubMessages<>();
		return new MessageVerifierReceiver<>() {

			@Override
			public Object receive(String destination, long timeout, TimeUnit timeUnit,
					@Nullable YamlContract contract) {
				return noOpStubMessages.receive(destination, timeout, timeUnit, contract);
			}

			@Override
			public Object receive(String destination, YamlContract contract) {
				return noOpStubMessages.receive(destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(ContractVerifierMessaging.class)
	public ContractVerifierMessaging<Object> contractVerifierMessaging() {
		return new ContractVerifierMessaging<>(new NoOpStubMessages<>(), new NoOpStubMessages<>());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnMissingClass("org.apache.avro.specific.SpecificRecordBase")
	public ContractVerifierObjectMapper contractVerifierObjectMapper(
			ObjectProvider<JsonMapper> jsonMapper) {
		JsonMapper mapper = jsonMapper.getIfAvailable(JsonMapper::new);
		return new ContractVerifierObjectMapper(mapper);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.apache.avro.specific.SpecificRecordBase")
	public ContractVerifierObjectMapper avroContractVerifierObjectMapper(
			ObjectProvider<JsonMapper> jsonMapper) throws ClassNotFoundException {
		JsonMapper mapper = jsonMapper.getIfAvailable(JsonMapper::new).rebuild()
				.addMixIn(Class.forName("org.apache.avro.specific.SpecificRecordBase"),
						IgnoreAvroMixin.class).build();
		return new ContractVerifierObjectMapper(mapper);
	}

	@JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
	interface IgnoreAvroMixin {

	}

}
