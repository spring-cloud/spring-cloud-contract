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

import java.util.concurrent.TimeUnit

import groovy.json.JsonOutput
import spock.lang.Specification

import org.springframework.cloud.contract.stubrunner.util.StubsParser
import org.springframework.cloud.contract.verifier.converter.YamlContract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender
import org.springframework.cloud.test.TestSocketUtils

class StubRunnerExecutorSpec extends Specification {

	static final int MIN_PORT = 18999
	static final int MAX_PORT = 19999

	private AvailablePortScanner portScanner
	private StubRepository repository
	private StubConfiguration stub = new StubConfiguration("group:artifact", "stubs")
	private StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder().build()

	def setup() {
		portScanner = new AvailablePortScanner(MIN_PORT, MAX_PORT)
		repository = new StubRepository(new File('src/test/resources/repository'),
				[], new StubRunnerOptionsBuilder().build())
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
			int port = TestSocketUtils.findAvailableTcpPort()
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
			stubRunnerOptions = new StubRunnerOptionsBuilder(stubIdsToPortMapping:
					stubIdsWithPortsFromString("group:artifact:${port},someotherartifact:${TestSocketUtils.findAvailableTcpPort()}"))
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
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner, new AssertingStubMessages(), [])
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

	def 'should return false if no messages are found'() {
		given:
			def stubConf = new StubConfiguration('asd', 'asd', 'asd', '')
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
			executor.runStubs(stubRunnerOptions,
					new StubRepository(new File('src/test/resources/repository/httpcontract'),
							[], new StubRunnerOptionsBuilder().build()), stubConf)
		then:
			!executor.trigger()
			!executor.trigger("missing", "label")
			!executor.trigger("label")
		cleanup:
			executor.shutdown()
	}

	def 'should not start http server if no contracts or mappings are found'() {
		given:
			def stubConf = new StubConfiguration('asd', 'asd', 'asd', '')
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
			RunningStubs stubs = executor.runStubs(stubRunnerOptions,
					new StubRepository(new File('src/test/resources/emptyrepo'),
							[], new StubRunnerOptionsBuilder().build()), stubConf)
		then:
			stubs.getPort('asd') == -1
		cleanup:
			executor.shutdown()
	}

	def 'should generate regex values when message is to be set and it contains regex'() {
		given:
			MockMessageVerifier messageVerifier = new MockMessageVerifier()
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner, messageVerifier, [])
		when:
			def stubConf = new StubConfiguration('asd', 'asd', 'asd', '')
			executor.runStubs(stubRunnerOptions,
					new StubRepository(new File('src/test/resources/messages'),
							[], new StubRunnerOptionsBuilder().build()), stubConf)
			boolean triggered = executor.trigger("trigger")
		then:
			triggered
			messageVerifier.called
		cleanup:
			executor.shutdown()
	}

	class MockMessageVerifier implements MessageVerifierSender, MessageVerifierReceiver {

		boolean called

		@Override
		void send(Object message, String destination, YamlContract contract) {

		}

		@Override
		Object receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
			return null
		}

		@Override
		Object receive(String destination, YamlContract contract) {
			return null
		}

		@Override
		void send(Object payload, Map headers, String destination, YamlContract contract) {
			this.called = true
			println "Body <${payload}>"
			assert !payload.toString().contains("cursor")
			println "Headers <${headers}>"
			assert headers.values().every { !it.toString().contains("cursor") }
		}
	}

	Map<StubConfiguration, Integer> stubIdsWithPortsFromString(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collectEntries { String entry ->
			return StubsParser.fromStringWithPort(entry)
		}
	}

	private class AssertingStubMessages implements MessageVerifierSender<Object>, MessageVerifierReceiver<Object> {

		@Override
		void send(Object message, String destination, YamlContract contract) {
			throw new UnsupportedOperationException()
		}

		@Override
		<T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
			assert !(JsonOutput.toJson(payload).contains("serverValue"))
			assert headers.entrySet().every { !(it.value.toString().contains("serverValue")) }
		}

		@Override
		Object receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
			throw new UnsupportedOperationException()
		}

		@Override
		Object receive(String destination, YamlContract contract) {
			throw new UnsupportedOperationException()
		}

	}

}
