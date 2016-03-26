package io.codearte.accurest.stubrunner

interface StubRunning extends Closeable, StubFinder {
	/**
	 * Runs the stubs and returns the {@link RunningStubs}
	 */
	RunningStubs runStubs()

}