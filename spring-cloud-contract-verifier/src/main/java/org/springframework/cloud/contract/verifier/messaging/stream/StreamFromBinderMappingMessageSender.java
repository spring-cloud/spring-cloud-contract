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

package org.springframework.cloud.contract.verifier.messaging.stream;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/**
 * @author Marcin Grzejszczak
 */
class StreamFromBinderMappingMessageSender implements MessageVerifierSender<Message<?>> {

	private static final Log log = LogFactory.getLog(StreamFromBinderMappingMessageSender.class);

	private final ApplicationContext context;

	private final DestinationResolver resolver;

	private final ContractVerifierStreamMessageBuilder builder = new ContractVerifierStreamMessageBuilder();

	StreamFromBinderMappingMessageSender(ApplicationContext context, DestinationResolver resolver) {
		this.context = context;
		this.resolver = resolver;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		send(this.builder.create(payload, headers), destination, contract);
	}

	@Override
	public void send(Message<?> message, String destination, YamlContract contract) {
		try {
			MessageChannel messageChannel = this.context.getBean(
					this.resolver.resolvedDestination(destination, DefaultChannels.OUTPUT), MessageChannel.class);
			messageChannel.send(message);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] "
					+ "to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

}
