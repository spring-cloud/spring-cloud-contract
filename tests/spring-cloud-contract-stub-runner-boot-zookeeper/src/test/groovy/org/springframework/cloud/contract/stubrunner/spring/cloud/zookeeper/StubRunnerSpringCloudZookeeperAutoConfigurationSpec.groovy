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

package org.springframework.cloud.contract.stubrunner.spring.cloud.zookeeper

import org.apache.curator.test.TestingServer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.zookeeper.ZookeeperProperties
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.SocketUtils
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = ["stubrunner.camel.enabled=false",
				"stubrunner.cloud.stubbed.discovery.enabled=false",
				"debug=true"])
@AutoConfigureStubRunner( ids =
		["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
		"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
		"org.springframework.cloud.contract.verifier.stubs:bootService"],
		repositoryRoot = "classpath:m2repo/repository/")
@DirtiesContext
class StubRunnerSpringCloudZookeeperAutoConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired @LoadBalanced RestTemplate restTemplate
	@Autowired ZookeeperServiceDiscovery zookeeperServiceDiscovery

	@BeforeClass
	@AfterClass
	static void setupProps() {
		System.clearProperty("stubrunner.stubs.repository.root");
		System.clearProperty("stubrunner.stubs.classifier");
	}

	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
			restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}

	def 'should have all apps registered in Service Discovery'() {
		expect:
			!zookeeperServiceDiscovery.getServiceDiscovery().queryForInstances('loanIssuance').empty
			!zookeeperServiceDiscovery.getServiceDiscovery().queryForInstances('someNameThatShouldMapFraudDetectionServer').empty
	}

	def cleanup() {
		zookeeperServiceDiscovery?.serviceDiscovery?.close()
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class Config {

		@Bean
		TestingServer testingServer() {
			return new TestingServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean
		ZookeeperProperties zookeeperProperties() {
			return new ZookeeperProperties(connectString: testingServer().connectString)
		}

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate()
		}
	}
}
