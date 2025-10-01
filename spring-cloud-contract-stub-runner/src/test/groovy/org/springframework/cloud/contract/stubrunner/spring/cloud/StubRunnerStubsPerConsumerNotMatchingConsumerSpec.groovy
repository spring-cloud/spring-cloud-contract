/*
 * Copyright 2013-present the original author or authors.
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

import java.util.function.Function

import org.junit.jupiter.api.Test

import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

import static org.assertj.core.api.Assertions.assertThat

@AutoConfigureStubRunner(ids = "org.springframework.cloud.contract.verifier.stubs:producerWithMultipleConsumers",
		repositoryRoot = "classpath:m2repo/repository/",
		stubsMode = StubRunnerProperties.StubsMode.REMOTE,
		stubsPerConsumer = true)
@ActiveProfiles("streamconsumer")
class StubRunnerStubsPerConsumerNotMatchingConsumerSpec {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(Config))
			.withPropertyValues("spring.application.name=bar-not-matching-consumer",
					"spring.cloud.contract.stubrunner.jms.enabled=false", "spring.profiles.active=streamconsumer", "spring.cloud.discovery.enabled=false", "spring.cloud.service-registry.auto-registration.enabled=false", "spring.cloud.contract.stubrunner.ids=org.springframework.cloud.contract.verifier.stubs:producerWithMultipleConsumers", "spring.cloud.contract.stubrunner.repositoryRoot=classpath:m2repo/repository/", "spring.cloud.contract.stubrunner.stubsMode=REMOTE", "spring.cloud.contract.stubrunner.stubsPerConsumer=true");

	@Test
	void 'should fail to start when JAR is found but there are no stubs or contracts'() {
		expect:
			this.contextRunner.run((context) -> {
				assertThat(context).hasFailed();
				assertThat(context.getStartupFailure()).hasMessageContaining("No stubs or contracts were found for [org.springframework.cloud.contract.verifier.stubs:producerWithMultipleConsumers:0.0.1-SNAPSHOT:stubs] and the switch to fail on no stubs was set")
			});
	}

	@Configuration
	@EnableAutoConfiguration(exclude = [ZookeeperAutoConfiguration])
	@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
	@Import(value = StubRunnerConfiguration)
	static class Config {
		@Bean
		Function output() {
			return { Object o ->
				println(o)
				return o
			}
		}
	}
}
