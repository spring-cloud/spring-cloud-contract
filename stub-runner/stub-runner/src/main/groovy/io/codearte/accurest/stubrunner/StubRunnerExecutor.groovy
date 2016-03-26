package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Runs stubs for a particular {@link StubServer}
 */
@CompileStatic
@Slf4j
class StubRunnerExecutor implements StubFinder {

	private final AvailablePortScanner portScanner
	private StubServer stubServer

	StubRunnerExecutor(AvailablePortScanner portScanner) {
		this.portScanner = portScanner
	}

	RunningStubs runStubs(StubRepository repository, StubConfiguration stubConfiguration) {
		startStubServers(stubConfiguration, repository)
		RunningStubs runningCollaborators =
				new RunningStubs([(stubServer.stubConfiguration): stubServer.port])
		log.info("All stubs are now running [${runningCollaborators.toString()}")
		return runningCollaborators
	}

	void shutdown() {
		stubServer?.stop()
	}

	@Override
	URL findStubUrl(String groupId, String artifactId) {
		if(!groupId) {
			return returnStubUrlIfMatches(stubServer.stubConfiguration.artifactId == artifactId)
		}
		return returnStubUrlIfMatches(stubServer.stubConfiguration.artifactId == artifactId &&
				stubServer.stubConfiguration.groupId == groupId)
	}

	@Override
	URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length == 1) {
			throw new IllegalArgumentException("$ivyNotation is invalid")
		}
		return findStubUrl(splitString[0], splitString[1])
	}

	private URL returnStubUrlIfMatches(boolean condition) {
		return condition ? stubServer.stubUrl : null
	}

	private void startStubServers(StubConfiguration stubConfiguration, StubRepository repository) {
		List<MappingDescriptor> mappings = repository.getProjectDescriptors()
		stubServer =  portScanner.tryToExecuteWithFreePort { int availablePort ->
			return new StubServer(availablePort, stubConfiguration, mappings).start()
		}
	}

}
