package io.codearte.accurest.stubrunner

import spock.lang.Specification

class StubRunnerExecutorSpec extends Specification {

	static final URL EXPECTED_STUB_URL = new URL('http://localhost:8999')
	static final int MIN_PORT = 8999
	static final int MAX_PORT = 8999

	private AvailablePortScanner portScanner
	private StubRepository repository
	private StubConfiguration stub = new StubConfiguration("group:artifact", "stubs")

	def setup() {
		portScanner = new AvailablePortScanner(MIN_PORT, MAX_PORT)
		repository = new StubRepository(new File('src/test/resources/repository'))
	}

	def 'should provide URL for given relative path of stub'() {
		given:
		StubRunnerExecutor executor = new StubRunnerExecutor(portScanner)
		when:
		executor.runStubs(repository, stub)
		then:
		executor.findStubUrl("group", "artifact") == EXPECTED_STUB_URL
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
		executor.runStubs(repository, stub)
		then:
		!executor.findStubUrl("unkowngroup", "unknownartifact")
	}

}
