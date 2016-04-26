package io.codearte.accurest.stubrunner.messaging.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.stubrunner.StubFinder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
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
@ContextConfiguration(classes = StreamStubRunnerSpec, loader = SpringApplicationContextLoader)
class StreamStubRunnerSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired AccurestMessaging messaging

	def setup() {
		// ensure that message were taken from the queue
		messaging.receiveMessage('output', 100, TimeUnit.MILLISECONDS)
	}

	def 'should download the stub and register a route for it'() {
		when:
		// tag::client_send[]
			messaging.send(new BookReturned('foo'), [sample: 'header'], 'input')
		// end::client_send[]
		then:
		// tag::client_receive[]
			AccurestMessage receivedMessage = messaging.receiveMessage('output')
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
			AccurestMessage receivedMessage = messaging.receiveMessage('output')
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
			stubFinder.trigger('io.codearte.accurest.stubs:streamService', 'return_book_1')
		// end::trigger_group_artifact[]
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('output')
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
			AccurestMessage receivedMessage = messaging.receiveMessage('output')
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
			AccurestMessage receivedMessage = messaging.receiveMessage('output')
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
			messaging.send(new BookReturned('not_matching'), [wrong: 'header_value'], 'input')
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('output', 100, TimeUnit.MILLISECONDS)
		and:
			receivedMessage == null
	}

	private boolean assertJsons(Object payload) {
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
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
