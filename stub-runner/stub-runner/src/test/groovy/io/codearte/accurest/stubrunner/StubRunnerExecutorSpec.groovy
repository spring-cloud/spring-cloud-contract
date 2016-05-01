package io.codearte.accurest.stubrunner

import io.codearte.accurest.stubrunner.util.StubsParser
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

	Map<StubConfiguration, Integer> stubIdsWithPortsFromString(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collectEntries { String entry ->
			return StubsParser.fromStringWithPort(entry)
		}
	}

}
