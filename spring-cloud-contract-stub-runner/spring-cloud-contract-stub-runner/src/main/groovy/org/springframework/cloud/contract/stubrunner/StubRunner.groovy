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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.verifier.dsl.Contract
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierMessaging

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
	private final StubRunnerExecutor localStubRunner
	private final ContractVerifierMessaging contractVerifierMessaging

	@Deprecated
	StubRunner(Arguments arguments) {
		this(arguments.stubRunnerOptions, arguments.repositoryPath, arguments.stub)
	}

	StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath, StubConfiguration stubsConfiguration) {
		this(stubRunnerOptions, repositoryPath, stubsConfiguration, new NoOpContractVerifierMessaging())
	}

	StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath, StubConfiguration stubsConfiguration,
			   ContractVerifierMessaging contractVerifierMessaging) {
		this.stubsConfiguration = stubsConfiguration
		this.stubRunnerOptions = stubRunnerOptions
		this.stubRepository = new StubRepository(new File(repositoryPath))
		AvailablePortScanner portScanner = new AvailablePortScanner(stubRunnerOptions.minPortValue,
				stubRunnerOptions.maxPortValue)
		this.contractVerifierMessaging = contractVerifierMessaging
		this.localStubRunner = new StubRunnerExecutor(portScanner, contractVerifierMessaging)
	}

	@Override
	RunningStubs runStubs() {
		registerShutdownHook()
		return localStubRunner.runStubs(stubRunnerOptions,stubRepository, stubsConfiguration)
	}

	@Override
	URL findStubUrl(String groupId, String artifactId) {
		return localStubRunner.findStubUrl(groupId, artifactId)
	}

	@Override
	URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length == 1) {
			// assuming that ivy notation represents artifactId only
			return findStubUrl(null, splitString[0])
		}
		return findStubUrl(splitString[0], splitString[1])
	}

	@Override
	RunningStubs findAllRunningStubs() {
		return localStubRunner.findAllRunningStubs()
	}

	@Override
	Map<StubConfiguration, Collection<Contract>> getContracts() {
		return localStubRunner.getContracts()
	}

	@Override
	boolean trigger(String ivyNotation, String labelName) {
		return localStubRunner.trigger(ivyNotation, labelName)
	}

	@Override
	boolean trigger(String labelName) {
		return localStubRunner.trigger(labelName)
	}

	@Override
	boolean trigger() {
		return localStubRunner.trigger()
	}

	@Override
	Map<String, Collection<String>> labels() {
		return localStubRunner.labels()
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