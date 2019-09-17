/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring


import spock.lang.Specification

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
/**
 * @author Marcin Grzejszczak
 */

@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(properties = ['some.property1=org.springframework.cloud.contract.verifier.stubs:loanIssuance'])
@AutoConfigureStubRunner
@ActiveProfiles("test-with-placeholders")
class StubRunnerOptionsBuilderSpec extends Specification {

	@StubRunnerPort("fraudDetectionServer")
	int fraudDetectionServerPort

	@StubRunnerPort("loanIssuance")
	int loanIssuancePort

	@Value('${stub.port}')
	int stubPort

	def 'should resolve placeholders'() {
		expect:
			fraudDetectionServerPort > 1000
			loanIssuancePort > 1000
		and:
			stubPort == fraudDetectionServerPort
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {}
}
