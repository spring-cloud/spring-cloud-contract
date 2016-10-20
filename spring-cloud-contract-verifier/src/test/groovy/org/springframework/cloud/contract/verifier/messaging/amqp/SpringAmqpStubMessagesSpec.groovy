package org.springframework.cloud.contract.verifier.messaging.amqp

import com.google.common.collect.ImmutableMap
import org.mockito.ArgumentCaptor
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
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
    MessageListenerAdapter messageListenerAdapter = Mock(MessageListenerAdapter.class)

    def "should send amqp message with type id"() {
        given:
            String payload = '''{"name":"some"}'''
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class)
            SpringAmqpStubMessages messageVerifier = new SpringAmqpStubMessages(rabbitTemplate, messageListenerAdapter)
        when:
            messageVerifier.send(payload,
                    ImmutableMap.builder()
                            .put(DEFAULT_CLASSID_FIELD_NAME, "org.example.Some")
                            .put("contentType", CONTENT_TYPE_JSON)
                            .build(),
                    "test-exchange")
        then:
            1 * messageListenerAdapter.onMessage({
                it.getMessageProperties().getContentType() == CONTENT_TYPE_JSON &&
                it.getMessageProperties().getHeaders().get(DEFAULT_CLASSID_FIELD_NAME) == "org.example.Some"
            })
    }
}
