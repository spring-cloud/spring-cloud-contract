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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryEnabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubRunnerSpringCloudAutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

/**
 * Provides autoconfiguraion for the Spring Cloud Load Balancer module.
 *
 * @since 2.2.3
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ LoadBalancerClient.class, LoadBalancerClientFactory.class })
@ConditionalOnProperty(value = "stubrunner.cloud.loadbalancer.enabled", matchIfMissing = true)
@ConditionalOnBean(StubMapperProperties.class)
@AutoConfigureBefore(LoadBalancerAutoConfiguration.class)
@AutoConfigureAfter({ LoadBalancerClientConfiguration.class, StubRunnerSpringCloudAutoConfiguration.class })
@ConditionalOnStubbedDiscoveryEnabled
public class SpringCloudLoadBalancerAutoConfiguration {

	@Bean
	@Primary
	LoadBalancerClientFactory stubRunnerLoadBalancerClientFactory(BeanFactory beanFactory) {
		return new StubRunnerLoadBalancerClientFactory(beanFactory);
	}

}

class StubRunnerLoadBalancerClientFactory extends LoadBalancerClientFactory implements Closeable {

	private final BeanFactory beanFactory;

	StubRunnerLoadBalancerClientFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
		return new ContractReactorServiceInstanceLoadBalancer(this.beanFactory, serviceId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, Class<T> type) {
		return (T) getInstance(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, Class<?> clazz, Class<?>... generics) {
		return (T) getInstance(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, ResolvableType type) {
		return (T) getInstance(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getInstances(String name, Class<T> type) {
		Map<String, T> map = new HashMap<>();
		map.put(name, (T) getInstance(name));
		return map;
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

	StubbedServiceInstance(StubFinder stubFinder, StubMapperProperties stubMapperProperties, String serviceId) {
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
		String mappedServiceName = StringUtils
				.hasText(this.stubMapperProperties.fromServiceIdToIvyNotation(this.serviceId))
						? this.stubMapperProperties.fromServiceIdToIvyNotation(this.serviceId) : this.serviceId;
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
		return URI.create((isSecure() ? "https://" : "http://") + getHost() + ":" + getPort());
	}

	@Override
	public Map<String, String> getMetadata() {
		return null;
	}

}

class ContractReactorServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer, LoadBalancerLifecycle {

	private final BeanFactory beanFactory;

	private final String serviceId;

	private StubFinder stubFinder;

	private StubMapperProperties stubMapperProperties;

	ContractReactorServiceInstanceLoadBalancer(BeanFactory beanFactory, String serviceId) {
		this.beanFactory = beanFactory;
		this.serviceId = serviceId;
	}

	@Override
	public void onStart(Request request) {

	}

	@Override
	public void onStartRequest(Request request, Response lbResponse) {

	}

	@Override
	public void onComplete(CompletionContext completionContext) {

	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		return Mono.just(
				new DefaultResponse(new StubbedServiceInstance(stubFinder(), stubMapperProperties(), this.serviceId)));
	}

	private StubFinder stubFinder() {
		if (this.stubFinder == null) {
			this.stubFinder = this.beanFactory.getBean(StubFinder.class);
		}
		return this.stubFinder;
	}

	private StubMapperProperties stubMapperProperties() {
		if (this.stubMapperProperties == null) {
			this.stubMapperProperties = this.beanFactory.getBean(StubMapperProperties.class);
		}
		return this.stubMapperProperties;
	}

}
