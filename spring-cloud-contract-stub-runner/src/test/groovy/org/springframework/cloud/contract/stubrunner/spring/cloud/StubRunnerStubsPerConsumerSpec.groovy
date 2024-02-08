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

package org.springframework.cloud.contract.stubrunner.spring.cloud

import java.util.function.Function

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.messaging.Message
import org.springframework.test.context.ActiveProfiles
/**
 * @author Marcin Grzejszczak
 */
// tag::test[]
@SpringBootTest(classes = Config, properties = ["spring.application.name=bar-consumer"])
@AutoConfigureStubRunner(ids = "org.springframework.cloud.contract.verifier.stubs:producerWithMultipleConsumers",
		repositoryRoot = "classpath:m2repo/repository/",
		stubsMode = StubRunnerProperties.StubsMode.REMOTE,
		stubsPerConsumer = true)
@ActiveProfiles("streamconsumer")
class StubRunnerStubsPerConsumerSpec {
// end::test[]

	@Autowired
	StubFinder stubFinder
	@Autowired
	Environment environment
	@Autowired
	MessageVerifierReceiver<Message<?>> messaging
	TestRestTemplate template = new TestRestTemplate()

	@Test
	void 'should start http stub servers for bar-consumer only'() {
		given:
		URL stubUrl = stubFinder.findStubUrl('producerWithMultipleConsumers')
		when:
		ResponseEntity entity = template.getForEntity("${stubUrl}/bar-consumer", String)
		then:
		assert entity.statusCode.value() == 200
		when:
		entity = template.getForEntity("${stubUrl}/foo-consumer", String)
		then:
		assert entity.statusCode.value() == 404
	}

	@Test
	void 'should trigger a message by label from proper consumer'() {
		when:
		stubFinder.trigger('return_book_for_bar')
		then:
		Message<?> receivedMessage = messaging.receive('output')
		and:
		assert receivedMessage != null
		assert receivedMessage.payload == '''{"bookName":"foo_for_bar"}'''.bytes
		assert receivedMessage.headers.get('BOOK-NAME') == 'foo_for_bar'
	}

	@Test
	void 'should not trigger a message by the not matching consumer'() {
		when:
		BDDAssertions.thenThrownBy(() -> stubFinder.trigger('return_book_for_foo')).isInstanceOf(IllegalArgumentException).hasMessageContaining("No label with name [return_book_for_foo] was found")
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
	static class Config {
		@Bean
		Function output() {
			return { Object o ->
				println(o)
				return o
			}
		}
	}
}
