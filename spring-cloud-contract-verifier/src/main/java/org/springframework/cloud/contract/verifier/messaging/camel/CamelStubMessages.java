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

package org.springframework.cloud.contract.verifier.messaging.camel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessageMetadata;
import org.springframework.util.StringUtils;

/**
 * @author Marcin Grzejszczak
 */
public class CamelStubMessages implements MessageVerifier<Message> {

	private static final Logger log = LoggerFactory.getLogger(CamelStubMessages.class);

	private final CamelContext context;

	private final ProducerTemplate producerTemplate;

	private final ConsumerTemplate consumerTemplate;

	private final ContractVerifierCamelMessageBuilder builder;

	public CamelStubMessages(CamelContext context, ProducerTemplate producerTemplate,
			ConsumerTemplate consumerTemplate) {
		this.context = context;
		this.producerTemplate = producerTemplate;
		this.consumerTemplate = consumerTemplate;
		this.builder = new ContractVerifierCamelMessageBuilder(context);
	}

	@Override
	public void send(Message message, String destination, YamlContract contract) {
		try {
			Exchange exchange = new DefaultExchange(this.context);
			exchange.setIn(message);
			StandaloneMetadata standaloneMetadata = StandaloneMetadata
					.fromMetadata(contract != null ? contract.metadata : null);
			ContractVerifierMessageMetadata verifierMessageMetadata = ContractVerifierMessageMetadata
					.fromMetadata(contract != null ? contract.metadata : null);
			String finalDestination = finalDestination(destination,
					additionalOptions(verifierMessageMetadata, standaloneMetadata), verifierMessageMetadata);
			log.info("Will send a message to URI [" + finalDestination + "]");
			this.producerTemplate.send(finalDestination, exchange);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] "
					+ "to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	private String additionalOptions(ContractVerifierMessageMetadata verifierMessageMetadata,
			StandaloneMetadata metadata) {
		return verifierMessageMetadata.getMessageType() == ContractVerifierMessageMetadata.MessageType.INPUT
				? metadata.getInput().getAdditionalOptions() : metadata.getOutputMessage().getAdditionalOptions();
	}

	public String finalDestination(String destination, String additionalOpts,
			ContractVerifierMessageMetadata verifierMessageMetadata) {
		String finalDestination = destination;
		if (verifierMessageMetadata.getMessageType() == ContractVerifierMessageMetadata.MessageType.SETUP) {
			return finalDestination;
		}
		if (StringUtils.hasText(additionalOpts)) {
			finalDestination = finalDestination + "?" + additionalOpts;
		}
		return finalDestination;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		send(this.builder.create(payload, headers), destination, contract);
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		try {
			StandaloneMetadata standaloneMetadata = StandaloneMetadata
					.fromMetadata(contract != null ? contract.metadata : null);
			ContractVerifierMessageMetadata verifierMessageMetadata = ContractVerifierMessageMetadata
					.fromMetadata(contract != null ? contract.metadata : null);
			String finalDestination = finalDestination(destination,
					additionalOptions(verifierMessageMetadata, standaloneMetadata), verifierMessageMetadata);
			log.info("Will receive a message from URI [" + finalDestination + "]");
			Exchange exchange = this.consumerTemplate.receive(finalDestination, timeUnit.toMillis(timeout));
			return exchange != null ? exchange.getIn() : null;
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " + " a channel with name [" + destination
					+ "]", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Message receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

}
