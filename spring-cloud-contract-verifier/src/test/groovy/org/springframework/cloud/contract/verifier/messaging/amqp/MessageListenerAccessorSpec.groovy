package org.springframework.cloud.contract.verifier.messaging.amqp

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import spock.lang.Specification

class MessageListenerAccessorSpec extends Specification {

    def queueName = "test.queue"
    def exchange = "test-exchange"
    def listenerContainer
    def binding

    def "should get single simple listener container"(){
        given:
            givenSimpleMessageListenerContainer()
            def messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
        when:
            def listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(exchange)
        then:
            listenerContainersForDestination.size() == 1
            listenerContainersForDestination.get(0) == listenerContainer
    }

    def "should get empty listener container list for unknown destination"(){
        given:
            givenSimpleMessageListenerContainer()
            def messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
        when:
            def listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination("some-exchange")
        then:
            listenerContainersForDestination.isEmpty()
    }

    def "should get empty listener container list for queue with no matching listener"(){
        given:
            givenSimpleMessageListenerContainer()
            binding = BindingBuilder.bind(new Queue("some.queue")).to(new DirectExchange(exchange)).with("#")
            def messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
        when:
            def listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(exchange)
        then:
            listenerContainersForDestination.isEmpty()
    }

    def "should get single simple listener container from RabbitListenerEndpointRegistry"(){
        given:
            givenSimpleMessageListenerContainer()
            def rabbitListenerEndpointRegistryMock = Mock(RabbitListenerEndpointRegistry)
            rabbitListenerEndpointRegistryMock.getListenerContainers() >> [listenerContainer]
            def messageListenerAccessor = new MessageListenerAccessor(rabbitListenerEndpointRegistryMock, [], [binding])
        when:
            def listenerContainersForDestination = messageListenerAccessor.getListenerContainersForDestination(exchange)
        then:
            listenerContainersForDestination.size() == 1
            listenerContainersForDestination.get(0) == listenerContainer
    }

    def givenSimpleMessageListenerContainer() {
        listenerContainer = new SimpleMessageListenerContainer()
        listenerContainer.setQueueNames(queueName)
        binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with("#")
    }
}