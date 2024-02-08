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

package org.springframework.cloud.contract.verifier.messaging.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/**
 * @author Marcin Grzejszczak
 */
public class SpringIntegrationStubMessages
		implements MessageVerifierSender<Message<?>>, MessageVerifierReceiver<Message<?>> {

	private static final Log log = LogFactory.getLog(SpringIntegrationStubMessages.class);

	private final ApplicationContext context;

	private final ContractVerifierIntegrationMessageBuilder builder = new ContractVerifierIntegrationMessageBuilder();

	public SpringIntegrationStubMessages(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		send(this.builder.create(payload, headers), destination, contract);
	}

	@Override
	public void send(Message<?> message, String destination, YamlContract contract) {
		try {
			MessageChannel messageChannel = this.context.getBean(destination, MessageChannel.class);
			messageChannel.send(message);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] "
					+ "to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		try {
			PollableChannel messageChannel = this.context.getBean(destination, PollableChannel.class);
			return messageChannel.receive(timeUnit.toMillis(timeout));
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " + " a channel with name [" + destination
					+ "]", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Message<?> receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

}
