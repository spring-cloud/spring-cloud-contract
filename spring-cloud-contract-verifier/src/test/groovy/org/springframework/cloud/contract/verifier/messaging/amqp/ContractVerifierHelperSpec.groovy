package org.springframework.cloud.contract.verifier.messaging.amqp

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessagePropertiesBuilder
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import spock.lang.Specification

import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON
/**
 * @author Mathias Düsterhöft
 */
class ContractVerifierHelperSpec extends Specification {

    def "should convert message"() {
        given:
            String payload = '''{"name":"some"}'''
            Message message = MessageBuilder
                .withBody(payload.bytes)
                .andProperties(MessagePropertiesBuilder.newInstance()
                        .setHeader("my-header", "some")
                        .setContentType(CONTENT_TYPE_JSON)
                        .build()).build()
            ContractVerifierHelper contractVerifierHelper = new ContractVerifierHelper(null, new Jackson2JsonMessageConverter())
        when:
            ContractVerifierMessage contractVerifierMessage = contractVerifierHelper.convert(message)
        then:
            ((Map) contractVerifierMessage.payload).containsKey("name")
            contractVerifierMessage.headers.containsKey("contentType")
            contractVerifierMessage.headers.containsKey("my-header")
    }
}
