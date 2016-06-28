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

import groovy.json.JsonOutput
import org.springframework.cloud.contract.stubrunner.util.StubsParser
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import spock.lang.Specification

import java.util.concurrent.TimeUnit

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
		then:
		!executor.findStubUrl("unkowngroup", "unknownartifact")
	}

	def 'should start a stub on a given port'() {
		given:
		StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		stubRunnerOptions = new StubRunnerOptionsBuilder(stubIdsToPortMapping: stubIdsWithPortsFromString('group:artifact:12345,someotherartifact:123'))
				.build()
		when:
		executor.runStubs(stubRunnerOptions, repository, stub)
		then:
		executor.findStubUrl("group", "artifact") == 'http://localhost:12345'.toURL()
	}

	def 'should ensure that triggered contracts have properly parsed message body when a message is sent'() {
		given:
			StubRunnerExecutor executor = new StubRunnerExecutor(portScanner, new AssertingContractVerifierMessaging())
			executor.runStubs(stubRunnerOptions, repository, stub)
		when:
			executor.trigger('send_order')
		then:
			noExceptionThrown()
	}

	Map<StubConfiguration, Integer> stubIdsWithPortsFromString(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collectEntries { String entry ->
			return StubsParser.fromStringWithPort(entry)
		}
	}

	private class AssertingContractVerifierMessaging implements ContractVerifierMessaging {

		@Override
		void send(ContractVerifierMessage message, String destination) {

		}

		@Override
		ContractVerifierMessage receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
			return null
		}

		@Override
		ContractVerifierMessage receiveMessage(String destination) {
			return null
		}

		@Override
		void send(Object o, Map headers, String destination) {

		}

		@Override
		ContractVerifierMessage create(Object o, Map headers) {
			assert !(JsonOutput.toJson(o).contains("serverValue"))
			assert headers.entrySet().every { !(it.value.toString().contains("serverValue")) }
			return null
		}

		@Override
		ContractVerifierMessage create(Object o) {
			return null
		}
	}

}
