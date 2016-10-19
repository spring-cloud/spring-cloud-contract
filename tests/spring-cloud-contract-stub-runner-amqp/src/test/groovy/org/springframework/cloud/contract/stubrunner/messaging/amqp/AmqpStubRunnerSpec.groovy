package org.springframework.cloud.contract.stubrunner.messaging.amqp

import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.BDDMockito.then

@ContextConfiguration(classes = [AmqpMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureStubRunner
class AmqpStubRunnerSpec extends Specification {

    @Autowired
    StubTrigger stubTrigger

    @SpyBean
    MessageSubscriber messageSubscriber

    @Captor
    ArgumentCaptor<Person> personArgumentCaptor
    
    def "should trigger stub amqp message"() {
        when:
        // tag::client_trigger[]
            stubTrigger.trigger("contract-test.person.created.event")
        // end::client_trigger[]
        then:
            then(messageSubscriber).should().handleMessage(personArgumentCaptor.capture())
            personArgumentCaptor.value.name != null
    }
}
