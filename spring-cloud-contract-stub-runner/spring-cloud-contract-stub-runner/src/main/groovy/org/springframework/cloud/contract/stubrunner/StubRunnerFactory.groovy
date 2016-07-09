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
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging

/**
 * Factory of StubRunners. Basing on the options and passed collaborators
 * downloads the stubs and returns a list of corresponding stub runners.
 */
@Slf4j
@CompileStatic
class StubRunnerFactory {

	private final StubRunnerOptions stubRunnerOptions
	private final StubDownloader stubDownloader
	private final ContractVerifierMessaging contractVerifierMessaging

	StubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader, ContractVerifierMessaging contractVerifierMessaging) {
		this.stubRunnerOptions = stubRunnerOptions
		this.stubDownloader = stubDownloader
		this.contractVerifierMessaging = contractVerifierMessaging
	}

	Collection<StubRunner> createStubsFromServiceConfiguration() {
		return stubRunnerOptions.getDependencies().collect { StubConfiguration stubsConfiguration ->
			Map.Entry<StubConfiguration, File> entry = stubDownloader.downloadAndUnpackStubJar(stubRunnerOptions, stubsConfiguration)
			if (!entry) {
				return null
			}
			return createStubRunner(entry.key, entry.value)
		}.findAll { it != null }
	}

	private StubRunner createStubRunner(StubConfiguration stubsConfiguration, File unzipedStubDir) {
		if (!unzipedStubDir) {
			return null
		}
		return createStubRunner(unzipedStubDir, stubsConfiguration, stubRunnerOptions)
	}

	private StubRunner createStubRunner(File unzippedStubsDir, StubConfiguration stubsConfiguration,
	                                    StubRunnerOptions stubRunnerOptions) {
		return new StubRunner(stubRunnerOptions, unzippedStubsDir.path, stubsConfiguration, contractVerifierMessaging)
	}

}
