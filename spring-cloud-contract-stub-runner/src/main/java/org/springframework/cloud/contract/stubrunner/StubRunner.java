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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;

/**
 * Represents a single instance of ready-to-run stubs. Can run the stubs and then will
 * return the name of the collaborator together with its URI. Can also be queried if the
 * current groupid and artifactid are matching the corresponding running stub.
 */
public class StubRunner implements StubRunning {

	private final StubRepository stubRepository;
	private final StubConfiguration stubsConfiguration;
	private final StubRunnerOptions stubRunnerOptions;
	private final StubRunnerExecutor localStubRunner;

	@Deprecated
	StubRunner(Arguments arguments) {
		this(arguments.getStubRunnerOptions(), arguments.getRepositoryPath(),
				arguments.getStub());
	}

	public StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath,
			StubConfiguration stubsConfiguration) {
		this(stubRunnerOptions, repositoryPath, stubsConfiguration,
				new NoOpStubMessages());
	}

	public StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath,
			StubConfiguration stubsConfiguration,
			MessageVerifier<?> contractVerifierMessaging) {
		this.stubsConfiguration = stubsConfiguration;
		this.stubRunnerOptions = stubRunnerOptions;
		this.stubRepository = new StubRepository(new File(repositoryPath));
		AvailablePortScanner portScanner = new AvailablePortScanner(
				stubRunnerOptions.getMinPortValue(), stubRunnerOptions.getMaxPortValue());
		this.localStubRunner = new StubRunnerExecutor(portScanner, contractVerifierMessaging);
	}

	@Override
	public RunningStubs runStubs() {
		registerShutdownHook();
		return this.localStubRunner.runStubs(this.stubRunnerOptions, this.stubRepository,
				this.stubsConfiguration);
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		return this.localStubRunner.findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":");
		if (splitString.length == 1) {
			// assuming that ivy notation represents artifactId only
			return findStubUrl(null, splitString[0]);
		}
		return findStubUrl(splitString[0], splitString[1]);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return this.localStubRunner.findAllRunningStubs();
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return this.localStubRunner.getContracts();
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		return this.localStubRunner.trigger(ivyNotation, labelName);
	}

	@Override
	public boolean trigger(String labelName) {
		return this.localStubRunner.trigger(labelName);
	}

	@Override
	public boolean trigger() {
		return this.localStubRunner.trigger();
	}

	@Override
	public Map<String, Collection<String>> labels() {
		return this.localStubRunner.labels();
	}

	private void registerShutdownHook() {
		Runnable stopAllServers = new Runnable() {
			@Override
			public void run() {
				try {
					close();
				}
				catch (IOException e) {
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(stopAllServers));
	}

	@Override
	public void close() throws IOException {
		if (this.localStubRunner != null) {
			this.localStubRunner.shutdown();
		}
	}
}