/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.provider.moco

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = MocoConfig, loader = SpringBootContextLoader)
// tag::[classpath_stub_runner]
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// to use stubs from classpath just provide ids without passing any other properties to
// @AutoConfigureStubRunner
@AutoConfigureStubRunner( ids = ["com.example:fraudDetectionServerMoco"])
// end::[classpath_stub_runner]
@DirtiesContext
@ActiveProfiles("test")
class MocoHttpServerStubSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired MyListener myListener

	def 'should successfully receive a response from a stub'() {
		given:
			String url = stubFinder.findStubUrl('fraudDetectionServerMoco').toString()
		expect:
			"${url.toString()}/name".toURL().text == 'fraudDetectionServerMoco'
			"${url.toString()}/bye".toURL().text == 'bye'
			"${url.toString()}/bye2".toURL().text == 'bye'
		when:
			"${url.toString()}/name2".toURL().text
		then:
			thrown(IOException)
		when:
			stubFinder.trigger("send_order")
		then:
			myListener.model?.description == "This is the order description"
		when:
			myListener.model = null
			stubFinder.trigger("send_order2")
		then:
			myListener.model?.description == "This is the order description"
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableBinding(Sink.class)
	static class MocoConfig {

		@Bean
		MyListener myListener() {
			return new MyListener()
		}
	}

	@Component
	static class MyListener {

		Model model

		@StreamListener(Sink.INPUT)
		void listen(Model data) {
			this.model = data
		}
	}

	static class Model {
		String uuid
		String description
	}
}

