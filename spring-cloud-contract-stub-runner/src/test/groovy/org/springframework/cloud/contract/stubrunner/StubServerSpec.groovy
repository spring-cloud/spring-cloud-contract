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

import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStub
import org.springframework.cloud.test.TestSocketUtils

class StubServerSpec extends Specification {
	static final int STUB_SERVER_PORT = TestSocketUtils.findAvailableTcpPort()
	static final URL EXPECTED_URL = new URL("http://localhost:$STUB_SERVER_PORT")

	File repository = new File('src/test/resources/repository/mappings/spring/cloud/bye')
	StubConfiguration stubConfiguration = new StubConfiguration("a:b")

	def 'should register stub mappings upon server start'() {
		given:
			List<File> mappingDescriptors = new StubRepository(repository).getStubs()
			StubServer pingStubServer = new StubServer(stubConfiguration, mappingDescriptors, [],
					new WireMockHttpServerStub()).start(new HttpServerStubConfiguration(new HttpServerStubConfigurer.NoOpHttpServerStubConfigurer(), StubRunnerOptions.fromSystemProps(), new StubConfiguration("a:b:c:d"), STUB_SERVER_PORT))
		when:
			pingStubServer.start()
		then:
			"http://localhost:$pingStubServer.port/bye".toURL().text == 'Goodbye world!'
		cleanup:
			pingStubServer.stop()
	}

	def 'should provide stub server URL'() {
		given:
			List<File> mappingDescriptors = new StubRepository(repository).getStubs()
			StubServer pingStubServer = new StubServer(stubConfiguration, mappingDescriptors, [],
					new WireMockHttpServerStub()).start(new HttpServerStubConfiguration(new HttpServerStubConfigurer.NoOpHttpServerStubConfigurer(), StubRunnerOptions.fromSystemProps(), new StubConfiguration("a:b:c:d"), STUB_SERVER_PORT))
		when:
			pingStubServer.start()
		then:
			pingStubServer.stubUrl == EXPECTED_URL
		cleanup:
			pingStubServer.stop()
	}
}
