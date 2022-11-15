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
import jakarta.jms.JMSException
import jakarta.jms.Message
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessagePostProcessor
import org.springframework.test.annotation.DirtiesContext

/**
 * SPIKE ON TESTS FROM NOTES IN MessagingSpec
 */
// Context configuration would end up in base class
@AutoConfigureMessageVerifier
@SpringBootTest(classes = JmsMessagingApplication)
class JmsMessagingApplicationSpec {

	// ALL CASES
	@Autowired
	JmsTemplate jmsTemplate
	@Inject
	MessageVerifier<Message> messageVerifier
	@Inject
	ContractVerifierMessaging contractVerifierMessaging
	@Inject
	ContractVerifierObjectMapper contractVerifierObjectMapper

	@BeforeAll
	static void setupSpec() {
		System.setProperty("debug", "true")
	}

	@Test
	void "should work for triggered based messaging"() {
		given:
		Contract.make {
			label 'some_label'
			input {
				triggeredBy('bookReturnedTriggered()')
			}
			outputMessage {
				sentTo('output')
				body('''{ "bookName" : "foo" }''')
				headers {
					header('BOOKNAME', 'foo')
				}
			}
		}
		// generated test should look like this:
		when:
		bookReturnedTriggered()
		then:
		ContractVerifierMessage response = contractVerifierMessaging.receive('output')
		assert response.getHeader('BOOKNAME') == 'foo'
		and:
		DocumentContext parsedJson = JsonPath.
				parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
		JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	void bookReturnedTriggered() {
		jmsTemplate.convertAndSend("output", '''{"bookName" : "foo" }''', new MessagePostProcessor() {
			@Override
			Message postProcessMessage(Message message) throws JMSException {
				message.setStringProperty("BOOKNAME", "foo")
				return message
			}
		})
	}

}
