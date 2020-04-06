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

package org.springframework.cloud.contract.stubrunner.messaging.integration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.FilterEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Spring Integration configuration that iterates over the downloaded Groovy DSLs and
 * registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(IntegrationFlowBuilder.class)
@ConditionalOnProperty(name = "stubrunner.integration.enabled", havingValue = "true",
		matchIfMissing = true)
public class StubRunnerIntegrationConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = "stubFlowRegistrar")
	public FlowRegistrar stubFlowRegistrar(AutowireCapableBeanFactory beanFactory,
			BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner
				.getContracts();
		IntegrationFlowBuilder dummyBuilder = IntegrationFlows
				.from(DummyMessageHandler.CHANNEL_NAME)
				.handle(new DummyMessageHandler(), "handle");
		beanFactory.initializeBean(dummyBuilder.get(),
				DummyMessageHandler.CHANNEL_NAME + ".flow");
		for (Entry<StubConfiguration, Collection<Contract>> entry : contracts
				.entrySet()) {
			StubConfiguration key = entry.getKey();
			Collection<Contract> value = entry.getValue();
			String name = key.getGroupId() + "_" + key.getArtifactId();
			MultiValueMap<String, Contract> map = new LinkedMultiValueMap<>();
			for (Contract dsl : value) {
				if (dsl == null) {
					continue;
				}
				if (dsl.getInput() != null && dsl.getInput().getMessageFrom() != null
						&& StringUtils.hasText(
								dsl.getInput().getMessageFrom().getClientValue())) {
					String from = dsl.getInput().getMessageFrom().getClientValue();
					map.add(from, dsl);
				}
			}
			for (Entry<String, List<Contract>> entries : map.entrySet()) {
				final String flowName = name + "_" + entries.getKey() + "_"
						+ entries.getValue().hashCode();
				IntegrationFlowBuilder builder = IntegrationFlows
						.from(entries.getKey()).filter(
								new StubRunnerIntegrationMessageSelector(
										entries.getValue()),
								new Consumer<FilterEndpointSpec>() {
									@Override
									public void accept(FilterEndpointSpec e) {
										e.id(flowName + ".filter");
									}
								})
						.transform(
								new StubRunnerIntegrationTransformer(entries.getValue()))
						.route(new StubRunnerIntegrationRouter(entries.getValue(),
								beanFactory));
				beanFactory.initializeBean(builder.get(), flowName);
				beanFactory.getBean(flowName + ".filter", Lifecycle.class).start();
			}

		}
		return new FlowRegistrar();
	}

	static class DummyMessageHandler {

		static String CHANNEL_NAME = "stub_runner_dummy_channel";

		public void handle(Message<?> message) {
		}

	}

	static class FlowRegistrar {

	}

}
