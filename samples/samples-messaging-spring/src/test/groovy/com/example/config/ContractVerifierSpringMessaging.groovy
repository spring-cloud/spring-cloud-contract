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

package com.example.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageBuilder
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

import javax.jms.Message
import java.util.concurrent.TimeUnit
/**
 * @author Marcin Grzejszczak
 */
@Component
public class ContractVerifierSpringMessaging<T> implements ContractVerifierMessaging<T, Message> {

	private static final Logger log = LoggerFactory.getLogger(ContractVerifierSpringMessaging.class);

	private final JmsTemplate jmsTemplate;
	private final ContractVerifierMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public ContractVerifierSpringMessaging(ContractVerifierMessageBuilder builder, JmsTemplate jmsTemplate) {
		this.builder = builder
		this.jmsTemplate = jmsTemplate
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	public void send(ContractVerifierMessage<T, Message> message, String destination) {
		try {
			jmsTemplate.send(destination, ((SpringMessage) message).messageCreator)
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			return builder.create(jmsTemplate.receive(destination));
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ContractVerifierMessage<T, Message> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message> create(Message message) {
		return builder.create(message);
	}
}
