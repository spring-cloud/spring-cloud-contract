/*
 * Copyright 2013-2020 the original author or authors.
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
 * Allows to perform additional configuration of the HTTP Server stub.
 *
 * @param <T> type of the stub configuration
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public interface HttpServerStubConfigurer<T> {

	/**
	 * Ensures that the HTTP server stub implementation configuration type is accepted.
	 * @param httpStubConfiguration - HTTP server stub implementation
	 * @return {@code true} when this configurer can be applied for this object
	 */
	boolean isAccepted(Object httpStubConfiguration);

	/**
	 * Performs additional configuration of the HTTP Server Stub.
	 * @param httpStubConfiguration - stub implementation to configure
	 * @param httpServerStubConfiguration - Spring Cloud Contract stub configuration
	 * @return the modified stub configuration
	 */
	default T configure(T httpStubConfiguration,
			HttpServerStubConfiguration httpServerStubConfiguration) {
		return httpStubConfiguration;
	}

	/**
	 * Implementation that does nothing.
	 */
	class NoOpHttpServerStubConfigurer implements HttpServerStubConfigurer {

		public static HttpServerStubConfigurer INSTANCE = new NoOpHttpServerStubConfigurer();

		@Override
		public boolean isAccepted(Object httpStubConfiguration) {
			return false;
		}

	}

}
