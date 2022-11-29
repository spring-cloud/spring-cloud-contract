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

package org.springframework.cloud.contract.stubrunner.messaging.integration

import java.util.concurrent.TimeUnit

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.assertj.core.api.BDDAssertions
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
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.messaging.Message
/**
 * @author Marcin Grzejszczak
 */
@ImportResource("classpath*:integration-context.xml")
@AutoConfigureStubRunner
@SpringBootTest(classes = Config)
@DisabledOnOs(OS.WINDOWS)
class IntegrationStubRunnerSpec {

	@Autowired
	StubFinder stubFinder
	@Autowired
	MessageVerifierReceiver<Message<?>> messaging

	@BeforeEach
	void setup() {
		// ensure that message were taken from the queue
		messaging.receive('outputTest', 100, TimeUnit.MILLISECONDS)
	}

	@Test
	void 'should trigger a message by label'() {
		when:
			// tag::client_trigger[]
			stubFinder.trigger('return_book_1')
			// end::client_trigger[]
		then:
			// tag::client_trigger_receive[]
			Message<?> receivedMessage = messaging.receive('outputTest')
			// end::client_trigger_receive[]
		and:
			// tag::client_trigger_message[]
			assert receivedMessage != null
			assert assertJsons(receivedMessage.payload)
			assert receivedMessage.headers.get('BOOK-NAME') == 'foo'
			// end::client_trigger_message[]
	}

	@Test
	void 'should trigger a label for the existing groupId and artifactId'() {
		when:
			// tag::trigger_group_artifact[]
			stubFinder.
					trigger('org.springframework.cloud.contract.verifier.stubs:integrationService', 'return_book_1')
			// end::trigger_group_artifact[]
		then:
			Message<?> receivedMessage = messaging.receive('outputTest')
		and:
			assert receivedMessage != null
			assert assertJsons(receivedMessage.payload)
			assert receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	@Test
	void 'should trigger a label for the existing artifactId'() {
		when:
			// tag::trigger_artifact[]
			stubFinder.trigger('integrationService', 'return_book_1')
			// end::trigger_artifact[]
		then:
			Message<?> receivedMessage = messaging.receive('outputTest')
		and:
			assert receivedMessage != null
			assert assertJsons(receivedMessage.payload)
			assert receivedMessage.headers.get('BOOK-NAME') == 'foo'
	}

	@Test
	void 'should throw exception when missing label is passed'() {
		when:
			BDDAssertions.thenThrownBy(() -> stubFinder.trigger('missing label')).isInstanceOf(IllegalArgumentException)
	}

	@Test
	void 'should throw exception when missing label and artifactid is passed'() {
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
			Message<?> receivedMessage = messaging.receive('outputTest')
		and:
			assert receivedMessage != null
			assert assertJsons(receivedMessage.payload)
			assert receivedMessage.headers.get('BOOK-NAME') == 'foo'
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
					sentTo('output')
					body('''{ "bookName" : "foo" }''')
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
	// end::sample_dsl[]

	@Configuration
	@ComponentScan
	@EnableAutoConfiguration
	static class Config {

	}

}
