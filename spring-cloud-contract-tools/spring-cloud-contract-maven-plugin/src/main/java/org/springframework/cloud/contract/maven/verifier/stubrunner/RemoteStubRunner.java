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
package org.springframework.cloud.contract.maven.verifier.stubrunner;

import java.lang.invoke.MethodHandles;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositorySystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;

@Named
public class RemoteStubRunner {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;

	@Inject
	public RemoteStubRunner(AetherStubDownloaderFactory aetherStubDownloaderFactory) {
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
	}

	public BatchStubRunner run(StubRunnerOptions options, RepositorySystemSession repositorySystemSession) {
		AetherStubDownloader stubDownloader = aetherStubDownloaderFactory.build(repositorySystemSession);
		try {
			log.debug("Launching StubRunner with args: " + String.valueOf(options));
			BatchStubRunner stubRunner = new BatchStubRunnerFactory(options,
					stubDownloader).buildBatchStubRunner();
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
