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

package org.springframework.cloud.contract.stubrunner.provider.moco

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
/**
 * @author Marcin Grzejszczak
 */
// tag::[classpath_stub_runner]
@SpringBootTest(classes = MocoConfig, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = ["com.example:fraudDetectionServerMoco"],
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
// end::[classpath_stub_runner]
@ActiveProfiles("test")
class MocoHttpServerStubSpec {

	@Autowired
	StubFinder stubFinder

	@Test
	void 'should successfully receive a response from a stub'() {
		given:
			String url = stubFinder.findStubUrl('fraudDetectionServerMoco').toString()
		expect:
			assert "${url.toString()}/name".toURL().text == 'fraudDetectionServerMoco'
			assert "${url.toString()}/bye".toURL().text == 'bye'
			assert "${url.toString()}/bye2".toURL().text == 'bye'
		and:
			BDDAssertions.thenThrownBy(() -> "${url.toString()}/name2".toURL().text).isInstanceOf(IOException)
	}

	@Configuration
	@EnableAutoConfiguration
	static class MocoConfig {
	}

}

