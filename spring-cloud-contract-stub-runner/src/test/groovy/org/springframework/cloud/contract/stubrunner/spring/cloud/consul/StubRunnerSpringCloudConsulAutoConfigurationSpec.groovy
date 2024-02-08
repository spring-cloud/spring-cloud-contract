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

package org.springframework.cloud.contract.stubrunner.spring.cloud.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import static org.mockito.BDDMockito.then
import static org.mockito.Matchers.argThat
import static org.mockito.Mockito.mock
/**
 * @author Marcin Grzejszczak
 */
@SpringBootTest(classes = Config, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = ["eureka.client.enabled=false",
				"spring.cloud.zookeeper.enabled=false",
				"stubrunner.cloud.stubbed.discovery.enabled=false",
				"stubrunner.cloud.eureka.enabled=false",
				"spring.cloud.zookeeper.discovery.enabled=false",
				"stubrunner.cloud.consul.enabled=true",
				"stubrunner.cloud.zookeeper.enabled=false",
				"debug=true"])
// tag::autoconfigure[]
@AutoConfigureStubRunner(ids = ["org.springframework.cloud.contract.verifier.stubs:loanIssuance",
 "org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer",
 "org.springframework.cloud.contract.verifier.stubs:bootService"] ,
stubsMode = StubRunnerProperties.StubsMode.REMOTE ,
repositoryRoot = "classpath:m2repo/repository/" )
// end::autoconfigure[]
class StubRunnerSpringCloudConsulAutoConfigurationSpec {

	@Autowired
	ConsulClient client

	@BeforeAll
	static void setupSpec() {
		System.clearProperty("stubrunner.stubs.repository.root")
		System.clearProperty("stubrunner.stubs.classifier")
	}

	@AfterAll
	static void cleanupSpec() {
		setupSpec()
	}

	@Test
	void 'should make service discovery work for #serviceName'() {

		given:
			def serviceName = ['loanIssuance:loanIssuance', 'bootService:bootService', 'fraudDetectionServer:someNameThatShouldMapFraudDetectionServer']
		when:
			serviceName.each {
				and:
					final String expectedId = it.split(':')[0]
					final String expectedName = it.split(':')[1]
				then: 'Consul registration took place for 3 stubs'
					then(client).should().agentServiceRegister(argThat(new NewServiceMatcher(expectedId, expectedName)))
			}
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

	@CompileStatic
	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class Config {

		@Bean
		ConsulClient mockedConsulClient() {
			return mock(ConsulClient)
		}

		@Bean
		ConsulDiscoveryProperties consulDiscoveryProperties() {
			return mock(ConsulDiscoveryProperties)
		}
	}
}
