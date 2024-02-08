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

import javax.annotation.Nullable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;

/**
 * Wrapper around messaging. Abstracts all message related operations like sending,
 * converting and receiving. Delegates the actual work to an implementation of a
 * {@link MessageVerifierSender} and {@link MessageVerifierReceiver}.
 *
 * @param <M> message type
 * @author Dave Syer
 */
public class ContractVerifierMessaging<M> {

	private static final Log log = LogFactory.getLog(ContractVerifierMessaging.class);

	private final MessageVerifierSender<M> sender;

	private final MessageVerifierReceiver<M> receiver;

	public ContractVerifierMessaging(MessageVerifierSender<M> sender, MessageVerifierReceiver<M> receiver) {
		this.sender = sender;
		this.receiver = receiver;
		if (sender != null) {
			log.info("The message verifier sender implementation is of type [" + sender.getClass() + "]");
		}
		if (receiver != null) {
			log.info("The message verifier receiver implementation is of type [" + receiver.getClass() + "]");
		}
	}

	public void send(ContractVerifierMessage message, String destination, @Nullable YamlContract contract) {
		if (contract != null) {
			setMessageType(contract, ContractVerifierMessageMetadata.MessageType.INPUT);
		}
		this.sender.send(message.getPayload(), message.getHeaders(), destination, contract);
	}

	public void send(ContractVerifierMessage message, String destination) {
		send(message, destination, null);
	}

	public ContractVerifierMessage receive(String destination, @Nullable YamlContract contract) {
		if (contract != null) {
			setMessageType(contract, ContractVerifierMessageMetadata.MessageType.OUTPUT);
		}
		return convert(this.receiver.receive(destination, contract));
	}

	private void setMessageType(YamlContract contract, ContractVerifierMessageMetadata.MessageType output) {
		contract.metadata.put(ContractVerifierMessageMetadata.METADATA_KEY,
				new ContractVerifierMessageMetadata(output));
	}

	public ContractVerifierMessage receive(String destination) {
		return receive(destination, null);
	}

	public <T> ContractVerifierMessage create(T payload, Map<String, Object> headers) {
		return new ContractVerifierMessage(payload, headers);
	}

	protected ContractVerifierMessage convert(M receive) {
		return new ContractVerifierMessage(receive, null);
	}

}
