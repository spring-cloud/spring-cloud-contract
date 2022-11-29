/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.messaging.amqp

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner

import static org.mockito.BDDMockito.then

@AutoConfigureStubRunner
@SpringBootTest(classes = AmqpMessagingApplication)
@Disabled("TODO: Migrate to middleware based approach")
class AmqpStubRunnerSpec {

	@Autowired
	StubTrigger stubTrigger

	@SpyBean
	MessageSubscriber messageSubscriber

	ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person)

	@Test
	void "should trigger stub amqp message"() {
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
					body([
							id  : $(consumer(9), producer(regex("[0-9]+"))),
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
			assert personArgumentCaptor.value.name != null
	}
}
