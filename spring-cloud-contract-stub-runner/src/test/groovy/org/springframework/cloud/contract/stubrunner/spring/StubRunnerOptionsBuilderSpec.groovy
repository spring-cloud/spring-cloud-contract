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

package org.springframework.cloud.contract.stubrunner.spring

import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
/**
 * @author Marcin Grzejszczak
 */

@SpringBootTest(classes = Config, properties = ['some.property1=org.springframework.cloud.contract.verifier.stubs:loanIssuance'])
@AutoConfigureStubRunner
@ActiveProfiles("test-with-placeholders")
class StubRunnerOptionsBuilderSpec {

	@StubRunnerPort("fraudDetectionServer")
	int fraudDetectionServerPort

	@StubRunnerPort("loanIssuance")
	int loanIssuancePort

	@Value('${stub.port}')
	int stubPort

	@Test
	void 'should resolve placeholders'() {
		expect:
			assert fraudDetectionServerPort > 1000
			assert loanIssuancePort > 1000
		and:
			assert stubPort == fraudDetectionServerPort
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {}
}
