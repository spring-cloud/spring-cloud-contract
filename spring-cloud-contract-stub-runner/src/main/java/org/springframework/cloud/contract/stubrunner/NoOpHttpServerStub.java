package org.springframework.cloud.contract.stubrunner;

/**
 * @author Marcin Grzejszczak
 */
class NoOpHttpServerStub implements HttpServerStub {
	@Override
	public int port() {
		return -1;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}
}
