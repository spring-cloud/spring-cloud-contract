/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring.cloud;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/**
 * {@link ServiceInstance} with a helpful constructor
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class StubRunnerServiceInstance implements ServiceInstance {

	private final String serviceId;
	private final String host;
	private final int port;
	private final URI uri;

	public StubRunnerServiceInstance(String serviceId, String host, int port, URI uri) {
		this.serviceId = serviceId;
		this.host = host;
		this.port = port;
		this.uri = uri;
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public Map<String, String> getMetadata() {
		return new HashMap<>();
	}
}
