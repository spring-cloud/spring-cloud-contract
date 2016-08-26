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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Wraps {@link DiscoveryClient} in a Stub Runner implementation that tries to find
 * a corresponding WireMock server for a searched dependency
 *
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(StubMapperProperties.class)
@ConditionalOnClass(DiscoveryClient.class)
@ConditionalOnProperty(value = "stubrunner.cloud.enabled", matchIfMissing = true)
public class StubRunnerSpringCloudAutoConfiguration {

	@Bean
	@ConditionalOnBean(DiscoveryClient.class)
	@Primary
	@ConditionalOnStubbedDiscoveryEnabled
	public DiscoveryClient stubRunnerDiscoveryClientWrapper(DiscoveryClient discoveryClient,
			StubFinder stubFinder,
			StubMapperProperties stubMapperProperties,
			@Value("${spring.application.name:unknown}") String springAppName) {
		return new StubRunnerDiscoveryClient(discoveryClient, stubFinder, stubMapperProperties, springAppName);
	}

	@Bean
	@Primary
	@ConditionalOnMissingBean(DiscoveryClient.class)
	@ConditionalOnStubbedDiscoveryEnabled
	public DiscoveryClient stubRunnerDiscoveryClient(StubFinder stubFinder,
			StubMapperProperties stubMapperProperties,
			@Value("${spring.application.name:unknown}") String springAppName) {
		return new StubRunnerDiscoveryClient(stubFinder, stubMapperProperties, springAppName);
	}

}
