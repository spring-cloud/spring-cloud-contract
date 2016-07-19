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

package org.springframework.cloud.contract.stubrunner.messaging.integration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.FilterEndpointSpec;
import org.springframework.integration.dsl.GenericEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.messaging.Message;

/**
 * Spring Integration configuration that iterates over the downloaded Groovy DSLs and
 * registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@ConditionalOnClass(IntegrationFlowBuilder.class)
public class StubRunnerIntegrationConfiguration {

	@Bean
	@ConditionalOnMissingBean(name="stubFlowRegistrar")
	public FlowRegistrar stubFlowRegistrar(AutowireCapableBeanFactory beanFactory,
			BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner
				.getContracts();
		for (Entry<StubConfiguration, Collection<Contract>> entry : contracts.entrySet()) {
			String name = entry.getKey().getGroupId() + "_"
					+ entry.getKey().getArtifactId();
			for (Contract dsl : entry.getValue()) {
				if (dsl.getInput() != null && dsl.getInput().getMessageFrom() != null
						&& dsl.getInput().getMessageFrom().getClientValue() != null) {
					final String flowName = name + "_" + dsl.getLabel() + "_" + dsl.hashCode();
					IntegrationFlowBuilder builder = IntegrationFlows
							.from(dsl.getInput().getMessageFrom().getClientValue())
							.filter(new StubRunnerIntegrationMessageSelector(dsl),
									new Consumer<FilterEndpointSpec>() {
										@Override
										public void accept(FilterEndpointSpec e) {
											e.id(flowName + ".filter");
										}
									})
							.transform(new StubRunnerIntegrationTransformer(dsl),
									new Consumer<GenericEndpointSpec<MessageTransformingHandler>>() {
										@Override
										public void accept(
												GenericEndpointSpec<MessageTransformingHandler> e) {
											e.id(flowName + ".transformer");
										}
									});
					if (dsl.getOutputMessage() != null) {
						builder = builder.channel(
								dsl.getOutputMessage().getSentTo().getClientValue());
					}
					else {
						builder = builder.handle(new DummyMessageHandler(), "handle");
					}
					beanFactory.initializeBean(builder.get(), flowName);
					beanFactory.getBean(flowName + ".filter", Lifecycle.class).start();
					beanFactory.getBean(flowName + ".transformer", Lifecycle.class)
							.start();
				}			
			}
		}
		return new FlowRegistrar();
	}

	private static class DummyMessageHandler {
		@SuppressWarnings("unused")
		public void handle(Message<?> message) {
		}
	}

	static class FlowRegistrar {
	}
}
