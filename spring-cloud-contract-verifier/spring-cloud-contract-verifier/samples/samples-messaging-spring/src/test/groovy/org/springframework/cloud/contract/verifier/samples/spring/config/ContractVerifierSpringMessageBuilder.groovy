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

package org.springframework.cloud.contract.verifier.samples.spring.config

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageBuilder
import org.springframework.jms.core.MessageCreator

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session
/**
 * @author Marcin Grzejszczak
 */
public class ContractVerifierSpringMessageBuilder<T extends Serializable> implements ContractVerifierMessageBuilder<T, Message> {

	@Override
	public ContractVerifierMessage<T, Message> create(final T payload, final Map<String, Object> headers) {
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createObjectMessage(payload);
				headers.entrySet().each { Map.Entry<String, Object> entry ->
					message.setObjectProperty(entry.key, entry.value)
				}
				return message
			}
		};

		return new SpringMessage<>(messageCreator)
	}

	@Override
	public ContractVerifierMessage<T, Message> create(Message message) {
		return new SpringMessage<>(message);
	}
}
