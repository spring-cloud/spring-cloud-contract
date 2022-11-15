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
import org.apache.camel.Message
import org.apache.camel.model.ModelCamelContext
import org.junit.jupiter.api.Test
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
/**
 * SPIKE ON TESTS FROM NOTES IN MessagingSpec
 */
// Context configuration would end up in base class
@AutoConfigureMessageVerifier
@SpringBootTest(classes = CamelMessagingApplication, properties = 'camel.component.rabbitmq.declare=true')
@Testcontainers
class CamelMessagingApplicationSpec {

	// ALL CASES
	@Autowired
	ModelCamelContext camelContext
	@Inject
	MessageVerifier<Message> contractVerifierMessaging

	ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()

	@Container
	static RabbitMQContainer broker = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

	@DynamicPropertySource
	static void setup(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.port", () -> broker.getAmqpPort());
	}

	@Test
	void "should work for triggered based messaging"() {
		given:
			// tag::sample_dsl[]
			Contract.make {
				label 'return_book_1'
				input {
					triggeredBy('bookReturnedTriggered()')
				}
				outputMessage {
					sentTo('rabbitmq:output?queue=output')
					body('''{ "bookName" : "foo" }''')
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// end::sample_dsl[]
			// generated test should look like this:
		when:
			bookReturnedTriggered()
		then:
			def response = contractVerifierMessaging.receive('rabbitmq:output?queue=output')
			assert response.headers.get('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.body))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	void bookReturnedTriggered() {
		camelContext.createProducerTemplate().
				sendBody('direct:start', '''{"bookName" : "foo" }''')
	}

}
