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

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.TextMessage

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.activemq.ActiveMQConnectionFactory
import spock.lang.IgnoreIf
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessagePostProcessor
import org.springframework.test.context.ContextConfiguration

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(properties = ["debug=true"])
@AutoConfigureStubRunner
@IgnoreIf({ os.windows })
class JmsStubRunnerSpec extends Specification {

	@Autowired
	StubFinder stubFinder
	@Autowired
	JmsTemplate jmsTemplate

	def cleanup() {
		// ensure that message were taken from the queue
		jmsTemplate.receive('output')
		jmsTemplate.receive('input')
	}

	def 'should download the stub and register a route for it'() {
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
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			receivedMessage.getStringProperty('BOOK-NAME') == 'foo'
			// end::client_receive_message[]
	}

	def 'should trigger a message by label'() {
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
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			receivedMessage.getStringProperty('BOOK-NAME') == 'foo'
			// end::client_trigger_message[]
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		when:
			// tag::trigger_group_artifact[]
			stubFinder.
					trigger('my:stubs', 'return_book_1')
			// end::trigger_group_artifact[]
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			receivedMessage.getStringProperty('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing artifactId'() {
		when:
			// tag::trigger_artifact[]
			stubFinder.trigger('stubs', 'return_book_1')
			// end::trigger_artifact[]
		then:
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			receivedMessage.getStringProperty('BOOK-NAME') == 'foo'
	}

	def 'should throw an exception when missing label is passed'() {
		when:
			stubFinder.trigger('missing label')
		then:
			thrown(IllegalArgumentException)
	}

	def 'should throw an exception when missing label and artifactid is passed'() {
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
			TextMessage receivedMessage = (TextMessage) jmsTemplate.receive('output')
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.getText())
			receivedMessage.getStringProperty('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label with no output message'() {
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
		then:
			noExceptionThrown()
	}

	def 'should not trigger a message that does not match input'() {
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
			receivedMessage == null
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
		@Bean
		ActiveMQConnectionFactory activeMQConnectionFactory(@Value('${activemq.url:vm://localhost?broker.persistent=false}') String url) {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL: url)
			try {
				factory.trustAllPackages = true
			}
			catch (Throwable e) {
			}
			return factory
		}
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
