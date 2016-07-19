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

package org.springframework.cloud.contract.stubrunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.stubrunner.AvailablePortScanner.PortCallback;
import org.springframework.cloud.contract.verifier.messaging.StubMessages;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;

import groovy.json.JsonOutput;

/**
 * Runs stubs for a particular {@link StubServer}
 */
public class StubRunnerExecutor implements StubFinder {

	private static final Logger log = LoggerFactory.getLogger(StubRunnerExecutor.class);
	private final AvailablePortScanner portScanner;
	private final StubMessages<?> contractVerifierMessaging;
	private StubServer stubServer;

	public StubRunnerExecutor(AvailablePortScanner portScanner,
			StubMessages<?> contractVerifierMessaging) {
		this.portScanner = portScanner;
		this.contractVerifierMessaging = contractVerifierMessaging;
	}

	public StubRunnerExecutor(AvailablePortScanner portScanner) {
		this(portScanner, new NoOpStubMessages());
	}

	public RunningStubs runStubs(StubRunnerOptions stubRunnerOptions,
			StubRepository repository, StubConfiguration stubConfiguration) {
		startStubServers(stubRunnerOptions, stubConfiguration, repository);
		RunningStubs runningCollaborators = new RunningStubs(Collections
				.singletonMap(stubServer.getStubConfiguration(), stubServer.getPort()));
		log.info("All stubs are now running " + runningCollaborators.toString());
		return runningCollaborators;
	}

	public void shutdown() {
		if (stubServer != null) {
			stubServer.stop();
		}
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		if (groupId == null) {
			return returnStubUrlIfMatches(
					artifactId.equals(stubServer.stubConfiguration.artifactId));
		}
		return returnStubUrlIfMatches(
				artifactId.equals(stubServer.stubConfiguration.artifactId)
						&& groupId.equals(stubServer.stubConfiguration.groupId));
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":");
		if (splitString.length == 1) {
			throw new IllegalArgumentException("$ivyNotation is invalid");
		}
		return findStubUrl(splitString[0], splitString[1]);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return new RunningStubs(Collections.singletonMap(stubServer.stubConfiguration,
				stubServer.getPort()));
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return Collections.singletonMap(stubServer.stubConfiguration,
				stubServer.getContracts());
	}

	@Override
	public boolean trigger(String ivyNotationAsString, String labelName) {
		Collection<Contract> matchingContracts = new ArrayList<>();
		for (Entry<StubConfiguration, Collection<Contract>> it : getContracts()
				.entrySet()) {
			if (it.getKey().groupIdAndArtifactMatches(ivyNotationAsString)) {
				matchingContracts.addAll(it.getValue());
			}
		}
		;
		return triggerForDsls(matchingContracts, labelName);
	}

	@Override
	public boolean trigger(String labelName) {
		Collection<Contract> matchingContracts = new ArrayList<>();
		for (Collection<Contract> it : getContracts().values()) {
			matchingContracts.addAll(it);
		}
		return triggerForDsls(matchingContracts, labelName);
	}

	private boolean triggerForDsls(Collection<Contract> dsls, String labelName) {
		Collection<Contract> matchingDsls = new ArrayList<>();
		for (Contract contract : dsls) {
			if (labelName.equals(contract.getLabel())) {
				matchingDsls.add(contract);
			}
		}
		if (matchingDsls.isEmpty()) {
			return false;
		}
		for (Contract contract : matchingDsls) {
			sendMessageIfApplicable(contract);
		}
		return true;
	}

	@Override
	public boolean trigger() {
		Collection<Contract> matchingContracts = new ArrayList<>();
		for (Collection<Contract> it : getContracts().values()) {
			matchingContracts.addAll(it);
		}
		for (Contract contract : matchingContracts) {
			sendMessageIfApplicable(contract);
		}
		return true;
	}

	@Override
	public Map<String, Collection<String>> labels() {
		Map<String, Collection<String>> labels = new LinkedHashMap<>();
		for (Entry<StubConfiguration, Collection<Contract>> it : getContracts()
				.entrySet()) {
			Collection<String> values = new ArrayList<>();
			for (Contract contract : it.getValue()) {
				values.add(contract.getLabel());
			}
			labels.put(it.getKey().toColonSeparatedDependencyNotation(), values);
		}
		return labels;
	}

	private void sendMessageIfApplicable(Contract groovyDsl) {
		OutputMessage outputMessage = groovyDsl.getOutputMessage();
		if (outputMessage == null) {
			return;
		}
		DslProperty<?> body = outputMessage == null ? null : outputMessage.getBody();
		Headers headers = outputMessage == null ? null : outputMessage.getHeaders();
		contractVerifierMessaging.send(
				JsonOutput.toJson(BodyExtractor.extractClientValueFromBody(
						body == null ? null : body.getClientValue())),
				headers == null ? null : headers.asStubSideMap(),
				outputMessage.getSentTo().getClientValue());
	}

	private URL returnStubUrlIfMatches(boolean condition) {
		return condition ? stubServer.getStubUrl() : null;
	}

	private void startStubServers(StubRunnerOptions stubRunnerOptions,
			final StubConfiguration stubConfiguration, StubRepository repository) {
		final List<WiremockMappingDescriptor> mappings = repository
				.getProjectDescriptors();
		final Collection<Contract> contracts = repository.contracts;
		Integer port = stubRunnerOptions.port(stubConfiguration);
		if (port != null && port >= 0) {
			stubServer = new StubServer(port, stubConfiguration, mappings, contracts);
		}
		else {
			stubServer = portScanner
					.tryToExecuteWithFreePort(new PortCallback<StubServer>() {

						@Override
						public StubServer call(int availablePort) {
							return new StubServer(availablePort, stubConfiguration,
									mappings, contracts);
						}

					});
		}
		if (!contracts.isEmpty() && !hasRequest(contracts)) {
			log.debug("There are no HTTP related contracts. Won't start any servers");
			return;
		}
		if (contracts.isEmpty()) {
			log.warn(
					"There are no contracts in the published JAR. This is an unusual situation "
							+ "that's why will start the server - maybe you know what you're doing...");
		}
		stubServer = stubServer.start();
	}

	private boolean hasRequest(Collection<Contract> contracts) {
		for (Contract contract : contracts) {
			if (contract.getRequest() != null) {
				return true;
			}
		}
		return false;
	}

}
