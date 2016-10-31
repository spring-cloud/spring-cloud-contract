package org.springframework.cloud.contract.verifier.messaging.amqp

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import spock.lang.Specification

class MessageListenerAccessorSpec extends Specification {

    String queueName = "test.queue"
    String exchange = "test-exchange"
    SimpleMessageListenerContainer listenerContainer
    Binding binding

    def "should get single simple listener container"(){
        given:
            givenSimpleMessageListenerContainer()
        MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [this.listenerContainer], [this.binding])
        when:
            List<SimpleMessageListenerContainer> listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(this.exchange)
        then:
            listenerContainersForDestination.size() == 1
            listenerContainersForDestination.get(0) == this.listenerContainer
    }

    def "should get empty listener container list for unknown destination"(){
        given:
            givenSimpleMessageListenerContainer()
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [this.listenerContainer], [this.binding])
        when:
            List<SimpleMessageListenerContainer> listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination("some-exchange")
        then:
            listenerContainersForDestination.isEmpty()
    }

    def "should get empty listener container list for queue with no matching listener"(){
        given:
            givenSimpleMessageListenerContainer()
            this.binding = BindingBuilder.bind(new Queue("some.queue")).to(new DirectExchange(this.exchange)).with("#")
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [this.listenerContainer], [this.binding])
        when:
            List<SimpleMessageListenerContainer> listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(this.exchange)
        then:
            listenerContainersForDestination.isEmpty()
    }

    def "should get single simple listener container from RabbitListenerEndpointRegistry"(){
        given:
            givenSimpleMessageListenerContainer()
            RabbitListenerEndpointRegistry rabbitListenerEndpointRegistryMock = Mock(RabbitListenerEndpointRegistry)
            rabbitListenerEndpointRegistryMock.getListenerContainers() >> [this.listenerContainer]
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(rabbitListenerEndpointRegistryMock, [], [this.binding])
        when:
            List<SimpleMessageListenerContainer> listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(this.exchange)
        then:
            listenerContainersForDestination.size() == 1
            listenerContainersForDestination.get(0) == this.listenerContainer
    }

    def givenSimpleMessageListenerContainer() {
        this.listenerContainer = new SimpleMessageListenerContainer()
        this.listenerContainer.setQueueNames(this.queueName)
        this.binding = BindingBuilder.bind(new Queue(this.queueName)).to(new DirectExchange(this.exchange)).with("#")
    }
}