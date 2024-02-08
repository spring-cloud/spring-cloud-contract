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

package org.springframework.cloud.contract.stubrunner.spring.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.contract.stubrunner.StubFinder;

/**
 * Custom version of {@link DiscoveryClient} that tries to find an instance in one of the
 * started WireMock servers.
 *
 * @author Marcin Grzejszczak
 * @since 2.2.4
 */
class StubRunnerReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	private static final Log log = LogFactory.getLog(StubRunnerReactiveDiscoveryClient.class);

	private final ReactiveDiscoveryClient delegate;

	private final StubFinder stubFinder;

	private final StubMapperProperties stubMapperProperties;

	StubRunnerReactiveDiscoveryClient(ReactiveDiscoveryClient delegate, StubFinder stubFinder,
			StubMapperProperties stubMapperProperties) {
		this.delegate = delegate instanceof StubRunnerDiscoveryClient ? noOpDiscoveryClient() : delegate;
		if (log.isDebugEnabled()) {
			log.debug("Will delegate calls to discovery service [" + this.delegate + "] if a stub is not found");
		}
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	StubRunnerReactiveDiscoveryClient(StubFinder stubFinder, StubMapperProperties stubMapperProperties) {
		this.delegate = noOpDiscoveryClient();
		if (log.isDebugEnabled()) {
			log.debug("Will delegate calls to discovery service [" + this.delegate + "] if a stub is not found");
		}
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	private StubRunnerNoOpReactiveDiscoveryClient noOpDiscoveryClient() {
		return new StubRunnerNoOpReactiveDiscoveryClient();
	}

	@Override
	public String description() {
		try {
			return this.delegate.description();
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to fetch description from delegate", e);
			}
		}
		return "";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		return Flux.just(client()).flatMapIterable(c -> c.getInstances(serviceId));
	}

	private StubRunnerDiscoveryClient client() {
		return new StubRunnerDiscoveryClient(this.stubFinder, this.stubMapperProperties);
	}

	@Override
	public Flux<String> getServices() {
		return Flux.just(client()).flatMapIterable(StubRunnerDiscoveryClient::getServices);
	}

	@Override
	public int getOrder() {
		return this.delegate.getOrder();
	}

}

class StubRunnerNoOpReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	@Override
	public String description() {
		return "Spring Cloud Stub Runner No-op ReactiveDiscoveryClient";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		return Flux.empty();
	}

	@Override
	public Flux<String> getServices() {
		return Flux.empty();
	}

}
