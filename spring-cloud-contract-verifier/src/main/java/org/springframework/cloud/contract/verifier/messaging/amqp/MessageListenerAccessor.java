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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Abstraction hiding details of the different sources of message listeners.
 *
 * Needed because
 * {@link org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor}
 * adds the listeners to the {@link RabbitListenerEndpointRegistry} so that the registry
 * is empty when wired into an auto configuration class so we wrap it in the accessor to
 * access the listeners late at runtime.
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
class MessageListenerAccessor {

	private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

	private final List<SimpleMessageListenerContainer> simpleMessageListenerContainers;

	private final List<Binding> bindings;

	MessageListenerAccessor(RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry,
			List<SimpleMessageListenerContainer> simpleMessageListenerContainers,
			List<Binding> bindings) {
		this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
		this.simpleMessageListenerContainers = simpleMessageListenerContainers;
		this.bindings = bindings;
	}

	List<SimpleMessageListenerContainer> getListenerContainersForDestination(
			String destination, String routingKey) {
		List<SimpleMessageListenerContainer> listenerContainers = collectListenerContainers();
		// we interpret the destination as exchange name and collect all the queues bound
		// to this exchange
		Set<String> queueNames = collectQueuesBoundToDestination(destination, routingKey);
		return getListenersByBoundQueues(listenerContainers, queueNames);
	}

	private List<SimpleMessageListenerContainer> getListenersByBoundQueues(
			List<SimpleMessageListenerContainer> listenerContainers,
			Set<String> queueNames) {
		List<SimpleMessageListenerContainer> matchingContainers = new ArrayList<>();
		for (SimpleMessageListenerContainer listenerContainer : listenerContainers) {
			if (listenerContainer.getQueueNames() != null) {
				for (String queueName : listenerContainer.getQueueNames()) {
					if (queueNames.contains(queueName)) {
						matchingContainers.add(listenerContainer);
						break;
					}
				}
			}
		}
		return matchingContainers;
	}

	private Set<String> collectQueuesBoundToDestination(String destination,
			String routingKey) {
		Set<String> queueNames = new HashSet<>();
		for (Binding binding : this.bindings) {
			if (destination.equals(binding.getExchange())
					&& (routingKey == null || routingKey.equals(binding.getRoutingKey()))
					&& DestinationType.QUEUE.equals(binding.getDestinationType())) {
				queueNames.add(binding.getDestination());
			}
		}
		return queueNames;
	}

	private List<SimpleMessageListenerContainer> collectListenerContainers() {
		List<SimpleMessageListenerContainer> listenerContainers = new ArrayList<>();
		if (this.simpleMessageListenerContainers != null) {
			listenerContainers.addAll(this.simpleMessageListenerContainers);
		}
		if (this.rabbitListenerEndpointRegistry != null) {
			for (MessageListenerContainer listenerContainer : this.rabbitListenerEndpointRegistry
					.getListenerContainers()) {
				if (listenerContainer instanceof SimpleMessageListenerContainer) {
					listenerContainers
							.add((SimpleMessageListenerContainer) listenerContainer);
				}
			}
		}
		return listenerContainers;
	}

}
