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

package org.springframework.cloud.contract.stubrunner.messaging.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * @author Marcin Grzejszczak
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@ContextConfiguration(classes = StreamStubRunnerSpec, loader = SpringBootContextLoader)
@EnableBinding([Sink, Source])
class StreamStubRunnerSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired ContractVerifierMessaging messaging

	def setup() {
		// ensure that message were taken from the queue
		messaging.receiveMessage('returnBook', 100, TimeUnit.MILLISECONDS)
	}

	def 'should download the stub and register a route for it'() {
		when:
		// tag::client_send[]
			messaging.send(new BookReturned('foo'), [sample: 'header'], 'bookStorage')
		// end::client_send[]
		then:
		// tag::client_receive[]
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook')
		// end::client_receive[]
		and:
		// tag::client_receive_message[]
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
		// end::client_receive_message[]
	}

	def 'should trigger a message by label'() {
		when:
		// tag::client_trigger[]
			stubFinder.trigger('return_book_1')
		// end::client_trigger[]
		then:
		// tag::client_trigger_receive[]
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook')
		// end::client_trigger_receive[]
		and:
		// tag::client_trigger_message[]
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
		// end::client_trigger_message[]
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		when:
		// tag::trigger_group_artifact[]
			stubFinder.trigger('org.springframework.cloud.contract.verifier.stubs:streamService', 'return_book_1')
		// end::trigger_group_artifact[]
		then:
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing artifactId'() {
		when:
		// tag::trigger_artifact[]
			stubFinder.trigger('streamService', 'return_book_1')
		// end::trigger_artifact[]
		then:
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should throw exception when missing label is passed'() {
		when:
			stubFinder.trigger('missing label')
		then:
			thrown(IllegalArgumentException)
	}

	def 'should throw exception when missing label and artifactid is passed'() {
		when:
			stubFinder.trigger('some:service', 'return_book_1')
		then:
			thrown(IllegalArgumentException)
	}

	def 'should trigger messages by running all triggers'() {
		when:
		// tag::trigger_all[]
			stubFinder.trigger()
		// end::trigger_all[]
		then:
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label with no output message'() {
		when:
		// tag::trigger_no_output[]
			messaging.send(new BookReturned('foo'), [sample: 'header'], 'delete')
		// end::trigger_no_output[]
		then:
			noExceptionThrown()
	}

	def 'should not trigger a message that does not match input'() {
		when:
			messaging.send(new BookReturned('not_matching'), [wrong: 'header_value'], 'bookStorage')
		then:
			ContractVerifierMessage receivedMessage = messaging.receiveMessage('returnBook', 100, TimeUnit.MILLISECONDS)
		and:
			receivedMessage == null
	}

	private boolean assertJsons(Object payload) {
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
	}

	Contract dsl =
	// tag::sample_dsl[]
	Contract.make {
		label 'return_book_1'
		input {
			triggeredBy('bookReturnedTriggered()')
		}
		outputMessage {
			sentTo('returnBook')
			body('''{ "bookName" : "foo" }''')
			headers {
				header('BOOK-NAME', 'foo')
			}
		}
	}
	// end::sample_dsl[]

	Contract dsl2 =
	// tag::sample_dsl_2[]
	Contract.make {
		label 'return_book_2'
		input {
			messageFrom('bookStorage')
			messageBody([
					bookName: 'foo'
			])
			messageHeaders {
				header('sample', 'header')
			}
		}
		outputMessage {
			sentTo('returnBook')
			body([
					bookName: 'foo'
			])
			headers {
				header('BOOK-NAME', 'foo')
			}
		}
	}
	// end::sample_dsl_2[]

	Contract dsl3 =
			// tag::sample_dsl_3[]
			Contract.make {
				label 'delete_book'
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
	// end::sample_dsl_3[]

}
