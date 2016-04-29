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
	private final Collection<StubConfiguration> dependencies
	private final StubDownloader stubDownloader
	private final AccurestMessaging accurestMessaging

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, Collection<StubConfiguration> dependencies) {
		this(stubRunnerOptions, dependencies, new AetherStubDownloader(stubRunnerOptions), new NoOpAccurestMessaging())
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, Collection<StubConfiguration> dependencies,
						   AccurestMessaging accurestMessaging) {
		this(stubRunnerOptions, dependencies, new AetherStubDownloader(stubRunnerOptions), accurestMessaging)
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions,
	                       Collection<StubConfiguration> dependencies,
						   StubDownloader stubDownloader) {
		this(stubRunnerOptions, dependencies, stubDownloader, new NoOpAccurestMessaging())
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions,
						   Collection<StubConfiguration> dependencies,
						   StubDownloader stubDownloader,
						   AccurestMessaging accurestMessaging) {
		this.stubRunnerOptions = stubRunnerOptions
		this.dependencies = dependencies
		this.stubDownloader = stubDownloader
		this.accurestMessaging = accurestMessaging
	}

	BatchStubRunner buildBatchStubRunner() {
		StubRunnerFactory stubRunnerFactory = new StubRunnerFactory(stubRunnerOptions, dependencies, stubDownloader, accurestMessaging)
		return new BatchStubRunner(stubRunnerFactory.createStubsFromServiceConfiguration())
	}

}
