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

package org.springframework.cloud.contract.stubrunner.spring.cloud.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.AfterClass
import org.junit.BeforeClass
import org.mockito.ArgumentMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.mockito.BDDMockito.then
import static org.mockito.Matchers.argThat
import static org.mockito.Mockito.mock
/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringBootContextLoader)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = ["stubrunner.camel.enabled=false",
				"eureka.client.enabled=false",
				"spring.cloud.zookeeper.enabled=false",
				"stubrunner.cloud.stubbed.discovery.enabled=false",
				"stubrunner.cloud.eureka.enabled=false",
				"spring.cloud.zookeeper.discovery.enabled=false",
				"stubrunner.cloud.consul.enabled=true",
				"stubrunner.cloud.zookeeper.enabled=false",
				"debug=true"])
@AutoConfigureStubRunner( ids =
		["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
		"org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
		"org.springframework.cloud.contract.verifier.stubs:bootService"],
		repositoryRoot = "classpath:m2repo/repository/")
@DirtiesContext
class StubRunnerSpringCloudConsulAutoConfigurationSpec extends Specification {

	@Autowired ConsulClient client

	@BeforeClass
	@AfterClass
	static void setupProps() {
		System.clearProperty("stubrunner.stubs.repository.root");
		System.clearProperty("stubrunner.stubs.classifier");
	}

	def 'should make service discovery work for #serviceName'() {
		given:
			final String expectedId = serviceName.split(':')[0]
			final String expectedName = serviceName.split(':')[1]
		when: 'Consul registration took place for 3 stubs'
			then(client).should().agentServiceRegister(argThat(new NewServiceMatcher(expectedId, expectedName)))
		then:
			noExceptionThrown()
		where:
			serviceName << ['loanIssuance:loanIssuance', 'bootService:bootService', 'fraudDetectionServer:someNameThatShouldMapFraudDetectionServer']
	}

	private static class NewServiceMatcher implements ArgumentMatcher<NewService> {

		private final String expectedId
		private final String expectedName

		NewServiceMatcher(String expectedId, String expectedName) {
			this.expectedId = expectedId
			this.expectedName = expectedName
		}

		@Override
		boolean matches(NewService item) {
			return item.id == expectedId && item.name == expectedName
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class Config {

		@Bean
		ConsulClient mockedConsulClient() {
			return mock(ConsulClient)
		}
	}
}
