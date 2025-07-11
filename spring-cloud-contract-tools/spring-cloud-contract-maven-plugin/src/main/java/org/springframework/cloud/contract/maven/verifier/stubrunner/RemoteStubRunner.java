/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.maven.verifier.stubrunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.aether.RepositorySystemSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.stereotype.Component;

/**
 * Fetches stubs from a remote location.
 *
 * @author Mariusz Smykula
 */
@Component
public class RemoteStubRunner {

	private static final Log log = LogFactory.getLog(RemoteStubRunner.class);

	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;

	@Autowired
	public RemoteStubRunner(AetherStubDownloaderFactory aetherStubDownloaderFactory) {
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
	}

	public BatchStubRunner run(StubRunnerOptions options, RepositorySystemSession repositorySystemSession) {
		StubDownloader stubDownloader = this.aetherStubDownloaderFactory.build(repositorySystemSession).build(options);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Launching StubRunner with args: " + options);
			}
			BatchStubRunner stubRunner = new BatchStubRunnerFactory(options, stubDownloader).buildBatchStubRunner();
			RunningStubs runningCollaborators = stubRunner.runStubs();
			log.info(runningCollaborators.toString());
			return stubRunner;
		}
		catch (Exception e) {
			log.error("An exception occurred while trying to execute the stubs: " + e.getMessage());
			throw e;
		}

	}

}
