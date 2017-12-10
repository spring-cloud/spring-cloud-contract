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

package org.springframework.cloud.contract.stubrunner.spring.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryDisabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Autoconfiguration for registering stubs in a Zookeeper Service discovery
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(value = {StubRunnerConfiguration.class,
	ConsulServiceRegistryAutoConfiguration.class})
@ConditionalOnClass(ConsulClient.class)
@ConditionalOnStubbedDiscoveryDisabled
@ConditionalOnProperty(value = "stubrunner.cloud.consul.enabled", matchIfMissing = true)
public class StubRunnerSpringCloudConsulAutoConfiguration {

	@Bean(initMethod = "registerStubs")
	public StubsRegistrar stubsRegistrar(StubRunning stubRunning, ConsulClient consulClient,
		StubMapperProperties stubMapperProperties,
		ConsulDiscoveryProperties consulDiscoveryProperties,
		InetUtils inetUtils) {
		return new ConsulStubsRegistrar(stubRunning, consulClient, stubMapperProperties,
			consulDiscoveryProperties, inetUtils);
	}
}
