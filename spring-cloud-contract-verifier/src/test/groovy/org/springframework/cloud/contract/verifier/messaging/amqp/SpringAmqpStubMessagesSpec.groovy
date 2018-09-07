package org.springframework.cloud.contract.verifier.messaging.amqp

import org.mockito.exceptions.verification.WantedButNotInvoked
import wiremock.com.google.common.collect.ImmutableMap
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import spock.lang.Specification

import static org.mockito.Mockito.mock
import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON
import static org.springframework.amqp.support.converter.DefaultClassMapper.DEFAULT_CLASSID_FIELD_NAME
/**
 * @author Mathias Düsterhöft
 */
class SpringAmqpStubMessagesSpec extends Specification {

    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class)
    SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer()
    MessageListenerAdapter messageListenerAdapter = Mock(MessageListenerAdapter.class)
    Message message = Mock(Message.class)

    String queueName = "test.queue"
    String exchange = "test-exchange"
    String payload = '''{"name":"some"}'''
    String routingKey = "resource.created"

    def "should send amqp message with type id"() {
        given:
            listenerContainer.setMessageListener(messageListenerAdapter)
            listenerContainer.setQueueNames(queueName)
            Binding binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with(routingKey)
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAccessor)

        when:
            messageVerifier.send(payload,
                    ImmutableMap.builder()
                            .put(DEFAULT_CLASSID_FIELD_NAME, "org.example.Some")
                            .put("amqp_receivedRoutingKey", routingKey)
                            .put("contentType", CONTENT_TYPE_JSON)
                            .build(),
                    exchange)
        then:
            1 * messageListenerAdapter.onMessage({ Message msg ->
                msg.getMessageProperties().getReceivedRoutingKey() == "resource.created" &&
                msg.getMessageProperties().getContentType() == CONTENT_TYPE_JSON &&
                msg.getMessageProperties().getHeaders().get(DEFAULT_CLASSID_FIELD_NAME) == "org.example.Some"
            })
    }

    def "should fail to receive a message if rabbit template wasn't called"() {
        given:
            listenerContainer.setMessageListener(messageListenerAdapter)
            listenerContainer.setQueueNames(queueName)
            Binding binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with(routingKey)
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAccessor)
        when:
            messageVerifier.receive("foo")
        then:
            thrown(WantedButNotInvoked)
    }

    def "should return null if received called and message was sent without any body"() {
        given:
            listenerContainer.setMessageListener(messageListenerAdapter)
            listenerContainer.setQueueNames(queueName)
            Binding binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with(routingKey)
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAccessor)

        and:
            rabbitTemplate.send("foo", "bar", null, null)
        expect:
            messageVerifier.receive("foo") == null
    }

    def "should return match the message if received called and message was sent with a message with null payload"() {
        given:
            listenerContainer.setMessageListener(messageListenerAdapter)
            listenerContainer.setQueueNames(queueName)
            Binding binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with(routingKey)
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAccessor)
            message.getMessageProperties() >> new MessageProperties()
        and:
            rabbitTemplate.send("foo", "bar", message, null)
        expect:
            messageVerifier.receive("foo") is message
    }
}
