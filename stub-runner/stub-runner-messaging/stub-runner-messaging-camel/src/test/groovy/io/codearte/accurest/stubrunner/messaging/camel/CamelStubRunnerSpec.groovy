package io.codearte.accurest.stubrunner.messaging.camel

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
			camelContext.createProducerTemplate().sendBodyAndHeaders('jms:input', new BookReturned('foo'), [sample: 'header'])
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertJsons(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a message by label'() {
		when:
			stubFinder.trigger('return_book_1')
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertJsons(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		when:
			stubFinder.trigger('io.codearte.accurest.stubs:camelService', 'return_book_1')
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertJsons(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing artifactId'() {
		when:
			stubFinder.trigger('camelService', 'return_book_1')
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertJsons(receivedMessage.in.body)
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
			stubFinder.trigger()
		then:
			Exchange receivedMessage = camelContext.createConsumerTemplate().receive('jms:output', 5000)
		and:
			receivedMessage != null
			assertJsons(receivedMessage.in.body)
			receivedMessage.in.headers.get('BOOK-NAME') == 'foo'
	}

	private boolean assertJsons(Object payload) {
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
	}

	@Bean
	ActiveMQComponent activeMQComponent(@Value('${activemq.url:vm://localhost?broker.persistent=false}') String url) {
		return new ActiveMQComponent(brokerURL: url)
	}
}
