package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic

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

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, Collection<StubConfiguration> dependencies) {
		this(stubRunnerOptions, dependencies, new GrapeStubDownloader())
	}

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions,
	                       Collection<StubConfiguration> dependencies,
	                       StubDownloader stubDownloader) {
		this.stubRunnerOptions = stubRunnerOptions
		this.dependencies = dependencies
		this.stubDownloader = stubDownloader
	}

	BatchStubRunner buildBatchStubRunner() {
		StubRunnerFactory stubRunnerFactory = new StubRunnerFactory(stubRunnerOptions, dependencies, stubDownloader)
		return new BatchStubRunner(stubRunnerFactory.createStubsFromServiceConfiguration())
	}

}
