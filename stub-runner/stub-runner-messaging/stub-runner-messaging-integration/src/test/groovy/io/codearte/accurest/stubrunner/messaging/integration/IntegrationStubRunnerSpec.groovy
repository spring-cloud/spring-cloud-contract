package io.codearte.accurest.stubrunner.messaging.integration

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.stubrunner.StubFinder
import org.springframework.beans.factory.BeanNotOfRequiredTypeException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.TimeUnit
/**
 * @author Marcin Grzejszczak
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@ContextConfiguration(classes = IntegrationStubRunnerSpec, loader = SpringApplicationContextLoader)
@ImportResource("classpath*:integration-context.xml")
class IntegrationStubRunnerSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired AccurestMessaging messaging

	def 'should download the stub and register a route for it'() {
		when:
			messaging.send(new BookReturned('foo'), [sample: 'header'], 'input')
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('outputTest')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a message by label'() {
		when:
			stubFinder.trigger('return_book_1')
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('outputTest')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing groupId:artifactId'() {
		when:
			stubFinder.trigger('io.codearte.accurest.stubs:integrationService', 'return_book_1')
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('outputTest')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should trigger a label for the existing artifactId'() {
		when:
			stubFinder.trigger('integrationService', 'return_book_1')
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('outputTest')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	def 'should not run any wrong trigger when missing label is passed'() {
		given:
			stubFinder.trigger('missing label')
		when:
			messaging.receiveMessage('output', 100, TimeUnit.MILLISECONDS)
		then:
			Exception e = thrown(RuntimeException)
			e.cause.class == BeanNotOfRequiredTypeException
	}

	def 'should not run any wrong trigger when missing label and artifactid is passed'() {
		given:
			stubFinder.trigger('some:service', 'return_book_1')
		when:
			messaging.receiveMessage('output', 100, TimeUnit.MILLISECONDS)
		then:
			Exception e = thrown(RuntimeException)
			e.cause.class == BeanNotOfRequiredTypeException
	}

	def 'should trigger messages by running all triggers'() {
		when:
			stubFinder.trigger()
		then:
			AccurestMessage receivedMessage = messaging.receiveMessage('outputTest')
		and:
			receivedMessage != null
			assertJsons(receivedMessage.payload)
			receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	private boolean assertJsons(Object payload) {
		String objectAsString = payload instanceof String ? payload :
				JsonOutput.toJson(payload)
		def json = new JsonSlurper().parseText(objectAsString)
		return json.bookName == 'foo'
	}

}
