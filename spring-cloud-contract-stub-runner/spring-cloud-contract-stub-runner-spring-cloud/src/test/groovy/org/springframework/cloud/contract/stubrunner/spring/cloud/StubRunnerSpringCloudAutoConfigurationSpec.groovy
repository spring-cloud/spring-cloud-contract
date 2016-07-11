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

package org.springframework.cloud.contract.stubrunner.spring.cloud

import groovy.util.logging.Slf4j
import org.apache.curator.test.TestingServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.zookeeper.ZookeeperProperties
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.SocketUtils
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@WebIntegrationTest(randomPort = true)
@IntegrationTest
@Slf4j
class StubRunnerSpringCloudAutoConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired @LoadBalanced RestTemplate restTemplate
	@Autowired ZookeeperServiceDiscovery zookeeperServiceDiscovery
	@Autowired ConfigurableApplicationContext applicationContext

	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
			restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}

	TestingServer startTestingServer() {
		try {
			return new TestingServer(SocketUtils.findAvailableTcpPort())
		} catch (Exception e) {
			log.error("Exception occurred ")
		}
	}

	def cleanup() {
		zookeeperServiceDiscovery.serviceDiscovery.close()
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
