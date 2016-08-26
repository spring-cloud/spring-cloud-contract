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

import org.junit.AfterClass
import org.junit.BeforeClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.consul.ConsulAutoConfiguration
import org.springframework.cloud.contract.stubrunner.StubFinder
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration
import org.springframework.cloud.zookeeper.discovery.RibbonZookeeperAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
//TODO: Document that ribbon.eureka.enabled=false needs to be set or override it somehow
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = ["stubrunner.camel.enabled=false",
				"spring.cloud.zookeeper.enabled=false",
				"spring.cloud.consul.enabled=false",
				"eureka.client.enabled=false",
				"stubrunner.cloud.stubbed.discovery.enabled=true",
				"spring.cloud.consul.discovery.enabled=false",
				"spring.cloud.zookeeper.discovery.enabled=false"])
// tag::autoconfigure[]
@AutoConfigureStubRunner(
		ids = ["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
		"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
		"org.springframework.cloud.contract.verifier.stubs:bootService"],
		repositoryRoot = "classpath:m2repo/repository/")
// end::autoconfigure[]
@DirtiesContext
class StubRunnerSpringCloudAutoConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder
	@Autowired @LoadBalanced RestTemplate restTemplate

	@BeforeClass
	@AfterClass
	static void setupProps() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
	}

	// tag::test[]
	def 'should make service discovery work'() {
		expect: 'WireMocks are running'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
		and: 'Stubs can be reached via load service discovery'
			restTemplate.getForObject('http://loanIssuance/name', String) == 'loanIssuance'
			restTemplate.getForObject('http://someNameThatShouldMapFraudDetectionServer/name', String) == 'fraudDetectionServer'
	}
	// end::test[]

	@Configuration
	@EnableAutoConfiguration(exclude = [RibbonZookeeperAutoConfiguration, EurekaClientAutoConfiguration,
			ConsulAutoConfiguration, ZookeeperAutoConfiguration])
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate()
		}
	}
}
