package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.Collection;

/**
 * Describes an HTTP Server Stub
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public interface HttpServerStub {
	/**
	 * Port on which the server is running
	 */
	int port();

	/**
	 * Returns {@code true} if the server is running
	 */
	boolean isRunning();

	/**
	 * Starts the server on a random port. Should return itself
	 * to allow chaining.
	 */
	HttpServerStub start();

	/**
	 * Starts the server on a given port. Should return itself
	 * to allow chaining.
	 */
	HttpServerStub start(int port);

	/**
	 * Stops the server. Should return itself to allow chaining.
	 */
	HttpServerStub stop();

	/**
	 * Registers the stub files in the HTTP server stub. Should return itself
	 * to allow chaining.
	 */
	HttpServerStub registerMappings(Collection<File> stubFiles);

	/**
	 * Returns a collection of registered mappings
	 */
	String registeredMappings();

	/**
	 * Returns {@code true} if the file is a valid stub mapping
	 */
	boolean isAccepted(File file);
}
