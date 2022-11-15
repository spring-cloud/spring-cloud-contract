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

package com.example

import javax.inject.Inject

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.assertj.core.api.Assertions.assertThat
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers

// Context configuration would end up in base class
@AutoConfigureMessageVerifier
@SpringBootTest(classes = AmqpMessagingApplication, properties = "stubrunner.amqp.enabled=true")
@Disabled("TODO: Migrate to middleware based approach")
class AmqpMessagingApplicationSpec {

	// ALL CASES
	@Inject
	ContractVerifierMessaging contractVerifierMessaging
	@Inject
	ContractVerifierObjectMapper contractVerifierObjectMapper

	@Test
	void should_work_for_triggered_based_messaging() {
		// given:
			def dsl = Contract.make {
				// Human readable description
				description 'Some description'
				// Label by means of which the output message can be triggered
				label 'some_label'
				// input to the contract
				input {
					// the contract will be triggered by a method
					triggeredBy('publishBook()')
				}
				// output message of the contract
				outputMessage {
					// destination to which the output message will be sent
					sentTo('test-exchange')
					// the body of the output message
					body('''{ "name" : "some" }''')
					// the headers of the output message
					headers {
						header('contentType', 'application/json')
						header('__TypeId__', 'com.example.Book')
					}
				}
			}
			// generated test should look like this:
		// when:
			publishBook()
		// then:
			def response = contractVerifierMessaging.receive('test-exchange')
			response.headers.get('contentType') == 'application/json'
		// and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('name').isEqualTo('some')
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired
	MessagePublisher messagePublisher

	void publishBook() {
		this.messagePublisher.sendMessage(new Book("some"))
	}
}
