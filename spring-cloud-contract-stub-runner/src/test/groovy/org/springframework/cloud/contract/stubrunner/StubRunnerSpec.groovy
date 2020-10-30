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

package org.springframework.cloud.contract.stubrunner


import spock.lang.Specification

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages
import org.springframework.util.SocketUtils

class StubRunnerSpec extends Specification {

	private static final int MIN_PORT = SocketUtils.findAvailableTcpPort()
	private static final int MAX_PORT = MIN_PORT
	private static final URL EXPECTED_STUB_URL = new URL("http://localhost:$MIN_PORT")

	private static final URL generateStubs = StubRunnerSpec.getResource('/generateStubs/')

	def 'should provide stub URL for provided groupid and artifactId'() {
		given:
			Arguments args = argumentsWithProjectDefinition()
			StubRunner runner = new StubRunner(args.stubRunnerOptions,
					args.repositoryPath, args.stub)
		when:
			runner.runStubs()
		then:
			runner.findStubUrl("groupId", "artifactId") == EXPECTED_STUB_URL
		cleanup:
			runner.close()
	}

	def 'should provide stub URL if only artifactId was passed'() {
		given:
			Arguments args = argumentsWithProjectDefinition()
			StubRunner runner = new StubRunner(args.stubRunnerOptions,
					args.repositoryPath, args.stub)
		when:
			runner.runStubs()
		then:
			runner.findStubUrl(null, "artifactId") == EXPECTED_STUB_URL
		cleanup:
			runner.close()
	}

	def 'should generate stubs at runtime'() {
		given:
			Arguments args = argumentsWithGenerateStubs()
			StubDownloader downloader = new FileStubDownloader().build(args.stubRunnerOptions)
			StubRunner runner = new StubRunnerFactory(args.stubRunnerOptions,
					downloader, new NoOpStubMessages()).createStubsFromServiceConfiguration().first()
		when:
			runner.runStubs()
		then:
			URL url = runner.findStubUrl("groupId2", "artifactId2")
			"Goodbye World!" == new URL(url.toString() + "/goodbye").text
		cleanup:
			runner.close()
	}

	def 'should override existing mappings when generating stubs at runtime'() {
		given:
			Arguments args = argumentsWithGenerateStubs()
			StubDownloader downloader = new FileStubDownloader().build(args.stubRunnerOptions)
			StubRunner runner = new StubRunnerFactory(args.stubRunnerOptions,
					downloader, new NoOpStubMessages()).createStubsFromServiceConfiguration().first()
		when:
			runner.runStubs()
		then:
			URL url = runner.findStubUrl("groupId2", "artifactId2")
			// don't return the response defined in hello.json, but the one defined in the contract
			"Hello New World!" == new URL(url.toString() + "/hello").text
		cleanup:
			runner.close()
	}

	def 'should handle contracts with body contents loaded from external file when generating stubs at runtime'() {
		given:
			Arguments args = argumentsWithGenerateStubs()
			StubDownloader downloader = new FileStubDownloader().build(args.stubRunnerOptions)
			StubRunner runner = new StubRunnerFactory(args.stubRunnerOptions,
					downloader, new NoOpStubMessages()).createStubsFromServiceConfiguration().first()
		when:
			runner.runStubs()
		then:
			URL url = runner.findStubUrl("groupId2", "artifactId2")
			"Goodbye from file!" == new URL(url.toString() + "/goodbye_from_file").text
		cleanup:
			runner.close()
	}

	Arguments argumentsWithProjectDefinition() {
		StubConfiguration stubConfiguration = new StubConfiguration("groupId", "artifactId", "classifier")
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder().withMinMaxPort(MIN_PORT, MAX_PORT).build()
		return new Arguments(stubRunnerOptions, 'src/test/resources/repository', stubConfiguration)
	}

	Arguments argumentsWithGenerateStubs() {
		StubConfiguration stubConfiguration = new StubConfiguration("groupId2", "artifactId2", "classifier2")
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
				.withMinMaxPort(MIN_PORT, MAX_PORT)
				.withGenerateStubs(true)
				.withStubs(stubConfiguration.toString())
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
				.withStubRepositoryRoot("stubs://file://" + generateStubs.path)
				.build()
		return new Arguments(stubRunnerOptions, 'src/test/resources/generateStubs', stubConfiguration)
	}

}
