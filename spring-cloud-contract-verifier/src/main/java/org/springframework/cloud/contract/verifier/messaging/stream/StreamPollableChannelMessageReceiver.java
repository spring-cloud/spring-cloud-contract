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

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;

class StreamPollableChannelMessageReceiver implements MessageVerifierReceiver<Message<?>> {

	private static final Log log = LogFactory.getLog(StreamPollableChannelMessageReceiver.class);

	private final ApplicationContext context;

	private final DestinationResolver destinationResolver;

	private final PollableChannel messageChannel;

	StreamPollableChannelMessageReceiver(ApplicationContext context) {
		this.context = context;
		this.destinationResolver = new DestinationResolver(context);
		this.messageChannel = new QueueChannel(1);
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		MessageHandler handler = this.messageChannel::send;
		MessageChannel channel = null;
		try {
			channel = this.context.getBean(
					this.destinationResolver.resolvedDestination(destination, DefaultChannels.INPUT),
					MessageChannel.class);
			if (channel instanceof SubscribableChannel) {
				((SubscribableChannel) channel).subscribe(handler);
				return this.messageChannel.receive(timeUnit.toMillis(timeout));
			}
			else if (channel instanceof PollableChannel) {
				return ((PollableChannel) channel).receive(timeUnit.toMillis(timeout));
			}
			throw new IllegalStateException("Unsupported channel type");
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " + " a channel with name [" + destination
					+ "]", e);
			throw new IllegalStateException(e);
		}
		finally {
			if (channel instanceof SubscribableChannel) {
				((SubscribableChannel) channel).unsubscribe(handler);
			}
		}
	}

	@Override
	public Message<?> receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

}
