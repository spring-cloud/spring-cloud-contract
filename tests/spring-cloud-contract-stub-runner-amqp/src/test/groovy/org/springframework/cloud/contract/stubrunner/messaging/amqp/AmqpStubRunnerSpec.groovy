package org.springframework.cloud.contract.stubrunner.messaging.amqp

import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.contract.spec.Contract
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
        given:
        // tag::amqp_contract[]

            Contract.make {
                // Human readable description
                description 'Should produce valid person data'
                // Label by means of which the output message can be triggered
                label 'contract-test.person.created.event'
                // input to the contract
                input {
                    // the contract will be triggered by a method
                    triggeredBy('createPerson()')
                }
                // output message of the contract
                outputMessage {
                    // destination to which the output message will be sent
                    sentTo 'contract-test.exchange'
                    headers {
                        header('contentType': 'application/json')
                        header('__TypeId__': 'org.springframework.cloud.contract.stubrunner.messaging.amqp.Person')
                    }
                    // the body of the output message
                    body ([
                            id: $(consumer(9), producer(regex("[0-9]+"))),
                            name: "me"
                    ])
                }
            }
        // end::amqp_contract[]
        when:
        // tag::client_trigger[]
            stubTrigger.trigger("contract-test.person.created.event")
        // end::client_trigger[]
        then:
            then(messageSubscriber).should().handleMessage(personArgumentCaptor.capture())
            personArgumentCaptor.value.name != null
    }
}
