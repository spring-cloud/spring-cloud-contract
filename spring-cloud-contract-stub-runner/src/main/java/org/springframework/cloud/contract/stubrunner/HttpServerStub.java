/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.Collection;

/**
 * Describes an HTTP Server Stub.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public interface HttpServerStub {

	/**
	 * @return port on which the server is running. Return {@code -1} if not applicable.
	 */
	int port();

	/**
	 * @return https port on which the server is running. Return {@code -1} if not
	 * applicable.
	 */
	default int httpsPort() {
		return -1;
	}

	/**
	 * @return {@code true} if the server is running
	 */
	boolean isRunning();

	/**
	 * Starts the server on a random port. Should return itself to allow chaining.
	 * @deprecated use {@link HttpServerStub#start(HttpServerStubConfiguration)}
	 * @return this
	 */
	@Deprecated
	HttpServerStub start();

	/**
	 * Starts the server on a given port. Should return itself to allow chaining.
	 * @deprecated use {@link HttpServerStub#start(HttpServerStubConfiguration)}
	 * @param port port on which the server should be ran
	 * @return this
	 */
	@Deprecated
	HttpServerStub start(int port);

	/**
	 * Starts the server. Should return itself to allow chaining.
	 * @param configuration - setup for the given stub
	 * @return this
	 */
	default HttpServerStub start(HttpServerStubConfiguration configuration) {
		if (configuration.isRandomPort()) {
			return start();
		}
		return start(configuration.port);
	};

	/**
	 * Stops the server. Should return itself to allow chaining.
	 * @return this
	 */
	HttpServerStub stop();

	/**
	 * Resets the server. Should return itself to allow chaining.
	 * @return this
	 */
	default HttpServerStub reset() {
		return this;
	}

	/**
	 * Registers the stub files in the HTTP server stub. Should return itself to allow
	 * chaining.
	 * @param stubFiles collection of files containing stubs
	 * @return this
	 */
	HttpServerStub registerMappings(Collection<File> stubFiles);

	/**
	 * @return a collection of registered mappings.
	 */
	String registeredMappings();

	/**
	 * @param file file to check if can be accepted.
	 * @return {@code true} if the file is a valid stub mapping
	 */
	boolean isAccepted(File file);

}
