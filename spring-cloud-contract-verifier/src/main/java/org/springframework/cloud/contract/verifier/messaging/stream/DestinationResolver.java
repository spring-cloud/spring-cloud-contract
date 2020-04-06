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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

class DestinationResolver {

	private static final Log log = LogFactory.getLog(DestinationResolver.class);

	private final ApplicationContext context;

	DestinationResolver(ApplicationContext context) {
		this.context = context;
	}

	String resolvedDestination(String destination, DefaultChannels defaultChannel) {
		try {
			BindingServiceProperties channelBindingServiceProperties = this.context
					.getBean(BindingServiceProperties.class);
			Map<String, String> channels = new HashMap<>();
			for (Map.Entry<String, BindingProperties> entry : channelBindingServiceProperties
					.getBindings().entrySet()) {
				if (destination.equals(entry.getValue().getDestination())) {
					if (log.isDebugEnabled()) {
						log.debug("Found a channel named [" + entry.getKey()
								+ "] with destination [" + destination + "]");
					}
					channels.put(entry.getKey(), destination);
				}
			}
			if (channels.size() == 1) {
				return channels.keySet().iterator().next();
			}
			else if (channels.size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("Found following channels [" + channels
							+ "] for destination [" + destination + "]. "
							+ "Will pick the one that matches the default channel name or the first one if none is matching");
				}
				String defaultChannelName = channels
						.get(defaultChannel.name().toLowerCase());
				String matchingChannelName = StringUtils.hasText(defaultChannelName)
						? defaultChannel.name().toLowerCase()
						: channels.keySet().iterator().next();
				if (log.isDebugEnabled()) {
					log.debug("Picked channel name is [" + matchingChannelName + "]");
				}
				return matchingChannelName;
			}
		}
		catch (Exception e) {
			log.error(
					"Exception took place while trying to resolve the destination. Will assume the name ["
							+ destination + "]",
					e);
		}
		if (log.isDebugEnabled()) {
			log.debug("No destination named [" + destination
					+ "] was found. Assuming that the destination equals the channel name");
		}
		return destination;
	}

}
