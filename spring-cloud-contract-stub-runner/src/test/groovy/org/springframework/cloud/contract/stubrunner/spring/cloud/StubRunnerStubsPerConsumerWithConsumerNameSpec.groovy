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

package org.springframework.cloud.contract.stubrunner.spring.cloud

import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.messaging.Message
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
/**
 * @author Marcin Grzejszczak
 */
// tag::test[]
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest
@AutoConfigureStubRunner(ids = "org.springframework.cloud.contract.verifier.stubs:producerWithMultipleConsumers",
		repositoryRoot = "classpath:m2repo/repository/",
		consumerName = "foo-consumer",
		stubsPerConsumer = true)
@DirtiesContext
class StubRunnerStubsPerConsumerWithConsumerNameSpec extends Specification {
// end::test[]

	@Autowired StubFinder stubFinder
	@Autowired Environment environment
	@Autowired MessageVerifier<Message<?>> messaging
	TestRestTemplate template = new TestRestTemplate()

	def 'should start http stub servers for foo-consumer only'() {
		given:
			URL stubUrl = stubFinder.findStubUrl('producerWithMultipleConsumers')
		when:
			ResponseEntity entity = template.getForEntity("${stubUrl}/foo-consumer", String)
		then:
			entity.statusCode.value() == 200
		when:
			entity = template.getForEntity("${stubUrl}/bar-consumer", String)
		then:
			entity.statusCode.value() == 404
	}

	def 'should trigger a message by label from proper consumer'() {
		when:
			stubFinder.trigger('return_book_for_foo')
		then:
			Message<?> receivedMessage = messaging.receive('output')
		and:
			receivedMessage != null
			receivedMessage.payload == '''{"bookName":"foo_for_foo"}'''
			receivedMessage.headers.get('BOOK-NAME') == 'foo_for_foo'
	}

	def 'should not trigger a message by the not matching consumer'() {
		when:
			stubFinder.trigger('return_book_for_bar')
		then:
			IllegalArgumentException e = thrown(IllegalArgumentException)
			e.message.contains("No label with name [return_book_for_bar] was found")
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableBinding(Sink)
	static class Config {}
}