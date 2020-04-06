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

import java.util.concurrent.TimeUnit;

/**
 * Core interface that allows you to receive messages.
 *
 * Destination is relevant to the underlying implementation. Might be a channel, queue,
 * topic etc.
 *
 * @param <M> message type
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public interface MessageVerifierReceiver<M> {

	/**
	 * Receives the message from the given destination. You can provide the timeout for
	 * receiving that message.
	 * @param destination destination from which the message will be received
	 * @param timeout timeout to wait for the message
	 * @param timeUnit param to define the unit of timeout
	 * @return received message
	 */
	M receive(String destination, long timeout, TimeUnit timeUnit);

	/**
	 * Receives the message from the given destination. A default timeout will be applied.
	 * @param destination destination from which the message will be received
	 * @return received message
	 */
	M receive(String destination);

}
