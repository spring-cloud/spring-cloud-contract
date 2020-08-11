/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.internal;

import java.util.Map;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * Wrapper around messaging. Abstracts all message related operations like sending,
 * converting and receiving. Delegates the actual work to an implementation of a
 * {@link MessageVerifier}.
 *
 * @param <M> message type
 * @author Dave Syer
 */
public class ContractVerifierMessaging<M> {

	private final MessageVerifier<M> exchange;

	public ContractVerifierMessaging(MessageVerifier<M> exchange) {
		this.exchange = exchange;
	}

	public void send(ContractVerifierMessage message, String destination,
			YamlContract contract) {
		this.exchange.send(message.getPayload(), message.getHeaders(), destination,
				contract);
	}

	public ContractVerifierMessage receive(String destination, YamlContract contract) {
		return convert(this.exchange.receive(destination, contract));
	}

	public <T> ContractVerifierMessage create(T payload, Map<String, Object> headers) {
		return new ContractVerifierMessage(payload, headers);
	}

	protected ContractVerifierMessage convert(M receive) {
		return new ContractVerifierMessage(receive, null);
	}

}
