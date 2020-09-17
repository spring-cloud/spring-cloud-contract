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
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;

/**
 * @author Marcin Grzejszczak
 */
class StreamInputDestinationMessageSender implements MessageVerifierSender<Message<?>> {

	private static final Log log = LogFactory.getLog(StreamInputDestinationMessageSender.class);

	private final ApplicationContext context;

	private final ContractVerifierStreamMessageBuilder builder = new ContractVerifierStreamMessageBuilder();

	StreamInputDestinationMessageSender(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		send(this.builder.create(payload, headers), destination, contract);
	}

	@Override
	public void send(Message<?> message, String destination, YamlContract contract) {
		try {
			InputDestination inputDestination = this.context.getBean(InputDestination.class);
			inputDestination.send(message, destination);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] "
					+ "to a destination with name [" + destination + "]", e);
			throw e;
		}
	}

}
