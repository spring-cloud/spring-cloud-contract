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
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * Factory of StubRunners. Basing on the options and passed collaborators downloads the
 * stubs and returns a list of corresponding stub runners.
 */
class StubRunnerFactory {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubRunnerOptions stubRunnerOptions;
	private final StubDownloader stubDownloader;
	private final MessageVerifier<?> contractVerifierMessaging;

	public StubRunnerFactory(StubRunnerOptions stubRunnerOptions,
			StubDownloader stubDownloader,
			MessageVerifier<?> contractVerifierMessaging) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.stubDownloader = stubDownloader;
		this.contractVerifierMessaging = contractVerifierMessaging;
	}

	public Collection<StubRunner> createStubsFromServiceConfiguration() {
		if (log.isDebugEnabled()) {
			log.debug("Will download stubs for dependencies " + this.stubRunnerOptions.getDependencies());
		}
		if (this.stubRunnerOptions.getDependencies().isEmpty()) {
			log.warn("No stubs to download have been passed. Most likely you have forgotten to pass "
					+ "them either via annotation or a property");
		}
		Collection<StubRunner> result = new ArrayList<>();
		for (StubConfiguration stubsConfiguration : this.stubRunnerOptions.getDependencies()) {
			Map.Entry<StubConfiguration, File> entry = this.stubDownloader
					.downloadAndUnpackStubJar(stubsConfiguration);
			if (log.isDebugEnabled()) {
				log.debug("For stub configuration [" + stubsConfiguration + "] the downloaded entry is [" + entry + "]");
			}
			if (entry != null) {
				result.add(createStubRunner(entry.getKey(), entry.getValue()));
			}

		}
		return result;
	}

	private StubRunner createStubRunner(StubConfiguration stubsConfiguration,
			File unzipedStubDir) {
		if (unzipedStubDir == null) {
			return null;
		}
		return createStubRunner(unzipedStubDir, stubsConfiguration, this.stubRunnerOptions);
	}

	private StubRunner createStubRunner(File unzippedStubsDir,
			StubConfiguration stubsConfiguration, StubRunnerOptions stubRunnerOptions) {
		return new StubRunner(stubRunnerOptions, unzippedStubsDir.getPath(),
				stubsConfiguration, this.contractVerifierMessaging);
	}

}
