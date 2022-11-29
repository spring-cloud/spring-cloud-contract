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
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.messaging.Message;

/**
 * @author Marcin Grzejszczak
 */
public class StreamStubMessages implements MessageVerifierSender<Message<?>>, MessageVerifierReceiver<Message<?>> {

	private final MessageVerifierSender<Message<?>> sender;

	private final MessageVerifierReceiver<Message<?>> receiver;

	public StreamStubMessages(MessageVerifierSender<Message<?>> sender, MessageVerifierReceiver<Message<?>> receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
		this.sender.send(payload, headers, destination, contract);
	}

	@Override
	public void send(Message<?> message, String destination, YamlContract contract) {
		this.sender.send(message, destination, contract);
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		return this.receiver.receive(destination, timeout, timeUnit, contract);
	}

	@Override
	public Message<?> receive(String destination, YamlContract contract) {
		return this.receiver.receive(destination, contract);
	}

}
