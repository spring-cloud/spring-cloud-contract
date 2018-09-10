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

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.util.StringUtils;

/**
 * Custom version of {@link DiscoveryClient} that tries to find an instance
 * in one of the started WireMock servers
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class StubRunnerDiscoveryClient implements DiscoveryClient {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final DiscoveryClient delegate;
	private final StubFinder stubFinder;
	private final StubMapperProperties stubMapperProperties;

	public StubRunnerDiscoveryClient(DiscoveryClient delegate, StubFinder stubFinder,
			StubMapperProperties stubMapperProperties, String springAppName) {
		this.delegate = delegate instanceof StubRunnerDiscoveryClient ?
				noOpDiscoveryClient() : delegate;
		if (log.isDebugEnabled()) {
			log.debug("Will delegate calls to discovery service [" + this.delegate + "] if a stub is not found");
		}
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	public StubRunnerDiscoveryClient(StubFinder stubFinder,
			StubMapperProperties stubMapperProperties, String springAppName) {
		this.delegate = noOpDiscoveryClient();
		if (log.isDebugEnabled()) {
			log.debug("Will delegate calls to discovery service [" + this.delegate + "] if a stub is not found");
		}
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	private StubRunnerNoOpDiscoveryClient noOpDiscoveryClient() {
		return new StubRunnerNoOpDiscoveryClient();
	}

	@Override
	public String description() {
		try {
			return this.delegate.description();
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to fetch description from delegate", e);
			}
		}
		return "";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		String ivyNotation = this.stubMapperProperties.fromServiceIdToIvyNotation(serviceId);
		String serviceToFind = StringUtils.hasText(ivyNotation) ? ivyNotation : serviceId;
		URL stubUrl = this.stubFinder.findStubUrl(serviceToFind);
		log.info("Resolved from ivy [" + ivyNotation + "] service to find [" + serviceToFind + "]. "
				+ "Found stub is available under URL [" + stubUrl + "]");
		if (stubUrl == null) {
			return getInstancesFromDelegate(serviceId);
		}
		return Collections.<ServiceInstance>singletonList(
				new StubRunnerServiceInstance(serviceId, stubUrl.getHost(), stubUrl.getPort(), toUri(stubUrl))
		);
	}

	private List<ServiceInstance> getInstancesFromDelegate(String serviceId) {
		try {
			return new ArrayList<>(this.delegate.getInstances(serviceId));
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to fetch instances from delegate", e);
			}
			return new ArrayList<>();
		}
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
		List<String> list = new ArrayList<>();
		List<String> services = getServicesFromDelegate();
		RunningStubs runningStubs = this.stubFinder.findAllRunningStubs();
		list.addAll(services);
		list.addAll(runningStubs.getAllServicesNames());
		return list;
	}

	private List<String> getServicesFromDelegate() {
		try {
			return new ArrayList<>(this.delegate.getServices());
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to fetch services from delegate", e);
			}
		}
		return new ArrayList<>();
	}

	@Override
	public int getOrder() {
		return this.delegate.getOrder();
	}
}

class StubRunnerNoOpDiscoveryClient implements DiscoveryClient {

	@Override
	public String description() {
		return "Spring Cloud Stub Runner No-op DiscoveryClient";
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		return Collections.emptyList();
	}

	@Override
	public List<String> getServices() {
		return Collections.emptyList();
	}
}
