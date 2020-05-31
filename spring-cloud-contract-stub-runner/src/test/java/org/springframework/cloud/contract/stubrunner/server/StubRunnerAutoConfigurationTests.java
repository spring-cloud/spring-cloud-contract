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

package org.springframework.cloud.contract.stubrunner.server;

import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tim Ysewyn
 */
public class StubRunnerAutoConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(StubRunnerConfiguration.class,
					StubRunnerAutoConfiguration.class));

	@Test
	public void shouldCreateBeansByDefault() {
		this.contextRunner.run((context) -> {
			assertThat(context.getBeansOfType(ContractVerifierMessaging.class))
					.hasSize(1);
			assertThat(context.getBeansOfType(TriggerController.class)).hasSize(1);
		});
	}

	@Test
	public void shouldNotCreateBeansWhenDisabled() {
		this.contextRunner.withPropertyValues("stubrunner.messaging.enabled=false")
				.run((context) -> {
					assertThat(context.getBeansOfType(ContractVerifierMessaging.class))
							.hasSize(0);
					assertThat(context.getBeansOfType(TriggerController.class))
							.hasSize(0);
				});
	}

	@Test
	public void shouldCreateBeansWhenExplicitlyEnabled() {
		this.contextRunner.withPropertyValues("stubrunner.messaging.enabled=true")
				.run((context) -> {
					assertThat(context.getBeansOfType(ContractVerifierMessaging.class))
							.hasSize(1);
					assertThat(context.getBeansOfType(TriggerController.class))
							.hasSize(1);
				});
	}

}
