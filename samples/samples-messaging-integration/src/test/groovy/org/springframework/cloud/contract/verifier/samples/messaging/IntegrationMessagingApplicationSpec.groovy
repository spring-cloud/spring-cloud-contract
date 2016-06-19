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

package org.springframework.cloud.contract.verifier.samples.messaging

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.contract.verifier.dsl.Contract
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierObjectMapper
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject
// Context configuration would end up in base class
@ContextConfiguration(classes = [IntegrationMessagingApplication], loader = SpringApplicationContextLoader)
public class IntegrationMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Inject ContractVerifierMessaging contractVerifierMessaging
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
			def response = contractVerifierMessaging.receiveMessage('output')
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

		//given:
		ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
				contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header']
		)
		when:
			contractVerifierMessaging.send(inputMessage, 'input')
		then:
			def response = contractVerifierMessaging.receiveMessage('output')
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

		//given:
		ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
				contractVerifierObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header']
		)
		when:
			contractVerifierMessaging.send(inputMessage, 'delete')
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