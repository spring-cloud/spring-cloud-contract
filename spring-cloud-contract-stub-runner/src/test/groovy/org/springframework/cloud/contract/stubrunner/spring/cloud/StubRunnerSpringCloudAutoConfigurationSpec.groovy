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

package org.springframework.cloud.contract.stubrunner.spring.cloud

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.consul.ConsulAutoConfiguration
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.stubrunner.spring.cloud.loadbalancer.StubRunnerLoadBalancerClientFactory
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate

/**
 * @author Marcin Grzejszczak
 */
@SpringBootTest(classes = Config)
@ActiveProfiles("cloudtest")
// tag::autoconfigure[]
@AutoConfigureStubRunner(
		ids = ["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
				"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
				"org.springframework.cloud.contract.verifier.stubs:bootService"],
		stubsMode = StubRunnerProperties.StubsMode.REMOTE,
		repositoryRoot = "classpath:m2repo/repository/")
// end::autoconfigure[]
@Disabled
class StubRunnerSpringCloudAutoConfigurationSpec {

	@Autowired
	StubFinder stubFinder
	@Autowired
	@LoadBalanced
	RestTemplate restTemplate
	@Autowired
	LoadBalancerClientFactory loadBalancerClientFactory;

	@BeforeAll
	static void setupSpec() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
	}

	@AfterAll
	static void cleanupSpec() {
		setupSpec()
	}

	@BeforeEach
	void setup() {
		assert loadBalancerClientFactory.getClass().getSimpleName() == "StubRunnerLoadBalancerClientFactory"
	}

	// tag::test[]
	@Test
	void 'should make service discovery work'() {
		expect: 'WireMocks are running'
		assert "${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
		assert "${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
		assert restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
		assert restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}
	// end::test[]

	@Configuration
	@EnableAutoConfiguration(exclude = [EurekaClientAutoConfiguration,
			ConsulAutoConfiguration, ZookeeperAutoConfiguration])
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate()
		}
	}
}
