package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.messaging.noop.NoOpAccurestMessaging

/**
 * Manages lifecycle of multiple {@link StubRunner} instances.
 *
 * @see StubRunner
 * @see BatchStubRunner
 */
@CompileStatic
class BatchStubRunnerFactory {

	private final StubRunnerOptions stubRunnerOptions
	private final StubDownloader stubDownloader
	private final AccurestMessaging accurestMessaging

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions) {
		this(stubRunnerOptions, new AetherStubDownloader(stubRunnerOptions), new NoOpAccurestMessaging())
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, AccurestMessaging accurestMessaging) {
		this(stubRunnerOptions, new AetherStubDownloader(stubRunnerOptions), accurestMessaging)
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader) {
		this(stubRunnerOptions, stubDownloader, new NoOpAccurestMessaging())
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader, AccurestMessaging accurestMessaging) {
		this.stubRunnerOptions = stubRunnerOptions
		this.stubDownloader = stubDownloader
		this.accurestMessaging = accurestMessaging
	}

	BatchStubRunner buildBatchStubRunner() {
		StubRunnerFactory stubRunnerFactory = new StubRunnerFactory(stubRunnerOptions, stubDownloader, accurestMessaging)
		return new BatchStubRunner(stubRunnerFactory.createStubsFromServiceConfiguration())
	}

}
