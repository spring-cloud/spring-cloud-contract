package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.messaging.noop.NoOpAccurestMessaging

/**
 * Runs stubs for a particular {@link StubServer}
 */
@CompileStatic
@Slf4j
class StubRunnerExecutor implements StubFinder {

	private final AvailablePortScanner portScanner
	private final AccurestMessaging accurestMessaging
	private StubServer stubServer

	StubRunnerExecutor(AvailablePortScanner portScanner, AccurestMessaging accurestMessaging) {
		this.portScanner = portScanner
		this.accurestMessaging = accurestMessaging
	}

	StubRunnerExecutor(AvailablePortScanner portScanner) {
		this.portScanner = portScanner
		this.accurestMessaging = new NoOpAccurestMessaging()
	}

	RunningStubs runStubs(StubRunnerOptions stubRunnerOptions, StubRepository repository, StubConfiguration stubConfiguration) {
		startStubServers(stubRunnerOptions, stubConfiguration, repository)
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
		if (!groupId) {
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

	@Override
	RunningStubs findAllRunningStubs() {
		return new RunningStubs([(stubServer.stubConfiguration) : stubServer.port])
	}

	@Override
	Map<StubConfiguration, Collection<GroovyDsl>> getAccurestContracts() {
		return [(stubServer.stubConfiguration): stubServer.contracts]
	}

	@Override
	boolean trigger(String ivyNotationAsString, String labelName) {
		Collection<GroovyDsl> matchingContracts = getAccurestContracts().findAll {
			it.key.groupIdAndArtifactMatches(ivyNotationAsString)
		}.values().flatten() as Collection<GroovyDsl>
		return triggerForDsls(matchingContracts, labelName)
	}

	@Override
	boolean trigger(String labelName) {
		return triggerForDsls(getAccurestContracts().values().flatten() as Collection<GroovyDsl>, labelName)
	}

	private boolean triggerForDsls(Collection<GroovyDsl> dsls, String labelName) {
		Collection<GroovyDsl> matchingDsls = dsls.findAll { it.label == labelName }
		if (matchingDsls.empty) {
			return false
		}
		matchingDsls.each {
			sendMessageIfApplicable(it)
		}
		return true
	}

	@Override
	boolean trigger() {
		(getAccurestContracts().values().flatten() as Collection<GroovyDsl>).each { GroovyDsl groovyDsl ->
			sendMessageIfApplicable(groovyDsl)
		}
		return true
	}

	@Override
	Map<String, Collection<String>> labels() {
		return getAccurestContracts().collectEntries {
			[(it.key.toColonSeparatedDependencyNotation()) : it.value.collect { it.label }]
		} as Map<String, List<String>>
	}

	private void sendMessageIfApplicable(GroovyDsl groovyDsl) {
		if (!groovyDsl.outputMessage) {
			return
		}
		AccurestMessage message = accurestMessaging.create(groovyDsl.outputMessage?.body?.clientValue,
				groovyDsl.outputMessage?.headers?.asStubSideMap())
		accurestMessaging.send(message, groovyDsl.outputMessage.sentTo.clientValue)
	}

	private URL returnStubUrlIfMatches(boolean condition) {
		return condition ? stubServer.stubUrl : null
	}

	private void startStubServers(StubRunnerOptions stubRunnerOptions, StubConfiguration stubConfiguration, StubRepository repository) {
		List<WiremockMappingDescriptor> mappings = repository.getProjectDescriptors()
		Collection<GroovyDsl> contracts = repository.accurestContracts
		Integer port = stubRunnerOptions.port(stubConfiguration)
		if (port) {
			stubServer = new StubServer(port, stubConfiguration, mappings, contracts).start()
		} else {
			stubServer =  portScanner.tryToExecuteWithFreePort { int availablePort ->
				return new StubServer(availablePort, stubConfiguration, mappings, contracts).start()
			}
		}
	}

}
