/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.kafka;

import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tim Ysewyn
 */
public class ContractVerifierKafkaConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(KafkaAutoConfiguration.class, ContractVerifierKafkaConfiguration.class))
			.withUserConfiguration(CustomConfiguration.class);

	@Test
	public void shouldCreateBeansByDefault() {
		this.contextRunner.run((context) -> {
			assertThat(context.getBeansOfType(KafkaStubMessages.class)).hasSize(1);
			assertThat(context.getBeansOfType(ContractVerifierKafkaHelper.class)).hasSize(1);
		});
	}

	@Test
	public void shouldNotCreateBeansWhenDisabled() {
		this.contextRunner.withPropertyValues("stubrunner.kafka.enabled=false").run((context) -> {
			assertThat(context.getBeansOfType(KafkaStubMessages.class)).hasSize(0);
			assertThat(context.getBeansOfType(ContractVerifierKafkaHelper.class)).hasSize(0);
		});
	}

	@Test
	public void shouldCreateBeansWhenExplicitlyEnabled() {
		this.contextRunner.withPropertyValues("stubrunner.kafka.enabled=true").run((context) -> {
			assertThat(context.getBeansOfType(KafkaStubMessages.class)).hasSize(1);
			assertThat(context.getBeansOfType(ContractVerifierKafkaHelper.class)).hasSize(1);
		});
	}

	static class CustomConfiguration {

		@Bean
		public EmbeddedKafkaBroker embeddedKafkaBroker() {
			return new EmbeddedKafkaBroker(1);
		}

	}

}
