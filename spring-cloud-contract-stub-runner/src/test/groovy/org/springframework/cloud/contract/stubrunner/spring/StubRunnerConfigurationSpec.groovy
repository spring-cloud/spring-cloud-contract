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

package org.springframework.cloud.contract.stubrunner.spring

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */

// Not necessary if Spring Cloud is used. TODO: make it work without this.
// tag::test[]
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@IntegrationTest(["stubrunner.cloud.enabled=false", "stubrunner.camel.enabled=false"])
@AutoConfigureStubRunner
@DirtiesContext
class StubRunnerConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.clearProperty("stubrunner.stubs.repository.root");
		System.clearProperty("stubrunner.stubs.classifier");
	}

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') == stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance')
			stubFinder.findStubUrl('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer') != null
		and:
			stubFinder.findAllRunningStubs().isPresent('loanIssuance')
			stubFinder.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs', 'fraudDetectionServer')
			stubFinder.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer')
		and: 'Stubs were registered'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {}
}
// end::test[]