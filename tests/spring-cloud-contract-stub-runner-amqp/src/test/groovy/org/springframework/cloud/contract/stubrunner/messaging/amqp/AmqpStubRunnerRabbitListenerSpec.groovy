package org.springframework.cloud.contract.stubrunner.messaging.amqp

import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [AmqpMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureStubRunner
@ActiveProfiles("listener")
class AmqpStubRunnerRabbitListenerSpec extends Specification {

    @Autowired
    StubTrigger stubTrigger

    @Autowired
    MessageSubscriberRabbitListener messageSubscriber

    @Captor
    ArgumentCaptor<Person> personArgumentCaptor
    
    def "should trigger stub amqp message consumed by annotated listener"() {
        when:
            stubTrigger.trigger("contract-test.person.created.event")
        then:
            messageSubscriber.person != null
            messageSubscriber.person.name != null
    }
}
