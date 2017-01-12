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

package org.springframework.cloud.contract.stubrunner.spring.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryDisabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Autoconfiguration for registering stubs in a Zookeeper Service discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter({ StubRunnerConfiguration.class, CuratorServiceDiscoveryAutoConfiguration.class })
@ConditionalOnClass(org.apache.curator.x.discovery.ServiceInstance.class)
@ConditionalOnStubbedDiscoveryDisabled
@ConditionalOnZookeeperDiscoveryEnabled
@ConditionalOnProperty(value = "stubrunner.cloud.zookeeper.enabled", matchIfMissing = true)
public class StubRunnerSpringCloudZookeeperAutoConfiguration {

	@Bean(initMethod = "registerStubs")
	public StubsRegistrar stubsRegistrar(StubRunning stubRunning, CuratorFramework curatorFramework,
			StubMapperProperties stubMapperProperties, ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		return new ZookeeperStubsRegistrar(stubRunning, curatorFramework, stubMapperProperties, zookeeperDiscoveryProperties);
	}
}
