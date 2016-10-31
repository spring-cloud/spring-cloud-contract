package org.springframework.cloud.contract.verifier.messaging.amqp

import com.google.common.collect.ImmutableMap
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
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

    String queueName = "test.queue"
    String exchange = "test-exchange"
    String payload = '''{"name":"some"}'''

    def "should send amqp message with type id"() {
        given:
            listenerContainer.setMessageListener(messageListenerAdapter)
            listenerContainer.setQueueNames(queueName)
            Binding binding = BindingBuilder.bind(new Queue(queueName)).to(new DirectExchange(exchange)).with("#")
            MessageListenerAccessor messageListenerAccessor = new MessageListenerAccessor(null, [listenerContainer], [binding])
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAccessor)

        when:
            messageVerifier.send(payload,
                    ImmutableMap.builder()
                            .put(DEFAULT_CLASSID_FIELD_NAME, "org.example.Some")
                            .put("contentType", CONTENT_TYPE_JSON)
                            .build(),
                    exchange)
        then:
            1 * messageListenerAdapter.onMessage({
                it.getMessageProperties().getContentType() == CONTENT_TYPE_JSON &&
                it.getMessageProperties().getHeaders().get(DEFAULT_CLASSID_FIELD_NAME) == "org.example.Some"
            })
    }
}
