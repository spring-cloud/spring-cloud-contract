/*
 *  Copyright 2013-2016 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageExchange
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.messaging.Message
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
// Context configuration would end up in base class
@ContextConfiguration(classes = [IntegrationMessagingApplication], loader = SpringBootContextLoader)
@DirtiesContext
@AutoConfigureContractVerifierMessaging
public class IntegrationMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Inject ContractVerifierMessageExchange<Message<?>> contractVerifierMessaging
	ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()

	def "should work for triggered based messaging"() {
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
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests triggered by a message"() {
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
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests without destination, triggered by a message"() {
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
			contractVerifierMessaging.send(contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header'], 'delete')
		then:
			noExceptionThrown()
			bookWasDeleted()
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired BookService bookService
	@Autowired BookListener bookListener

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookListener.bookSuccessfulyDeleted.get()
	}

}