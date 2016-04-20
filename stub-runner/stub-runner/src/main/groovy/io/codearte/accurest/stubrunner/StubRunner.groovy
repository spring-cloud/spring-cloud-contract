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

	private final StubRepository stubRepository
	private final StubConfiguration stubsConfiguration
	private final StubRunnerOptions stubRunnerOptions
	private StubRunnerExecutor localStubRunner

	@Deprecated
	StubRunner(Arguments arguments) {
		stubsConfiguration = arguments.stub
		stubRunnerOptions = arguments.stubRunnerOptions
		this.stubRepository = new StubRepository(new File(arguments.repositoryPath))
	}

	StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath, StubConfiguration stubsConfiguration) {
		this.stubsConfiguration = stubsConfiguration
		this.stubRunnerOptions = stubRunnerOptions
		this.stubRepository = new StubRepository(new File(repositoryPath))
	}

	@Override
	RunningStubs runStubs() {
		AvailablePortScanner portScanner = new AvailablePortScanner(stubRunnerOptions.minPortValue,
				stubRunnerOptions.maxPortValue)
		localStubRunner = new StubRunnerExecutor(portScanner)
		registerShutdownHook()

		return localStubRunner.runStubs(stubRepository, stubsConfiguration)
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

	@Override
	RunningStubs findAllRunningStubs() {
		return localStubRunner.findAllRunningStubs()
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