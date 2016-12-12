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

import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;

/**
 * Manages lifecycle of multiple {@link StubRunner} instances.
 *
 * @see StubRunner
 * @see BatchStubRunner
 */
public class BatchStubRunnerFactory {

	private final StubRunnerOptions stubRunnerOptions;
	private final StubDownloader stubDownloader;
	private final MessageVerifier<?> contractVerifierMessaging;

	public BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions) {
		this(stubRunnerOptions, aetherStubDownloader(stubRunnerOptions), new NoOpStubMessages());
	}

	private static StubDownloader aetherStubDownloader(StubRunnerOptions stubRunnerOptions) {
		StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider();
		return provider.hasBuilder() ? provider.get().build(stubRunnerOptions) : new AetherStubDownloader(stubRunnerOptions);
	}

	public BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader) {
		this(stubRunnerOptions, stubDownloader, new NoOpStubMessages());
	}

	public BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader, MessageVerifier<?> contractVerifierMessaging) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.stubDownloader = stubDownloader;
		this.contractVerifierMessaging = contractVerifierMessaging;
	}

	public BatchStubRunner buildBatchStubRunner() {
		StubRunnerFactory stubRunnerFactory = new StubRunnerFactory(this.stubRunnerOptions, this.stubDownloader, this.contractVerifierMessaging);
		return new BatchStubRunner(stubRunnerFactory.createStubsFromServiceConfiguration());
	}

}
