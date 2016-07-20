package org.springframework.cloud.contract.stubrunner;

/**
 * Describes an HTTP Server Stub
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
interface HttpServerStub {
	int port();
	boolean isRunning();
	void start();
	void stop();
}
