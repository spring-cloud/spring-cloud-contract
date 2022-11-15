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

package org.springframework.cloud.contract.stubrunner.messaging.jms

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.jms.JMSException
import jakarta.jms.Message
import jakarta.jms.TextMessage
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessagePostProcessor
/**
 * @author Marcin Grzejszczak
 */
@SpringBootTest(classes = Config, properties = ["debug=true"])
@AutoConfigureStubRunner
@DisabledOnOs(OS.WINDOWS)
class JmsStubRunnerSpec {

	@Autowired
	StubFinder stubFinder
	@Autowired
	JmsTemplate jmsTemplate

	@AfterEach
	void cleanup() {
		// ensure that message were taken from the queue
		jmsTemplate.receive('output')
		jmsTemplate.receive('input')
	}

	@Test
	void 'should download the stub and register a route for it'() {
		when:
			// tag::client_send[]
			jmsTemplate.
					convertAndSend('input', new BookReturned('foo'), new MessagePostProcessor() {
						@Override
						Message postProcessMessage(Message message) throws JMSException {
							message.setStringProperty("sample", "header")
							return message
						}
					})
			// end::client_send[]
		then:
			// tag::client_receive[]
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
			// end::client_receive[]
		and:
			// tag::client_receive_message[]
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			assert receivedMessage.getStringProperty('BOOKNAME') == 'foo'
			// end::client_receive_message[]
	}

	@Test
	void 'should trigger a message by label'() {
		when:
			// tag::client_trigger[]
			stubFinder.trigger('return_book_1')
			// end::client_trigger[]
		then:
			// tag::client_trigger_receive[]
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
			// end::client_trigger_receive[]
		and:
			// tag::client_trigger_message[]
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			assert receivedMessage.getStringProperty('BOOKNAME') == 'foo'
			// end::client_trigger_message[]
	}

	@Test
	void 'should trigger a label for the existing groupId:artifactId'() {
		when:
			// tag::trigger_group_artifact[]
			stubFinder.
					trigger('my:stubs', 'return_book_1')
			// end::trigger_group_artifact[]
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			assert receivedMessage.getStringProperty('BOOKNAME') == 'foo'
	}

	@Test
	void 'should trigger a label for the existing artifactId'() {
		when:
			// tag::trigger_artifact[]
			stubFinder.trigger('stubs', 'return_book_1')
			// end::trigger_artifact[]
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			assert receivedMessage.getStringProperty('BOOKNAME') == 'foo'
	}

	@Test
	void 'should throw an exception when missing label is passed'() {
		when:
		BDDAssertions.thenThrownBy(() -> stubFinder.trigger('missing label')).isInstanceOf(IllegalArgumentException)
	}

	@Test
	void 'should throw an exception when missing label and artifactid is passed'() {
		when:
		BDDAssertions.thenThrownBy(() -> stubFinder.trigger('some:service', 'return_book_1')).isInstanceOf(IllegalArgumentException)
	}

	@Test
	void 'should trigger messages by running all triggers'() {
		when:
			// tag::trigger_all[]
			stubFinder.trigger()
			// end::trigger_all[]
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			assert receivedMessage.getStringProperty('BOOKNAME') == 'foo'
	}

	@Test
	void 'should trigger a label with no output message'() {
		when:
			// tag::trigger_no_output[]
			jmsTemplate.
					convertAndSend('delete', new BookReturned('foo'), new MessagePostProcessor() {
						@Override
						Message postProcessMessage(Message message) throws JMSException {
							message.setStringProperty("sample", "header")
							return message
						}
					})
			// end::trigger_no_output[]
	}

	@Test
	void 'should not trigger a message that does not match input'() {
		when:
			jmsTemplate.
					convertAndSend('input', new BookReturned('notmatching'), new MessagePostProcessor() {
						@Override
						Message postProcessMessage(Message message) throws JMSException {
							message.setStringProperty("wrong", "header")
							return message
						}
					})
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			assert receivedMessage == null
	}

	private boolean assertThatBodyContainsBookNameFoo(Object payload) {
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
	}

	@Configuration
	@ComponentScan
	@EnableAutoConfiguration
	@EnableJms
	static class Config {

	}

	Contract dsl =
			// tag::sample_dsl[]
			Contract.make {
				label 'return_book_1'
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
	// end::sample_dsl[]

	Contract dsl2 =
			// tag::sample_dsl_2[]
			Contract.make {
				label 'return_book_2'
				input {
					messageFrom('input')
					messageBody([
							bookName: 'foo'
					])
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
						header('BOOKNAME', 'foo')
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
