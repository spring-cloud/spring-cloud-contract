/*
 *  Copyright 2013-2016 the original author or authors.
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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierMessaging
import org.springframework.cloud.contract.verifier.util.BodyExtractor

/**
 * Runs stubs for a particular {@link StubServer}
 */
@CompileStatic
@Slf4j
class StubRunnerExecutor implements StubFinder {

	private final AvailablePortScanner portScanner
	private final ContractVerifierMessaging contractVerifierMessaging
	private StubServer stubServer

	StubRunnerExecutor(AvailablePortScanner portScanner, ContractVerifierMessaging contractVerifierMessaging) {
		this.portScanner = portScanner
		this.contractVerifierMessaging = contractVerifierMessaging
	}

	StubRunnerExecutor(AvailablePortScanner portScanner) {
		this.portScanner = portScanner
		this.contractVerifierMessaging = new NoOpContractVerifierMessaging()
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
	Map<StubConfiguration, Collection<Contract>> getContracts() {
		return [(stubServer.stubConfiguration): stubServer.contracts]
	}

	@Override
	boolean trigger(String ivyNotationAsString, String labelName) {
		Collection<Contract> matchingContracts = getContracts().findAll {
			it.key.groupIdAndArtifactMatches(ivyNotationAsString)
		}.values().flatten() as Collection<Contract>
		return triggerForDsls(matchingContracts, labelName)
	}

	@Override
	boolean trigger(String labelName) {
		return triggerForDsls(getContracts().values().flatten() as Collection<Contract>, labelName)
	}

	private boolean triggerForDsls(Collection<Contract> dsls, String labelName) {
		Collection<Contract> matchingDsls = dsls.findAll { it.label == labelName }
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
		(getContracts().values().flatten() as Collection<Contract>).each { Contract groovyDsl ->
			sendMessageIfApplicable(groovyDsl)
		}
		return true
	}

	@Override
	Map<String, Collection<String>> labels() {
		return getContracts().collectEntries {
			[(it.key.toColonSeparatedDependencyNotation()) : it.value.collect { it.label }]
		} as Map<String, Collection<String>>
	}

	private void sendMessageIfApplicable(Contract groovyDsl) {
		if (!groovyDsl.outputMessage) {
			return
		}
		ContractVerifierMessage message = contractVerifierMessaging.create(
				new JsonOutput().toJson(BodyExtractor.extractClientValueFromBody(groovyDsl.outputMessage?.body?.clientValue)),
				groovyDsl.outputMessage?.headers?.asStubSideMap())
		contractVerifierMessaging.send(message, groovyDsl.outputMessage.sentTo.clientValue)
	}

	private URL returnStubUrlIfMatches(boolean condition) {
		return condition ? stubServer.stubUrl : null
	}

	private void startStubServers(StubRunnerOptions stubRunnerOptions, StubConfiguration stubConfiguration, StubRepository repository) {
		List<WiremockMappingDescriptor> mappings = repository.getProjectDescriptors()
		Collection<Contract> contracts = repository.contracts
		Integer port = stubRunnerOptions.port(stubConfiguration)
		if (port) {
			stubServer = new StubServer(port, stubConfiguration, mappings, contracts)
		} else {
			stubServer =  portScanner.tryToExecuteWithFreePort { int availablePort ->
				return new StubServer(availablePort, stubConfiguration, mappings, contracts)
			}
		}
		if (!contracts.empty && !contracts.any { it.request }) {
			log.debug("There are no HTTP related contracts. Won't start any servers")
			return
		}
		if (contracts.empty) {
			log.warn("There are no contracts in the published JAR. This is an unusual situation " +
					"that's why will start the server - maybe you know what you're doing...")
		}
		stubServer = stubServer.start()
	}

}
