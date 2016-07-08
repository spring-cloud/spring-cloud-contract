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

package org.springframework.cloud.contract.verifier.messaging.camel;

import java.util.Map;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage;

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageBuilder;

/**
 * @author Marcin Grzejszczak
 */
public class ContractVerifierCamelMessageBuilder<T> implements
		ContractVerifierMessageBuilder<T, Message> {

	@Override
	public ContractVerifierMessage<T, Message> create(T payload, Map<String, Object> headers) {
		DefaultMessage message = new DefaultMessage();
		message.setBody(payload);
		message.setHeaders(headers);
		return new CamelMessage<>(message);
	}

	@Override
	public ContractVerifierMessage<T, Message> create(Message message) {
		if (message == null) {
			return null;
		}
		return new CamelMessage<>(message);
	}
}
