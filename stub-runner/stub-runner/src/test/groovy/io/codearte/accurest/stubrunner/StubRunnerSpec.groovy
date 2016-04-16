package io.codearte.accurest.stubrunner

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
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptions(minPortValue: MIN_PORT, maxPortValue: MAX_PORT)
		return new Arguments(stubRunnerOptions, 'src/test/resources/repository', stubConfiguration)
	}

}
