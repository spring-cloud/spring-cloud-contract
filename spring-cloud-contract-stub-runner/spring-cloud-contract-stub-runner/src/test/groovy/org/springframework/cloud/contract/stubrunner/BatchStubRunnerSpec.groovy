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

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification

class BatchStubRunnerSpec extends Specification {

	static final String KNOWN_STUB_PATH = 'group:knownArtifact'
	static final String UNKNOWN_STUB_PATH = 'group:unknownArtifact'
	static final URL KNOWN_STUB_URL = new URL('http://localhost:8080')

	def 'should provide stub URL from enclosed stub runner'() {
		given:
		BatchStubRunner batchStubRunner = new BatchStubRunner(runners())
		expect:
		batchStubRunner.findStubUrl(KNOWN_STUB_PATH) == KNOWN_STUB_URL
	}

	def 'should return empty optional for unknown stub path'() {
		given:
		BatchStubRunner batchStubRunner = new BatchStubRunner(runners())
		expect:
		!batchStubRunner.findStubUrl(UNKNOWN_STUB_PATH)
	}

	def 'should throw exception if trying to execute not available trigger'() {
		given:
		BatchStubRunner batchStubRunner = new BatchStubRunner(runners())
		when:
		batchStubRunner.trigger('non existing label')
		then:
		IllegalArgumentException exception = thrown(IllegalArgumentException)
		exception.message == "No label with name [non existing label] was found. Here you have the list of dependencies and their labels [Dependency [a:b:c] has labels [foo]]"
	}

	Collection<StubRunner> runners() {
		StubRunner runner = Mock(StubRunner)
		runner.findStubUrl("group", "knownArtifact") >> KNOWN_STUB_URL
		runner.findStubUrl("group", "unknownArtifact") >> null
		runner.labels() >> ['a:b:c' : ['foo']]
		return [runner]
	}

}
