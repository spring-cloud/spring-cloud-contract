/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example

import javax.inject.Inject

import org.apache.camel.Message
import org.apache.camel.model.ModelCamelContext
import org.junit.BeforeClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
/**
 * SPIKE ON TESTS FROM NOTES IN MessagingSpec
 */
// Context configuration would end up in base class
@ContextConfiguration(classes = [CamelMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureMessageVerifier
class CamelMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Autowired ModelCamelContext camelContext
	@Autowired BookDeleter bookDeleter
	@Inject MessageVerifier<Message> contractVerifierMessaging

	ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()

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
					sentTo('activemq:output')
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
			def response = contractVerifierMessaging.receive('activemq:output')
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.body))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests triggered by a message"() {
		given:
			Contract.make {
				label 'some_label'
				input {
					messageFrom('jms:input')
					messageBody([
					        bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')

					}
				}
				outputMessage {
					sentTo('jms:output')
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
			contractVerifierMessaging.send(
				contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header'], 'jms:input')
		then:
			def response = contractVerifierMessaging.receive('jms:output')
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.body))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
			Contract.make {
				label 'some_label'
				input {
					messageFrom('jms:delete')
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
			contractVerifierMessaging.send(contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header'], 'jms:delete')
		then:
			noExceptionThrown()
			bookWasDeleted()
	}

	void bookReturnedTriggered() {
		camelContext.createProducerTemplate().sendBody('direct:start', '''{"bookName" : "foo" }''')
	}

	PollingConditions pollingConditions = new PollingConditions()

	void bookWasDeleted() {
		pollingConditions.eventually {
			assert bookDeleter.bookSuccessfulyDeleted.get()
		}
	}

}