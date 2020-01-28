/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring.cloud.loadbalancer;

import java.io.Closeable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryEnabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

/**
 * Provides autoconfiguraion for the Spring Cloud Load Balancer module.
 *
 * @since 3.0.0
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(LoadBalancerClient.class)
@ConditionalOnProperty(value = "stubrunner.cloud.loadbalancer.enabled",
		matchIfMissing = true)
@ConditionalOnBean(StubMapperProperties.class)
@AutoConfigureBefore(LoadBalancerAutoConfiguration.class)
@AutoConfigureAfter(LoadBalancerClientConfiguration.class)
@ConditionalOnStubbedDiscoveryEnabled
public class SpringCloudLoadBalancerAutoConfiguration {

	@Bean
	@Primary
	LoadBalancerClientFactory stubRunnerLoadBalancerClientFactory(StubFinder stubFinder,
			StubMapperProperties stubMapperProperties) {
		return new StubRunnerLoadBalancerClientFactory(stubFinder, stubMapperProperties);
	}

}

class StubRunnerLoadBalancerClientFactory extends LoadBalancerClientFactory
		implements Closeable {

	private final StubFinder stubFinder;

	private final StubMapperProperties stubMapperProperties;

	StubRunnerLoadBalancerClientFactory(StubFinder stubFinder,
			StubMapperProperties stubMapperProperties) {
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	@Override
	public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
		return request -> Mono.just(new DefaultResponse(
				new StubbedServiceInstance(stubFinder, stubMapperProperties, serviceId)));
	}

	@Override
	public void close() {
		StubbedServiceInstance.CACHE.clear();
	}

}

class StubbedServiceInstance implements ServiceInstance {

	private final StubFinder stubFinder;

	private final StubMapperProperties stubMapperProperties;

	private final String serviceId;

	static final Map<String, Map.Entry<StubConfiguration, Integer>> CACHE = new ConcurrentHashMap<>();

	StubbedServiceInstance(StubFinder stubFinder,
			StubMapperProperties stubMapperProperties, String serviceId) {
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
		this.serviceId = serviceId;
	}

	private Map.Entry<StubConfiguration, Integer> stubEntry() {
		Map.Entry<StubConfiguration, Integer> entry = CACHE.get(this.serviceId);
		if (entry != null) {
			return entry;
		}
		RunningStubs runningStubs = this.stubFinder.findAllRunningStubs();
		String mappedServiceName = StringUtils.hasText(
				this.stubMapperProperties.fromServiceIdToIvyNotation(this.serviceId))
						? this.stubMapperProperties.fromServiceIdToIvyNotation(
								this.serviceId)
						: this.serviceId;
		entry = runningStubs.getEntry(mappedServiceName);
		CACHE.put(this.serviceId, entry);
		return entry;
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}

	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public int getPort() {
		Map.Entry<StubConfiguration, Integer> entry = stubEntry();
		if (entry == null) {
			return 0;
		}
		return entry.getValue();
	}

	@Override
	public boolean isSecure() {
		// TODO: What if the user marks a stub as secure?
		return false;
	}

	@Override
	public URI getUri() {
		return URI.create(
				(isSecure() ? "https://" : "http://") + getHost() + ":" + getPort());
	}

	@Override
	public Map<String, String> getMetadata() {
		return null;
	}

}
