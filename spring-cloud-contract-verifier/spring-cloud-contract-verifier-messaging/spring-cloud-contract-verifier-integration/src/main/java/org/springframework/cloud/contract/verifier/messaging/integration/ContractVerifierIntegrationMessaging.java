/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.messaging.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.stereotype.Component;

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage;

/**
 * @author Marcin Grzejszczak
 */
@Component
public class ContractVerifierIntegrationMessaging<T> implements
		ContractVerifierMessaging<T, Message<T>> {

	private static final Logger log = LoggerFactory.getLogger(
			ContractVerifierIntegrationMessaging.class);

	private final ApplicationContext context;
	private final ContractVerifierMessageBuilder<T, Message<T>> builder;

	@Autowired
	public ContractVerifierIntegrationMessaging(ApplicationContext context, ContractVerifierMessageBuilder<T, Message<T>> contractVerifierMessageBuilder) {
		this.context = context;
		this.builder = contractVerifierMessageBuilder;
	}

	@Override
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	public void send(ContractVerifierMessage<T, Message<T>> message, String destination) {
		try {
			MessageChannel messageChannel = context.getBean(destination, MessageChannel.class);
			messageChannel.send(message.convert());
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message<T>> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			PollableChannel messageChannel = context.getBean(destination, PollableChannel.class);
			return builder.create((Message<T>) messageChannel.receive(timeUnit.toMillis(timeout)));
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ContractVerifierMessage<T, Message<T>> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	public ContractVerifierMessage<T, Message<T>> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	public ContractVerifierMessage<T, Message<T>> create(Message<T> message) {
		return builder.create(message);
	}
}
