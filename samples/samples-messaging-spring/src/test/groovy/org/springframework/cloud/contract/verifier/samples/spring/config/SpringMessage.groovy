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
import org.springframework.jms.core.MessageCreator

import javax.jms.Message
import javax.jms.ObjectMessage
/**
 * @author Marcin Grzejszczak
 */
public class SpringMessage<T> implements ContractVerifierMessage<T, Message> {

	private final ObjectMessage messageDelegate;
	final MessageCreator messageCreator;

	public SpringMessage(Message messageDelegate) {
		this.messageDelegate = (ObjectMessage) messageDelegate;
		this.messageCreator = null
	}

	public SpringMessage(MessageCreator messageCreator) {
		this.messageDelegate = null;
		this.messageCreator = messageCreator
	}

	@Override
	public T getPayload() {
		return messageDelegate.getObject();
	}

	@Override
	public Map<String, Object> getHeaders() {
		Map<String, Object> headers = [:]
		def enumeration = messageDelegate.propertyNames
		while(enumeration.hasMoreElements()) {
			String propertyName = enumeration.nextElement()
			headers << [(propertyName) : messageDelegate.getObjectProperty(propertyName)]
		}
		return headers
	}

	@Override
	Object getHeader(String key) {
		return getHeaders().get(key)
	}

	@Override
	public Message convert() {
		return messageDelegate;
	}

}
