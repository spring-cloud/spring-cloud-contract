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

class AvailablePortScannerSpec extends Specification {

	private static final int MIN_PORT = 8989
	private static final int MAX_PORT = 8990
	private static final int MAX_RETRY_COUNT_FOR_NEGATIVE_SCENARIOS = 2

	def 'should execute given closure with the next available port number'() {
		given:
		AvailablePortScanner portScanner = new AvailablePortScanner(MIN_PORT, MAX_PORT)
		when:
		int usedPort = portScanner.tryToExecuteWithFreePort { int port -> port }
		then:
		noExceptionThrown()
		usedPort == MIN_PORT || MAX_PORT
	}

	def 'should execute given closure with the available port from specified range'() {
		given:
		AvailablePortScanner portScanner = new AvailablePortScanner(MIN_PORT, MIN_PORT)
		when:
		int usedPort = portScanner.tryToExecuteWithFreePort { int port -> port }
		then:
		noExceptionThrown()
		usedPort == MIN_PORT
	}

	def 'should throw exception when improper range has been provided'() {
		when:
		new AvailablePortScanner(minPort, maxPort, MAX_RETRY_COUNT_FOR_NEGATIVE_SCENARIOS)
		then:
		def ex = thrown(AvailablePortScanner.InvalidPortRange)
		ex.message == "Invalid bounds exceptions, min port [$minPort] is greater to max port [$maxPort]"
		where:
		minPort | maxPort
		MAX_PORT | MIN_PORT

	}

	def 'should throw exception when there is no available port'() {
		given:
		AvailablePortScanner portScanner = new AvailablePortScanner(MIN_PORT, MAX_PORT, MAX_RETRY_COUNT_FOR_NEGATIVE_SCENARIOS)
		when:
		portScanner.tryToExecuteWithFreePort {
			throw new BindException('Bind exception from closure')
		}
		then:
		def ex = thrown(AvailablePortScanner.NoPortAvailableException)
		ex.message == "Could not find available port in range $MIN_PORT:$MAX_PORT"
	}
}
