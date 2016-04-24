package io.codearte.accurest.stubrunner.messaging.camel

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.StubFinder
import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@ContextConfiguration(classes = CamelStubRunnerSpec, loader = SpringApplicationContextLoader)
class CamelStubRunnerSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired CamelContext camelContext

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
			stubFinder.trigger('io.codearte.accurest.stubs:camelService', 'return_book_1')
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

	def 'should not run any wrong trigger when missing label is passed'() {
		when:
			stubFinder.trigger('missing label')
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 100)
		and:
			receivedMessage == null
	}

	def 'should not run any wrong trigger when missing label and artifactid is passed'() {
		when:
			stubFinder.trigger('some:service', 'return_book_1')
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 100)
		and:
			receivedMessage == null
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

	def setup() {
		// ensure that message were taken from the queue
		camelContext.createConsumerTemplate().receive('jms:output', 100)
	}

	@Bean
	ActiveMQComponent activeMQComponent(@Value('${activemq.url:vm://localhost?broker.persistent=false}') String url) {
		return new ActiveMQComponent(brokerURL: url)
	}

	GroovyDsl dsl =
	// tag::sample_dsl[]
	io.codearte.accurest.dsl.GroovyDsl.make {
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

	GroovyDsl dsl2 =
	// tag::sample_dsl_2[]
	io.codearte.accurest.dsl.GroovyDsl.make {
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

	GroovyDsl dsl3 =
	// tag::sample_dsl_3[]
	io.codearte.accurest.dsl.GroovyDsl.make {
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
