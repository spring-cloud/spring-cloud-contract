package org.springframework.cloud.contract.verifier.messaging.amqp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Abstraction hiding details of the different sources of message listeners
 *
 * Needed because {@link org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor} adds the listners to the
 * {@link RabbitListenerEndpointRegistry} so that the registry is empty when wired into an auto configuration class
 * so we wrap it in the accessor to access the listeners late at runtime
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
public class MessageListenerAccessor {

	private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

	private final List<SimpleMessageListenerContainer> simpleMessageListenerContainers;
	private final List<Binding> bindings;

	public MessageListenerAccessor(RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry,
									List<SimpleMessageListenerContainer> simpleMessageListenerContainers, List<Binding> bindings) {
		this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
		this.simpleMessageListenerContainers = simpleMessageListenerContainers;
		this.bindings = bindings;
	}

	public List<SimpleMessageListenerContainer> getListenerContainersForDestination(String destination) {
		List<SimpleMessageListenerContainer> listenerContainers = collectListenerContainers();
		//we interpret the destination as exchange name and collect all the queues bound to this exchange
		Set<String> queueNames = collectQueuesBoundToDestination(destination);

		return getListenersByBoundQueues(listenerContainers, queueNames);
	}

	private List<SimpleMessageListenerContainer> getListenersByBoundQueues(List<SimpleMessageListenerContainer> listenerContainers, Set<String> queueNames) {
		List<SimpleMessageListenerContainer> matchingContainers = new ArrayList<>();
		for (SimpleMessageListenerContainer listenerContainer : listenerContainers) {
			if (listenerContainer.getQueueNames() != null) {
				for (String queueName :  listenerContainer.getQueueNames()) {
					if (queueNames.contains(queueName)) {
						matchingContainers.add(listenerContainer);
						break;
					}
				}
			}
		}
		return matchingContainers;
	}

	private Set<String> collectQueuesBoundToDestination(String destination) {
		Set<String> queueNames = new HashSet<>();
		for (Binding binding: this.bindings) {
			if (binding.getExchange().equals(destination) && binding.getDestinationType().equals(Binding.DestinationType.QUEUE)) {
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
			for (MessageListenerContainer listenerContainer : this.rabbitListenerEndpointRegistry.getListenerContainers()) {
				if (listenerContainer instanceof SimpleMessageListenerContainer) {
					listenerContainers.add((SimpleMessageListenerContainer) listenerContainer);
				}
			}
		}
		return listenerContainers;
	}
}
