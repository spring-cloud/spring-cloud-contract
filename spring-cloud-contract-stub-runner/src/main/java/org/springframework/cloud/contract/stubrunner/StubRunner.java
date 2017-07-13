/*
 *  Copyright 2013-2017 the original author or authors.
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;

/**
 * Represents a single instance of ready-to-run stubs. Can run the stubs and then will
 * return the name of the collaborator together with its URI. Can also be queried if the
 * current groupid and artifactid are matching the corresponding running stub.
 */
public class StubRunner implements StubRunning {

	private static final Log log = LogFactory.getLog(StubRunner.class);

	private final StubRepository stubRepository;
	private final StubConfiguration stubsConfiguration;
	private final StubRunnerOptions stubRunnerOptions;
	private final StubRunnerExecutor localStubRunner;

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
		List<HttpServerStub> serverStubs = SpringFactoriesLoader.loadFactories(HttpServerStub.class, null);
		this.stubRepository = new StubRepository(new File(repositoryPath), serverStubs, this.stubRunnerOptions);
		AvailablePortScanner portScanner = new AvailablePortScanner(
				stubRunnerOptions.getMinPortValue(), stubRunnerOptions.getMaxPortValue());
		this.localStubRunner = new StubRunnerExecutor(portScanner, contractVerifierMessaging, serverStubs);
	}

	@Override
	public RunningStubs runStubs() {
		registerShutdownHook();
		RunningStubs stubs = this.localStubRunner.runStubs(this.stubRunnerOptions, this.stubRepository,
				this.stubsConfiguration);
		if (this.stubRunnerOptions.hasMappingsOutputFolder()) {
			String registeredMappings = this.localStubRunner.registeredMappings();
			if (StringUtils.hasText(registeredMappings)) {
				File outputMappings = new File(this.stubRunnerOptions.getMappingsOutputFolder(),
						this.stubsConfiguration.artifactId + "_"
								+ stubs.getPort(this.stubsConfiguration.toColonSeparatedDependencyNotation()));
				try {
					outputMappings.getParentFile().mkdirs();
					clearOldFiles(outputMappings.getParentFile(), this.stubsConfiguration.artifactId);
					outputMappings.createNewFile();
					Files.write(Paths.get(outputMappings.toURI()), registeredMappings.getBytes());
					if (log.isDebugEnabled()) {
						log.debug("Stored the mappings for artifactid [" + this.stubsConfiguration.artifactId + "] at [" + outputMappings + "] location");
					}
				}
				catch (IOException e) {
					log.error("Exception occurred while trying to store the mappings", e);
					throw new IllegalStateException(e);
				}
			}
		}
		return stubs;
	}

	private void clearOldFiles(File outputFolder, final String filename) {
		File[] files = outputFolder.listFiles(new FilenameFilter() {
			@Override public boolean accept(final File dir, final String name) {
				return name.startsWith(filename);
			}
		});
		if (files == null) {
			if(log.isDebugEnabled()) {
				log.debug("Failed to retrieve any mappings");
			}
			return;
		}
		for (final File file : files) {
			if (!file.delete()) {
				if(log.isDebugEnabled()) {
					log.debug("Exception occurred while trying to remove [" + file.getAbsolutePath() + "]");
				}
			}
		}
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		return this.localStubRunner.findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		return this.localStubRunner.findStubUrl(ivyNotation);
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