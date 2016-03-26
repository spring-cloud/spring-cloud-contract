package io.codearte.accurest.stubrunner

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
		usedPort == MIN_PORT
	}

	def 'should throw exception when improper range has been provided'() {
		when:
		new AvailablePortScanner(minPort, maxPort, MAX_RETRY_COUNT_FOR_NEGATIVE_SCENARIOS)
		then:
		def ex = thrown(AvailablePortScanner.InvalidPortRange)
		ex.message == "Invalid bounds exceptions, min port [$minPort] is greater or equal to max port [$maxPort]"
		where:
		minPort | maxPort
		MIN_PORT | MIN_PORT
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
