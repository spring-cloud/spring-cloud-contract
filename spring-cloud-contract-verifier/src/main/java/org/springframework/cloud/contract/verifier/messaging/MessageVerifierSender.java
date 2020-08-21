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

package org.springframework.cloud.contract.verifier.messaging;

import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.cloud.contract.verifier.converter.YamlContract;

/**
 * Core interface that allows you to send messages.
 *
 * Destination is relevant to the underlying implementation. Might be a channel, queue,
 * topic etc.
 *
 * @param <M> message type
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public interface MessageVerifierSender<M> {

	/**
	 * Sends the message to the given destination.
	 * @param message to send
	 * @param destination destination to which the message will be sent
	 */
	default void send(M message, String destination) {
		send(message, destination, null);
	}

	/**
	 * Sends the given payload with headers, to the given destination.
	 * @param <T> payload type
	 * @param payload payload to send
	 * @param headers headers to send
	 * @param destination destination to which the message will be sent
	 */
	default <T> void send(T payload, Map<String, Object> headers, String destination) {
		send(payload, headers, destination, null);
	}

	/**
	 * Sends the message to the given destination.
	 * @param message to send
	 * @param destination destination to which the message will be sent
	 * @param contract contract related to this method
	 */
	void send(M message, String destination, @Nullable YamlContract contract);

	/**
	 * Sends the given payload with headers, to the given destination.
	 * @param <T> payload type
	 * @param payload payload to send
	 * @param headers headers to send
	 * @param destination destination to which the message will be sent
	 * @param contract contract related to this method
	 */
	<T> void send(T payload, Map<String, Object> headers, String destination,
			@Nullable YamlContract contract);

}
