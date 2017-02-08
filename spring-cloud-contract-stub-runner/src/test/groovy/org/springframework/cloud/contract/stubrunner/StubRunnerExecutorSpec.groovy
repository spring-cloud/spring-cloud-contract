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

import groovy.json.JsonOutput
import org.springframework.util.SocketUtils

import java.util.concurrent.TimeUnit

import org.springframework.cloud.contract.stubrunner.util.StubsParser
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier

import spock.lang.Specification

class StubRunnerExecutorSpec extends Specification {

	static final int MIN_PORT = 8999
	static final int MAX_PORT = 9999

	private AvailablePortScanner portScanner
	private StubRepository repository
	private StubConfiguration stub = new StubConfiguration("group:artifact", "stubs")
	private StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder().build()

	def setup() {
		portScanner = new AvailablePortScanner(MIN_PORT, MAX_PORT)
		repository = new StubRepository(new File('src/test/resources/repository'))
	}

	def 'should provide URL for given relative path of stub'() {
		given:
		StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
		executor.runStubs(stubRunnerOptions, repository, stub)
		then:
		URL url = executor.findStubUrl("group", "artifact")
		url.port >= MIN_PORT
		url.port <= MAX_PORT
		and:
		executor.findAllRunningStubs().isPresent('artifact')
		executor.findAllRunningStubs().isPresent('group', 'artifact')
		executor.findAllRunningStubs().isPresent('group:artifact')
		cleanup:
		executor.shutdown()
	}

	def 'should provide no URL for unknown dependency path'() {
		given:
		StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
		executor.runStubs(stubRunnerOptions, repository, stub)
		and:
		executor.findStubUrl("unkowngroup", "unknownartifact")
		then:
		thrown(StubNotFoundException)
		cleanup:
		executor.shutdown()
	}

	def 'should start a stub on a given port'() {
		given:
		int port = SocketUtils.findAvailableTcpPort()
		StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		stubRunnerOptions = new StubRunnerOptionsBuilder(stubIdsToPortMapping:
				stubIdsWithPortsFromString("group:artifact:${port},someotherartifact:${SocketUtils.findAvailableTcpPort()}"))
				.build()
		when:
		executor.runStubs(stubRunnerOptions, repository, stub)
		then:
		executor.findStubUrl("group", "artifact") == "http://localhost:${port}".toURL()
		cleanup:
		executor.shutdown()
	}

	def 'should ensure that triggered contracts have properly parsed message body when a message is sent'() {
		given:
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner, new AssertingStubMessages())
			executor.runStubs(stubRunnerOptions, repository, stub)
		when:
			executor.trigger('send_order')
		then:
			noExceptionThrown()
		cleanup:
		executor.shutdown()
	}

	def 'should match stub with empty classifier'() {
		given:
			def stubConf = new StubConfiguration('groupX', 'artifactX', 'versionX', '')
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
			executor.runStubs(stubRunnerOptions, repository, stubConf)
		then:
			URL url = executor.findStubUrl('groupX:artifactX:versionX:')
			url.port >= MIN_PORT
			url.port <= MAX_PORT
		and:
			executor.findAllRunningStubs().isPresent('artifactX')
			executor.findAllRunningStubs().isPresent('groupX', 'artifactX')
			executor.findAllRunningStubs().isPresent('groupX:artifactX')
		cleanup:
			executor.shutdown()
	}

	Map<StubConfiguration, Integer> stubIdsWithPortsFromString(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collectEntries { String entry ->
			return StubsParser.fromStringWithPort(entry)
		}
	}

	private class AssertingStubMessages implements MessageVerifier<Object> {
		
		@Override
		public void send(Object message, String destination) {
			throw new UnsupportedOperationException()
		}

		@Override
		public <T> void send(T payload, Map<String, Object> headers, String destination) {
			assert !(JsonOutput.toJson(payload).contains("serverValue"))
			assert headers.entrySet().every { !(it.value.toString().contains("serverValue")) }
		}

		@Override
		public Object receive(String destination, long timeout, TimeUnit timeUnit) {
			throw new UnsupportedOperationException()
		}

		@Override
		public Object receive(String destination) {
			throw new UnsupportedOperationException()
		}

	}

}
