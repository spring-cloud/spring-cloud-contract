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
import spock.lang.Ignore
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.test.context.ContextConfiguration

/**
 * SPIKE ON TESTS FROM NOTES IN MessagingSpec
 */
// Context configuration would end up in base class
@ContextConfiguration(classes = [Config, StreamMessagingApplication], loader = SpringBootContextLoader)
@SpringBootTest(properties = "debug=true")
@AutoConfigureMessageVerifier
class StreamMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Inject
	MessageVerifier<Message<?>> contractVerifierMessaging
	ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()

	def "should work for triggered based messaging"() {
		given:
			def dsl = Contract.make {
				label 'some_label'
				input {
					triggeredBy('bookReturnedTriggered()')
				}
				outputMessage {
					sentTo('bookReturned')
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
			def response = contractVerifierMessaging.receive('bookReturned')
			response.headers.get('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests triggered by a message"() {
		given:
			def dsl = Contract.make {
				label 'some_label'
				input {
					messageFrom('inputDestination')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('bookReturned')
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
					[sample: 'header'], 'inputDestination')
		then:
			def response = contractVerifierMessaging.receive('bookReturned')
			response.headers.get('BOOK-NAME') == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.
					parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	@Ignore
	def "should generate tests without destination, triggered by a message"() {
		given:
			def dsl = Contract.make {
				label 'some_label'
				input {
					messageFrom("bookDeleted")
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
							[sample: 'header'], "bookDeleted")
		then:
			noExceptionThrown()
			bookWasDeleted()
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired
	BookService bookService
	@Autowired
	BookDeletedListener bookDeletedListener

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookDeletedListener.bookSuccessfulyDeleted.get()
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration(TestChannelBinderConfiguration)
	static class Config {

	}
}
