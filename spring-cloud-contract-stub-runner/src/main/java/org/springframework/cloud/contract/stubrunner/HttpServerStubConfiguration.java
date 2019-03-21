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

/**
 * Configuration class for an {@link HttpServerStub}.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public final class HttpServerStubConfiguration {

	/**
	 * Allows to perform additional configuration of the HTTP Server stub.
	 */
	public final HttpServerStubConfigurer configurer;

	/**
	 * Options of Stub Runner itself.
	 */
	public final StubRunnerOptions stubRunnerOptions;

	/**
	 * Configuration of a particular stub.
	 */
	public final StubConfiguration stubConfiguration;

	/**
	 * Port at which the stub will be started.
	 */
	public final Integer port;

	public HttpServerStubConfiguration(HttpServerStubConfigurer configurer,
			StubRunnerOptions stubRunnerOptions, StubConfiguration stubConfiguration,
			Integer port) {
		this.configurer = configurer;
		this.stubRunnerOptions = stubRunnerOptions;
		this.stubConfiguration = stubConfiguration;
		this.port = port;
	}

	public boolean isRandomPort() {
		return this.port == null || this.port == 0;
	}

	public String toColonSeparatedDependencyNotation() {
		return this.stubConfiguration != null
				? this.stubConfiguration.toColonSeparatedDependencyNotation() : "";
	}

}
