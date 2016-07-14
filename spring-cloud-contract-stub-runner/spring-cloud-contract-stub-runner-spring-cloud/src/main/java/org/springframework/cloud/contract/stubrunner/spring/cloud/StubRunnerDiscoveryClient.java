/*
 *  Copyright 2013-2016 the original author or authors.
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
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClient;

import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;

/**
 * Custom version of {@link DiscoveryClient} that tries to find an instance
 * in one of the started WireMock servers
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class StubRunnerDiscoveryClient implements DiscoveryClient {

	private final DiscoveryClient delegate;
	private final StubFinder stubFinder;
	private final StubMapperProperties stubMapperProperties;

	public StubRunnerDiscoveryClient(DiscoveryClient delegate, StubFinder stubFinder,
			StubMapperProperties stubMapperProperties, String springAppName) {
		this.delegate = delegate instanceof  StubRunnerDiscoveryClient ?
				noOpDiscoveryClient(springAppName) : delegate;
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	private NoopDiscoveryClient noOpDiscoveryClient(String springAppName) {
		return new NoopDiscoveryClient(new DefaultServiceInstance(springAppName, "localhost", 0, false));
	}

	@Override
	public String description() {
		return delegate.description();
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		return delegate.getLocalServiceInstance();
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		String ivyNotation = stubMapperProperties.fromServiceIdToIvyNotation(serviceId);
		String serviceToFind = StringUtils.hasText(ivyNotation) ? ivyNotation : serviceId;
		URL stubUrl = stubFinder.findStubUrl(serviceToFind);
		if (stubUrl == null) {
			return delegate.getInstances(serviceId);
		}
		return Collections.<ServiceInstance>singletonList(
				new StubRunnerServiceInstance(serviceId, stubUrl.getHost(), stubUrl.getPort(), toUri(stubUrl))
		);
	}

	private URI toUri(URL url) {
		try {
			return url.toURI();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<String> getServices() {
		List<String> services = delegate.getServices();
		RunningStubs runningStubs = stubFinder.findAllRunningStubs();
		services.addAll(runningStubs.getAllServicesNames());
		return services;
	}
}
