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

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification

class StubRunnerSpec extends Specification {

	private static final int MIN_PORT = 8111
	private static final int MAX_PORT = 8111
	private static final URL EXPECTED_STUB_URL = new URL("http://localhost:$MIN_PORT")

	def 'should provide stub URL for provided groupid and artifactId'() {
		given:
		StubRunner runner = new StubRunner(argumentsWithProjectDefinition())
		when:
		runner.runStubs()
		then:
		runner.findStubUrl("groupId", "artifactId") == EXPECTED_STUB_URL
		cleanup:
		runner.close()
	}

	def 'should provide stub URL if only artifactId was passed'() {
		given:
		StubRunner runner = new StubRunner(argumentsWithProjectDefinition())
		when:
		runner.runStubs()
		then:
		runner.findStubUrl(null, "artifactId") == EXPECTED_STUB_URL
		cleanup:
		runner.close()
	}

	Arguments argumentsWithProjectDefinition() {
		StubConfiguration stubConfiguration = new StubConfiguration("groupId", "artifactId", "classifier")
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder().withMinMaxPort(MIN_PORT, MAX_PORT).build()
		return new Arguments(stubRunnerOptions, 'src/test/resources/repository', stubConfiguration)
	}

}
