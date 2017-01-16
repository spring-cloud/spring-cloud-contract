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

package org.springframework.cloud.contract.stubrunner.messaging.camel

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.activemq.spring.ActiveMQConnectionFactory
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.component.jms.JmsConfiguration
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
import org.springframework.test.context.ContextConfiguration
import spock.lang.IgnoreIf
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(properties = "debug=true")
@AutoConfigureStubRunner
@IgnoreIf({ os.windows })
class CamelStubRunnerSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired CamelContext camelContext

	def setup() {
		// ensure that message were taken from the queue
		camelContext.createConsumerTemplate().receive('jms:output', 100)
	}

	def 'should download the stub and register a route for it'() {
		when:
		// tag::client_send[]
			camelContext.createProducerTemplate().sendBodyAndHeaders('jms:input', new BookReturned('foo'), [sample: 'header'])
		// end::client_send[]
		then:
		// tag::client_receive[]
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		// end::client_receive[]
		and:
		// tag::client_receive_message[]
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
		// end::client_receive_message[]
	}

	def 'should trigger a message by label'() {
		when:
		// tag::client_trigger[]
			stubFinder.trigger('return_book_1')
		// end::client_trigger[]
		then:
		// tag::client_trigger_receive[]
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		// end::client_trigger_receive[]
		and:
		// tag::client_trigger_message[]
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
		// end::client_trigger_message[]
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		when:
		// tag::trigger_group_artifact[]
			stubFinder.trigger('org.springframework.cloud.contract.verifier.stubs:camelService', 'return_book_1')
		// end::trigger_group_artifact[]
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing artifactId'() {
		when:
		// tag::trigger_artifact[]
			stubFinder.trigger('camelService', 'return_book_1')
		// end::trigger_artifact[]
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
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
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertThatBodyContainsBookNameFoo(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label with no output message'() {
		when:
		// tag::trigger_no_output[]
			camelContext.createProducerTemplate().sendBodyAndHeaders('jms:delete', new BookReturned('foo'), [sample: 'header'])
		// end::trigger_no_output[]
		then:
			noExceptionThrown()
	}

	def 'should not trigger a message that does not match input'() {
		when:
			camelContext.createProducerTemplate().sendBodyAndHeaders('jms:input', new BookReturned('notmatching'), [wrong: 'header_value'])
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 100)
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
	static class Config {

		@Bean
		ActiveMQConnectionFactory activeMQConnectionFactory(@Value('${activemq.url:vm://localhost?broker.persistent=false}') String url) {
			return new ActiveMQConnectionFactory(brokerURL: url, trustAllPackages: true)
		}

		@Bean
		JmsConfiguration jmsConfiguration(ActiveMQConnectionFactory activeMQConnectionFactory) {
			return new JmsConfiguration(connectionFactory: activeMQConnectionFactory)
		}

		@Bean
		ActiveMQComponent activeMQComponent(JmsConfiguration jmsConfiguration) {
			return new ActiveMQComponent(configuration: jmsConfiguration)
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
			sentTo('jms:output')
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
	// end::sample_dsl_2[]

	Contract dsl3 =
	// tag::sample_dsl_3[]
	Contract.make {
		label 'delete_book'
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
	// end::sample_dsl_3[]
}
