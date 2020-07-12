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

package org.springframework.cloud.contract.stubrunner.messaging;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Supports
 * {@link org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner} by
 * loading in AutoConfigurations related to Stream and Integration only if the relevant
 * jars are in classpath.
 *
 * @author Biju Kunjummen
 */
@Configuration(proxyBeanMethods = false)
public class StubRunnerStreamsIntegrationAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(TestChannelBinderConfiguration.class)
	@ImportAutoConfiguration(classes = TestChannelBinderConfiguration.class)
	static class StreamsRelatedAutoConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(IntegrationAutoConfiguration.class)
	@ImportAutoConfiguration(classes = { IntegrationAutoConfiguration.class })
	static class IntegrationRelatedAutoConfiguration {

	}

}
