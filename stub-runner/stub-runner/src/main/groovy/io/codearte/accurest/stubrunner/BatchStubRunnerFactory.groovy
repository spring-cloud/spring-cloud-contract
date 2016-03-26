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

	BatchStubRunnerFactory(StubRunnerOptions stubRunnerOptions, Collection<StubConfiguration> dependencies) {
		this.stubRunnerOptions = stubRunnerOptions
		this.dependencies = dependencies
	}

	BatchStubRunner buildBatchStubRunner() {
		StubRunnerFactory stubRunnerFactory = new StubRunnerFactory(stubRunnerOptions, dependencies)
		return new BatchStubRunner(stubRunnerFactory.createStubsFromServiceConfiguration())
	}

}
