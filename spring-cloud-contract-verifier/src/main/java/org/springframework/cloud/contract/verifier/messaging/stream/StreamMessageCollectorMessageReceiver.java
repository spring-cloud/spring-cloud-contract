/*
 * Copyright 2013-2019 the original author or authors.
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

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

class StreamMessageCollectorMessageReceiver
		implements MessageVerifierReceiver<Message<?>> {

	private static final Log log = LogFactory
			.getLog(StreamMessageCollectorMessageReceiver.class);

	private final DestinationResolver resolver;

	private MessageCollector messageCollector;

	private final ApplicationContext context;

	StreamMessageCollectorMessageReceiver(DestinationResolver resolver,
			ApplicationContext context) {
		this.resolver = resolver;
		this.context = context;
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit) {
		try {
			MessageChannel messageChannel = this.context.getBean(
					this.resolver.resolvedDestination(destination, DefaultChannels.INPUT),
					MessageChannel.class);
			return messageCollector().forChannel(messageChannel).poll(timeout, timeUnit);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from "
					+ " a channel with name [" + destination + "]", e);
			throw new IllegalStateException(e);
		}
	}

	private MessageCollector messageCollector() {
		if (this.messageCollector == null) {
			this.messageCollector = context.getBean(MessageCollector.class);
		}
		return this.messageCollector;
	}

	@Override
	public Message<?> receive(String destination) {
		return receive(destination, 5, TimeUnit.SECONDS);
	}

}
