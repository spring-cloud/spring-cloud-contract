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

package org.springframework.cloud.contract.stubrunner.messaging.kafka

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Commons
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.DefaultKafkaHeaderMapper
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(properties = ["debug=true"])
@AutoConfigureStubRunner
@IgnoreIf({ os.windows })
@EmbeddedKafka(topics = ["input", "output", "delete"])
@Commons
class KafkaStubRunnerSpec extends Specification {

	@Autowired
	StubFinder stubFinder
	@Autowired
	KafkaTemplate kafkaTemplate
	@Autowired
	MyMessageListener myMessageListener
	PollingConditions await = new PollingConditions(timeout: 15, initialDelay: 1, delay: 1)

	def setup() {
		this.myMessageListener.clear()
	}

	def cleanup() {
		this.myMessageListener.clear()
	}

	private Message receiveFromOutput() {
		Message m = this.myMessageListener.output()
		log.info("Received message [" + m + "]")
		return m
	}

	// Skipping the test on Jenkins cause it's for some reason flakey only there
	def 'should download the stub and register a route for it'() {
		expect:
			await.eventually {
				log.info("Sending the message")
				// tag::client_send[]
				Message message = MessageBuilder.createMessage(new BookReturned('foo'), new MessageHeaders([sample: "header",]))
				kafkaTemplate.setDefaultTopic('input')
				kafkaTemplate.send(message)
				// end::client_send[]
				log.info("Message sent")
				log.info("Receiving the message")
				// tag::client_receive[]
				Message receivedMessage = receiveFromOutput()
				// end::client_receive[]
				log.info("Message received [" + receivedMessage + "]")
				// tag::client_receive_message[]
				assert receivedMessage != null
				assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
				assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
				// end::client_receive_message[]
			}
	}

	def 'should trigger a message by label'() {
		expect:
			await.eventually {
				// tag::client_trigger[]
				stubFinder.trigger('return_book_1')
				// end::client_trigger[]
				// tag::client_trigger_receive[]
				Message receivedMessage = receiveFromOutput()
				// end::client_trigger_receive[]
				// tag::client_trigger_message[]
				assert receivedMessage != null
				assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
				assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
				// end::client_trigger_message[]
			}
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		expect:
			await.eventually {
				// tag::trigger_group_artifact[]
				stubFinder.
						trigger('my:stubs', 'return_book_1')
				// end::trigger_group_artifact[]
				Message receivedMessage = receiveFromOutput()
				assert receivedMessage != null
				assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
				assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
			}
	}

	def 'should trigger a label for the existing artifactId'() {
		expect:
			await.eventually {
				// tag::trigger_artifact[]
				stubFinder.trigger('stubs', 'return_book_1')
				// end::trigger_artifact[]
				Message receivedMessage = receiveFromOutput()
				assert receivedMessage != null
				assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
				assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
			}
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
		expect:
			await.eventually {
				// tag::trigger_all[]
				stubFinder.trigger()
				// end::trigger_all[]
				Message receivedMessage = receiveFromOutput()
				assert receivedMessage != null
				assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
				assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
			}
	}

	def 'should trigger a label with no output message'() {
		when:
			// tag::trigger_no_output[]
			Message message = MessageBuilder.createMessage(new BookReturned('foo'), new MessageHeaders([sample: "header",]))
			kafkaTemplate.setDefaultTopic('delete')
			kafkaTemplate.send(message)
			// end::trigger_no_output[]
		then:
			noExceptionThrown()
	}

	def 'should not trigger a message that does not match input'() {
		when:
			Message message = MessageBuilder.createMessage(new BookReturned('notmatching'), new MessageHeaders([wrong: "header",]))
			kafkaTemplate.setDefaultTopic('input')
			kafkaTemplate.send(message)
		then:
			Message receivedMessage = receiveFromOutput()
		and:
			receivedMessage == null
	}

	private boolean assertThatBodyContainsBookNameFoo(Object payload) {
		log.info("Got payload [" + payload + "]")
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
	}

	@Configuration
	@ComponentScan
	@EnableAutoConfiguration
	@EnableKafka
	static class Config {

		@Bean
		DefaultKafkaHeaderMapper headerMapper() {
			return new DefaultKafkaHeaderMapper();
		}

	}

	@Commons
	@Component
	static class MyMessageListener {

		CountDownLatch latch = new CountDownLatch(1)

		Message output

		@KafkaListener(topics = ["output"])
		void output(Message message) {
			log.info("I got the message [${message}]")
			this.output = message
			this.latch.countDown()
		}

		void clear() {
			this.output = null
			this.latch = new CountDownLatch(1)
		}

		Message output() {
			this.latch.await(2, TimeUnit.SECONDS)
			return this.output
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