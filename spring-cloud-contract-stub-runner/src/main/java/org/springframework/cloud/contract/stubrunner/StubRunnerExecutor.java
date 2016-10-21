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
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;

import groovy.json.JsonOutput;

/**
 * Runs stubs for a particular {@link StubServer}
 */
class StubRunnerExecutor implements StubFinder {

	private static final Logger log = LoggerFactory.getLogger(StubRunnerExecutor.class);
	private final AvailablePortScanner portScanner;
	private final MessageVerifier<?> contractVerifierMessaging;
	private StubServer stubServer;

	public StubRunnerExecutor(AvailablePortScanner portScanner,
			MessageVerifier<?> contractVerifierMessaging) {
		this.portScanner = portScanner;
		this.contractVerifierMessaging = contractVerifierMessaging;
	}

	protected StubRunnerExecutor(AvailablePortScanner portScanner) {
		this(portScanner, new NoOpStubMessages());
	}

	public RunningStubs runStubs(StubRunnerOptions stubRunnerOptions,
			StubRepository repository, StubConfiguration stubConfiguration) {
		if (this.stubServer != null) {
			if (log.isDebugEnabled()) {
				log.debug("Returning cached version of stubs [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]");
			}
			return runningStubs();
		}
		startStubServers(stubRunnerOptions, stubConfiguration, repository);
		RunningStubs runningCollaborators = runningStubs();
		log.info("All stubs are now running " + runningCollaborators.toString());
		return runningCollaborators;
	}

	private RunningStubs runningStubs() {
		return new RunningStubs(Collections
					.singletonMap(this.stubServer.getStubConfiguration(), this.stubServer.getPort()));
	}

	public void shutdown() {
		if (this.stubServer != null) {
			this.stubServer.stop();
		}
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		URL url = null;
		if (groupId == null) {
			url = findStubUrl(
					this.stubServer.stubConfiguration.artifactId.equals(artifactId));
		}
		if (url == null) {
			url = findStubUrl(this.stubServer.stubConfiguration.artifactId.equals(artifactId)
					&& this.stubServer.stubConfiguration.groupId.equals(groupId));
		}
		if (url == null) {
			throw new StubNotFoundException(groupId, artifactId);
		}
		return url;
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":");
		if (splitString.length > 4) {
			throw new IllegalArgumentException("[" + ivyNotation + "] is an invalid notation. Pass [groupId]:artifactId[:version][:classifier].");
		} else if (splitString.length == 1) {
			return findStubUrl(null, splitString[0]);
		} else if (splitString.length == 2) {
			return findStubUrl(splitString[0], splitString[1]);
		} else if (splitString.length == 3) {
			return findStubUrl(groupIdArtifactVersionMatches(splitString));
		}
		return findStubUrl(groupIdArtifactVersionMatches(splitString) && classifierMatches(splitString));
	}

	private boolean classifierMatches(String[] splitString) {
		return this.stubServer.stubConfiguration.classifier.equals(splitString[3]);
	}

	private boolean groupIdArtifactVersionMatches(String[] splitString) {
		return this.stubServer.stubConfiguration.artifactId.equals(splitString[0])
				&& this.stubServer.stubConfiguration.groupId.equals(splitString[1])
				&& this.stubServer.stubConfiguration.version.equals(splitString[2]);
	}

	private URL findStubUrl(boolean condition) {
		return returnStubUrlIfMatches(condition);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return new RunningStubs(Collections.singletonMap(this.stubServer.stubConfiguration,
				this.stubServer.getPort()));
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return Collections.singletonMap(this.stubServer.stubConfiguration,
				this.stubServer.getContracts());
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
				if (contract.getLabel() != null) {
					values.add(contract.getLabel());
				}
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
		DslProperty<?> body = outputMessage.getBody();
		Headers headers = outputMessage.getHeaders();
		this.contractVerifierMessaging.send(
				JsonOutput.toJson(BodyExtractor.extractClientValueFromBody(
						body == null ? null : body.getClientValue())),
				headers == null ? null : headers.asStubSideMap(),
				outputMessage.getSentTo().getClientValue());
	}

	private URL returnStubUrlIfMatches(boolean condition) {
		return condition ? this.stubServer.getStubUrl() : null;
	}

	private void startStubServers(final StubRunnerOptions stubRunnerOptions,
			final StubConfiguration stubConfiguration, StubRepository repository) {
		final List<WiremockMappingDescriptor> mappings = repository
				.getProjectDescriptors();
		final Collection<Contract> contracts = repository.contracts;
		Integer port = stubRunnerOptions.port(stubConfiguration);
		if (!contracts.isEmpty() && !hasRequest(contracts)) {
			if (log.isDebugEnabled()) {
				log.debug("There are no HTTP related contracts. Won't start any servers");
			}
			this.stubServer = new StubServer(stubRunnerOptions, stubConfiguration, mappings, contracts, new NoOpHttpServerStub());
			return;
		}
		if (contracts.isEmpty()) {
			log.warn(
					"There are no contracts in the published JAR. This is an unusual situation "
							+ "that's why will start the server - maybe you know what you're doing...");
		}
		if (port != null && port >= 0) {
			this.stubServer = new StubServer(stubRunnerOptions, stubConfiguration, mappings, contracts,
					new WireMockHttpServerStub(port));
		} else {
			this.stubServer = this.portScanner
					.tryToExecuteWithFreePort(new PortCallback<StubServer>() {
						@Override
						public StubServer call(int availablePort) {
							return new StubServer(stubRunnerOptions, stubConfiguration,
									mappings, contracts,
									new WireMockHttpServerStub(availablePort));
						}
					});
		}
		this.stubServer = this.stubServer.start();
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
