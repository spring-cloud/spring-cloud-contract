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

package org.springframework.cloud.contract.verifier.messaging.boot;

import org.junit.Test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tim Ysewyn
 */
public class AutoConfigureMessageVerifierTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(Configuration.class);

	@Test
	public void shouldConfigureForNoOpWhenMissingImplementation() {
		this.contextRunner.withClassLoader(
				new FilteredClassLoader(org.apache.camel.Message.class, org.springframework.messaging.Message.class,
						JmsTemplate.class, KafkaTemplate.class, RabbitTemplate.class, EnableBinding.class))
				.run((context) -> {
					assertThat(context.getBeansOfType(MessageVerifierSender.class)).hasSize(1);
					assertThat(context.getBeansOfType(NoOpStubMessages.class)).hasSize(1);
				});
	}

	@AutoConfigureMessageVerifier
	private static class Configuration {

	}

}
