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

import org.junit.AfterClass
import org.junit.BeforeClass
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient
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
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@ActiveProfiles("cloudtest")
@AutoConfigureStubRunner(
		ids = ["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
				"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
				"org.springframework.cloud.contract.verifier.stubs:bootService"],
		stubsMode = StubRunnerProperties.StubsMode.REMOTE,
		repositoryRoot = "classpath:m2repo/repository/")
class StubRunnerSpringCloudReactiveAutoConfigurationSpec extends Specification {
	@Autowired
	StubFinder stubFinder
	@Autowired
	ReactiveDiscoveryClient reactiveDiscoveryClient;
	@Autowired
	LoadBalancerClientFactory loadBalancerClientFactory;
	RestTemplate restTemplate = new RestTemplate()

	@BeforeClass
	@AfterClass
	static void setupProps() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
	}

	def setup() {
		assert loadBalancerClientFactory instanceof StubRunnerLoadBalancerClientFactory
	}

	// tag::test[]
	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			ServiceInstance loanIssuance = reactiveDiscoveryClient.getInstances('loanIssuance').blockFirst()
			restTemplate.getForObject(loanIssuance.uri.toString() + '/name', String) == 'loanIssuance'
			ServiceInstance fraudDetection = reactiveDiscoveryClient.getInstances('someNameThatShouldMapFraudDetectionServer').blockFirst()
			restTemplate.getForObject(fraudDetection.uri.toString() + '/name', String)== 'fraudDetectionServer'
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
