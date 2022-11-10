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
import org.junit.jupiter.api.Test
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.messaging.Message

// Context configuration would end up in base class
@AutoConfigureMessageVerifier
@SpringBootTest(classes = IntegrationMessagingApplication)
class IntegrationMessagingApplicationSpec {

	// ALL CASES
	@Inject
	MessageVerifier<Message<?>> contractVerifierMessaging
	ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()

	@Test
	void "should work for triggered based messaging"() {
		given:
			// tag::method_trigger[]
			def dsl = Contract.make {
				// Human readable description
				description 'Some description'
				// Label by means of which the output message can be triggered
				label 'some_label'
				// input to the contract
				input {
					// the contract will be triggered by a method
					triggeredBy('bookReturnedTriggered()')
				}
				// output message of the contract
				outputMessage {
					// destination to which the output message will be sent
					sentTo('output')
					// the body of the output message
					body('''{ "bookName" : "foo" }''')
					// the headers of the output message
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// end::method_trigger[]
			// generated test should look like this:
		when:
			bookReturnedTriggered()
		then:
			def response = contractVerifierMessaging.receive('output')
			response.headers.get('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	void "should generate tests triggered by a message"() {
		given:
			// tag::message_trigger[]
			def dsl = Contract.make {
				description 'Some Description'
				label 'some_label'
				// input is a message
				input {
					// the message was received from this destination
					messageFrom('input')
					// has the following body
					messageBody([
							bookName: 'foo'
					])
					// and the following headers
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('output')
					body([
							bookName: 'foo'
					])
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// end::message_trigger[]

			// generated test should look like this:

		when:
			contractVerifierMessaging.send(
					contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
					[sample: 'header'], 'input')
		then:
			def response = contractVerifierMessaging.receive('output')
			response.headers.get('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	@Test
	void "should generate tests without destination, triggered by a message"() {
		given:
			def dsl = Contract.make {
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
			contractVerifierMessaging.
					send(contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
							[sample: 'header'], 'delete')
		then:
			bookWasDeleted()
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired
	BookService bookService
	@Autowired
	BookListener bookListener

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookListener.bookSuccessfullyDeleted.get()
	}

}
