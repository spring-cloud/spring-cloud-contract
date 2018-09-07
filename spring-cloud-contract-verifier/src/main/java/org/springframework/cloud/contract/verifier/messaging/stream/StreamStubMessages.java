/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

/**
 * @author Marcin Grzejszczak
 */
public class StreamStubMessages implements MessageVerifier<Message<?>> {

	private static final Logger log = LoggerFactory.getLogger(StreamStubMessages.class);

	private final ApplicationContext context;
	private final MessageCollector messageCollector;
	private final ContractVerifierStreamMessageBuilder builder = new ContractVerifierStreamMessageBuilder();

	public StreamStubMessages(ApplicationContext context) {
		this.context = context;
		this.messageCollector = context.getBean(MessageCollector.class);
	}

	@Override
	public <T> void send(T payload, Map<String, Object> headers, String destination) {
		send(this.builder.create(payload, headers), destination);
	}

	@Override
	public void send(Message<?> message, String destination) {
		try {
			MessageChannel messageChannel = this.context
					.getBean(resolvedDestination(destination, DefaultChannels.OUTPUT), MessageChannel.class);
			messageChannel.send(message);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message
					+ "] " + "to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	public Message<?> receive(String destination, long timeout, TimeUnit timeUnit) {
		try {
			MessageChannel messageChannel = this.context
					.getBean(resolvedDestination(destination, DefaultChannels.INPUT), MessageChannel.class);
			return this.messageCollector.forChannel(messageChannel).poll(timeout, timeUnit);
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to read a message from "
					+ " a channel with name [" + destination + "]", e);
			throw new IllegalStateException(e);
		}
	}

	private String resolvedDestination(String destination, DefaultChannels defaultChannel) {
		try {
			BindingServiceProperties channelBindingServiceProperties = this.context
					.getBean(BindingServiceProperties.class);
			Map<String, String> channels = new HashMap<>();
			for (Map.Entry<String, BindingProperties> entry : channelBindingServiceProperties
					.getBindings().entrySet()) {
				if (destination.equals(entry.getValue().getDestination())) {
					if (log.isDebugEnabled()) {
						log.debug("Found a channel named [{}] with destination [{}]",
								entry.getKey(), destination);
					}
					channels.put(entry.getKey().toLowerCase(), destination);
				}
			}
			if (channels.size() == 1) {
				return channels.keySet().iterator().next();
			} else if (channels.size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("Found following channels [{}] for destination [{}]. "
									+ "Will pick the one that matches the default channel name or the first one if none is matching",
							channels, destination);
				}
				String defaultChannelName = channels.get(defaultChannel.name().toLowerCase());
				String matchingChannelName = StringUtils.hasText(defaultChannelName) ? defaultChannel.name().toLowerCase() : channels.keySet().iterator().next();
				if (log.isDebugEnabled()) {
					log.debug("Picked channel name is [{}]", matchingChannelName);
				}
				return matchingChannelName;
			}
		} catch (Exception e) {
			log.error("Exception took place while trying to resolve the destination. Will assume the name [" + destination + "]", e);
		}
		if (log.isDebugEnabled()) {
			log.debug(
					"No destination named [" + destination + "] was found. Assuming that the destination equals the channel name",
					destination);
		}
		return destination;
	}

	@Override
	public Message<?> receive(String destination) {
		return receive(destination, 5, TimeUnit.SECONDS);
	}

}


enum DefaultChannels {
	INPUT, OUTPUT
}