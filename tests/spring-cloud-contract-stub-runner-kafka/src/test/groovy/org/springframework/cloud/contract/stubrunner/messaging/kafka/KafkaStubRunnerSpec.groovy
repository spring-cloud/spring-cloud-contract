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

package org.springframework.cloud.contract.stubrunner.messaging.kafka

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import org.assertj.core.api.BDDAssertions
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
/**
 * @author Marcin Grzejszczak
 */
@SpringBootTest(classes = Config, properties = ["debug=true"])
@AutoConfigureStubRunner
@DisabledOnOs(value = OS.WINDOWS)
@EmbeddedKafka(topics = ["input", "input2", "output", "delete"])
@Commons
class KafkaStubRunnerSpec {

	@Autowired
	StubFinder stubFinder
	@Autowired
	KafkaTemplate kafkaTemplate
	@Autowired
	MyMessageListener myMessageListener

	@BeforeEach
	@AfterEach
	void setup() {
		this.myMessageListener.clear()
	}

	@CompileStatic
	private Message receiveFromOutput() {
		Message m = null
		Awaitility.await().untilAsserted(() -> {
			m = this.myMessageListener.output()
			log.info("Received from message [" + m + "]")
			assert m != null
		})
		return m
	}

	@CompileStatic
	private Message receiveNullableMessageFromOutput() {
		Message m = this.myMessageListener.output()
		log.info("Received message [" + m + "]")
		return m
	}

	// Skipping the test on Jenkins cause it's for some reason flakey only there
	@Test
	void 'should download the stub and register a route for it'() {
		expect:
			log.info("Sending the message")
			// tag::client_send[]
			Message message = MessageBuilder.createMessage(new BookReturned('foo'), new MessageHeaders([sample: "header",]))
			kafkaTemplate.setDefaultTopic('input')
			kafkaTemplate.send(message)
			// end::client_send[]
			log.info("Message sent")
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
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

	@Test
	void 'should propagate the Kafka record key via message headers'() {
		expect:
			log.info("Sending the message")
			// tag::client_send[]
			Message message = MessageBuilder.createMessage(new BookReturned('bar'), new MessageHeaders([kafka_messageKey: "bar5150",]))
			kafkaTemplate.setDefaultTopic('input2')
			kafkaTemplate.send(message)
			// end::client_send[]
			log.info("Message sent")
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
			log.info("Receiving the message")
			// tag::client_receive[]
			Message receivedMessage = receiveFromOutput()
			// end::client_receive[]
			log.info("Message received [" + receivedMessage + "]")
			// tag::client_receive_message[]
			assert receivedMessage != null
			assert assertThatBodyContainsBookName(receivedMessage.getPayload(), 'bar')
			assert receivedMessage.getHeaders().get('BOOK-NAME') == 'bar'
			assert receivedMessage.getHeaders().get("kafka_receivedMessageKey") == 'bar5150'
			// end::client_receive_message[]
		}
	}

	@Test
	void 'should trigger a message by label'() {
		expect:
			// tag::client_trigger[]
			stubFinder.trigger('return_book_1')
			// end::client_trigger[]
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
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

	@Test
	void 'should trigger a label for the existing groupId and artifactId'() {
		expect:
			// tag::trigger_group_artifact[]
			stubFinder.
					trigger('my:stubs', 'return_book_1')
			// end::trigger_group_artifact[]
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
			Message receivedMessage = receiveFromOutput()
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
			assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
		}
	}

	@Test
	void 'should trigger a label for the existing artifactId'() {
		expect:
			// tag::trigger_artifact[]
			stubFinder.trigger('stubs', 'return_book_1')
			// end::trigger_artifact[]
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
			Message receivedMessage = receiveFromOutput()
			assert receivedMessage != null
			assert assertThatBodyContainsBookNameFoo(receivedMessage.getPayload())
			assert receivedMessage.getHeaders().get('BOOK-NAME') == 'foo'
		}
	}

	@Test
	void 'should throw an exception when missing label is passed'() {
		expect:
		BDDAssertions.thenThrownBy(() -> stubFinder.trigger('missing label')).isInstanceOf(IllegalArgumentException)
	}

	@Test
	void 'should throw an exception when missing label and artifactid is passed'() {
		expect:
		BDDAssertions.thenThrownBy(() -> stubFinder.trigger('some:service', 'return_book_1')).isInstanceOf(IllegalArgumentException)
	}

	@Test
	void 'should trigger messages by running all triggers'() {
		expect:
			// tag::trigger_all[]
			stubFinder.trigger()
			// end::trigger_all[]
		Awaitility.await().pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
			Message receivedMessage = receiveFromOutput()
			assert receivedMessage != null
			assert assertThatBodyContainsBookName(receivedMessage.getPayload())
			assert receivedMessage.getHeaders().get('BOOK-NAME') != null
		}
	}

	@Test
	void 'should trigger a label with no output message'() {
		when:
		// tag::trigger_no_output[]
		Message message = MessageBuilder.createMessage(new BookReturned('foo'), new MessageHeaders([sample: "header",]))
		kafkaTemplate.setDefaultTopic('delete')
		kafkaTemplate.send(message)
		// end::trigger_no_output[]
	}

	@Test
	void 'should not trigger a message that does not match input'() {
		when:
		Message message = MessageBuilder.createMessage(new BookReturned('notmatching'), new MessageHeaders([wrong: "header",]))
		kafkaTemplate.setDefaultTopic('input')
		kafkaTemplate.send(message)
		then:
		Message receivedMessage = receiveNullableMessageFromOutput()
		and:
		assert receivedMessage == null
	}

	private boolean assertThatBodyContainsBookNameFoo(Object payload) {
		return assertThatBodyContainsBookName(payload, 'foo')
	}

	private boolean assertThatBodyContainsBookName(Object payload, String expectedValue) {
		log.info("Got payload [" + payload + "]")
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == expectedValue
	}

	private boolean assertThatBodyContainsBookName(Object payload) {
		log.info("Got payload [" + payload + "]")
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName != null
	}

	@Configuration
	@ComponentScan
	@EnableAutoConfiguration
	@EnableKafka
	static class Config {
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
		}

		void clear() {
			this.output = null
		}

		Message output() {
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
