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
	void trigger(String ivyNotationAsString, String labelName) {
		Collection<GroovyDsl> matchingContracts = getAccurestContracts().findAll {
			it.key.matches(ivyNotationAsString)
		}.values().flatten() as Collection<GroovyDsl>
		triggerForDsls(matchingContracts, labelName)
	}

	@Override
	void trigger(String labelName) {
		triggerForDsls(getAccurestContracts().values().flatten() as Collection<GroovyDsl>, labelName)
	}

	private void triggerForDsls(Collection<GroovyDsl> dsls, String labelName) {
		dsls.findAll { it.label == labelName}.each {
			sendMessageIfApplicable(it)
		}
	}

	@Override
	void trigger() {
		(getAccurestContracts().values().flatten() as Collection<GroovyDsl>).each { GroovyDsl groovyDsl ->
			sendMessageIfApplicable(groovyDsl)
		}
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

	private void startStubServers(StubConfiguration stubConfiguration, StubRepository repository) {
		List<WiremockMappingDescriptor> mappings = repository.getProjectDescriptors()
		Collection<GroovyDsl> contracts = repository.accurestContracts
		stubServer =  portScanner.tryToExecuteWithFreePort { int availablePort ->
			return new StubServer(availablePort, stubConfiguration, mappings, contracts).start()
		}
	}

}
