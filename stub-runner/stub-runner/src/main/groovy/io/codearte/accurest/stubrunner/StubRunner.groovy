package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Represents a single instance of ready-to-run stubs.
 * Can run the stubs and then will return the name of the collaborator together with
 * its URI.
 * Can also be queried if the current groupid and artifactid are matching the
 * corresponding running stub.
 */
@Slf4j
@CompileStatic
class StubRunner implements StubRunning {

	private StubRunnerExecutor localStubRunner
	private final Arguments arguments
	private final StubRepository stubRepository

	StubRunner(Arguments arguments) {
		this.arguments = arguments
		this.stubRepository = new StubRepository(new File(arguments.repositoryPath))
	}

	@Override
	RunningStubs runStubs() {
		AvailablePortScanner portScanner = new AvailablePortScanner(arguments.stubRunnerOptions.minPortValue,
				arguments.stubRunnerOptions.maxPortValue)
		localStubRunner = new StubRunnerExecutor(portScanner)
		registerShutdownHook()
		return localStubRunner.runStubs(stubRepository, arguments.stub)
	}

	@Override
	URL findStubUrl(String groupId, String artifactId) {
		return localStubRunner.findStubUrl(groupId, artifactId)
	}

	@Override
	URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length == 1) {
			throw new IllegalArgumentException("$ivyNotation is invalid")
		}
		return findStubUrl(splitString[0], splitString[1])
	}

	private void registerShutdownHook() {
		Runnable stopAllServers = { this.close() }
		Runtime.runtime.addShutdownHook(new Thread(stopAllServers))
	}

	@Override
	void close() throws IOException {
		localStubRunner?.shutdown()
	}
}