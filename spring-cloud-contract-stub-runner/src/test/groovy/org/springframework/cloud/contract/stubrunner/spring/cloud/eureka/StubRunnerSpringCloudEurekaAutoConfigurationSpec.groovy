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

package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
import org.springframework.cloud.zookeeper.discovery.RibbonZookeeperAutoConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = ["stubrunner.camel.enabled=false",
				"spring.cloud.zookeeper.enabled=false",
				"spring.cloud.zookeeper.discovery.enabled=false",
				"stubrunner.cloud.eureka.enabled=true",
				"stubrunner.cloud.stubbed.discovery.enabled=false",
				"stubrunner.cloud.ribbon.enabled=false",
				"debug=true"])
@AutoConfigureStubRunner( ids =
		["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
				"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
				"org.springframework.cloud.contract.verifier.stubs:bootService"],
		repositoryRoot = "classpath:m2repo/repository/")
@DirtiesContext
@ActiveProfiles("eureka")
class StubRunnerSpringCloudEurekaAutoConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired @LoadBalanced RestTemplate restTemplate
	@Shared ConfigurableApplicationContext eurekaServer

	void setupSpec() {
		System.clearProperty("stubrunner.stubs.repository.root")
		System.clearProperty("stubrunner.stubs.classifier")
		eurekaServer = SpringApplication.run(EurekaServer,
				"--stubrunner.camel.enabled=false",
				"--spring.cloud.zookeeper.enabled=false",
				"--spring.cloud.zookeeper.discovery.enabled=false",
				"--stubrunner.cloud.eureka.enabled=true",
				"--stubrunner.cloud.stubbed.discovery.enabled=false",
				"--stubrunner.cloud.ribbon.enabled=false",
				"--debug=true",
				"--server.port=8761",
				"--spring.profiles.active=eureka")
	}

	void cleanupSpec() {
		System.clearProperty("stubrunner.stubs.repository.root")
		System.clearProperty("stubrunner.stubs.classifier")
		eurekaServer.close()
	}

	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
			restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}

	@Configuration
	@EnableAutoConfiguration(exclude = [RibbonZookeeperAutoConfiguration])
	@EnableEurekaClient
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate()
		}
	}

	@Configuration
	@EnableAutoConfiguration(exclude = [RibbonZookeeperAutoConfiguration])
	@EnableEurekaServer
	static class EurekaServer {

	}
}
