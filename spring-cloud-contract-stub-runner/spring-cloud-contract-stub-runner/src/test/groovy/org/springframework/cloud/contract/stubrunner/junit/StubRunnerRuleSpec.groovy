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

package org.springframework.cloud.contract.stubrunner.junit

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerRuleSpec extends Specification {

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.clearProperty("stubrunner.stubs.repository.root");
		System.clearProperty("stubrunner.stubs.classifier");
	}

	// tag::classrule[]
	@ClassRule @Shared StubRunnerRule rule = new StubRunnerRule()
			.repoRoot(StubRunnerRuleSpec.getResource("/m2repo/repository").toURI().toString())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "loanIssuance")
			.downloadStub("org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer")

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			rule.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance') != null
			rule.findStubUrl('loanIssuance') != null
			rule.findStubUrl('loanIssuance') == rule.findStubUrl('org.springframework.cloud.contract.verifier.stubs', 'loanIssuance')
			rule.findStubUrl('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer') != null
		and:
			rule.findAllRunningStubs().isPresent('loanIssuance')
			rule.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs', 'fraudDetectionServer')
			rule.findAllRunningStubs().isPresent('org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer')
		and: 'Stubs were registered'
			"${rule.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${rule.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}
	// end::classrule[]
}
