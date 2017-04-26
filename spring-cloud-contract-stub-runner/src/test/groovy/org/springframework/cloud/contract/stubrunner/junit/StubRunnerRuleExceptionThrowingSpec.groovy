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

package org.springframework.cloud.contract.stubrunner.junit

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerRuleExceptionThrowingSpec extends Specification {

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
	}

	@ClassRule @Shared StubRunnerRule rule = new StubRunnerRule()
			.repoRoot(StubRunnerRuleExceptionThrowingSpec.getResource("/m2repo/repository").toURI().toString())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "bootService")

	def 'should throw exception when no message verifier was passed and message related method was triggered'() {
		when:
			rule.trigger()
		then:
			thrown(UnsupportedOperationException)
		when:
			rule.trigger("return_book_1")
		then:
			thrown(UnsupportedOperationException)
		when:
			rule.trigger("bootService", "return_book_1")
		then:
			thrown(UnsupportedOperationException)
	}
}
