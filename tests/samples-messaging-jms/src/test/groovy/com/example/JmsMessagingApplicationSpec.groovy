/*
 * Copyright 2013-2019 the original author or authors.
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
import javax.jms.JMSException
import javax.jms.Message

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import org.junit.BeforeClass
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessagePostProcessor
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration

/**
 * SPIKE ON TESTS FROM NOTES IN MessagingSpec
 */
// Context configuration would end up in base class
@ContextConfiguration(classes = [JmsMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureMessageVerifier
class JmsMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Autowired
	JmsTemplate jmsTemplate
	@Autowired
	BookDeleter bookDeleter
	@Inject MessageVerifier<Message> messageVerifier
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	@BeforeClass
	static void init() {
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*")
	}

	def "should work for triggered based messaging"() {
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
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// generated test should look like this:
		when:
			bookReturnedTriggered()
		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive('output')
			response.getHeader('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	@DirtiesContext
	def "should generate tests triggered by a message"() {
		given:
			Contract.make {
				label 'some_label'
				input {
					messageFrom('input2')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('output2')
					body([
							bookName: 'foo'
					])
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// generated test should look like this:
		when:
			messageVerifier.send(
					contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
					[sample: 'header'], 'input2')
		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive('output2')
			response.getHeader('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
			Contract.make {
				label 'some_label'
				input {
					messageFrom('delete')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
					assertThat('bookWasDeleted()')
				}
			}
			// generated test should look like this:
		when:
			messageVerifier.
					send(contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
							[sample: 'header'], 'delete')
		then:
			noExceptionThrown()
			bookWasDeleted()
	}

	void bookReturnedTriggered() {
		jmsTemplate.convertAndSend("output", '''{"bookName" : "foo" }''', new MessagePostProcessor() {
			@Override
			Message postProcessMessage(Message message) throws JMSException {
				message.setStringProperty("BOOK-NAME", "foo")
				return message
			}
		})
	}

	PollingConditions pollingConditions = new PollingConditions()

	void bookWasDeleted() {
		pollingConditions.eventually {
			assert bookDeleter.bookSuccessfulyDeleted.get()
		}
	}

}
