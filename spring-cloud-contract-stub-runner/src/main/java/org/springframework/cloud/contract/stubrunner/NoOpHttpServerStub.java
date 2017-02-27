package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.Collection;

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
	public HttpServerStub start() {
		return this;
	}

	@Override
	public HttpServerStub start(int port) {
		return this;
	}

	@Override
	public HttpServerStub stop() {
		return this;
	}

	@Override
	public HttpServerStub registerMappings(Collection<File> stubFiles) {
		return this;
	}

	@Override public boolean isAccepted(File file) {
		return true;
	}
}
