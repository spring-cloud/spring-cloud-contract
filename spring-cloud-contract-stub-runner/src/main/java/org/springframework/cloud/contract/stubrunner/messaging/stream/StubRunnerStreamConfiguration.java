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

package org.springframework.cloud.contract.stubrunner.messaging.stream;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.messaging.integration.StubRunnerIntegrationConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.FilterEndpointSpec;
import org.springframework.integration.dsl.GenericEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

/**
 * Spring Cloud Stream configuration that iterates over the downloaded Groovy DSLs and
 * registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@ConditionalOnClass({IntegrationFlows.class, EnableBinding.class})
@ConditionalOnProperty(name="stubrunner.stream.enabled", havingValue="true", matchIfMissing=true)
@AutoConfigureBefore(StubRunnerIntegrationConfiguration.class)
public class StubRunnerStreamConfiguration {

	private static final Logger log = LoggerFactory
			.getLogger(StubRunnerStreamConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(name="stubFlowRegistrar")
	@ConditionalOnBean(BindingServiceProperties.class)
	public FlowRegistrar stubFlowRegistrar(AutowireCapableBeanFactory beanFactory,
			BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner
				.getContracts();
		for (Entry<StubConfiguration, Collection<Contract>> entry : contracts
				.entrySet()) {
			StubConfiguration key = entry.getKey();
			Collection<Contract> value = entry.getValue();
			String name = key.getGroupId() + "_" + key.getArtifactId();
			for (Contract dsl : value) {
				if (dsl == null) {
					continue;
				}
				if (dsl.getInput() != null
							&& dsl.getInput().getMessageFrom() != null
							&& StringUtils.hasText(
									dsl.getInput().getMessageFrom().getClientValue())) {
					final String flowName = name + "_" + dsl.getLabel() + "_"
							+ dsl.hashCode();
					String from = resolvedDestination(beanFactory,
							dsl.getInput().getMessageFrom().getClientValue());
					IntegrationFlowBuilder builder = IntegrationFlows.from(from)
							.filter(new StubRunnerStreamMessageSelector(dsl),
									new Consumer<FilterEndpointSpec>() {
										@Override
										public void accept(FilterEndpointSpec e) {
											e.id(flowName + ".filter");
										}
									})
							.transform(new StubRunnerStreamTransformer(dsl),
									new Consumer<GenericEndpointSpec<MessageTransformingHandler>>() {
										@Override
										public void accept(
												GenericEndpointSpec<MessageTransformingHandler> e) {
											e.id(flowName + ".transformer");
										}
									});
					if (dsl.getOutputMessage() != null
							&& dsl.getOutputMessage().getSentTo() != null) {
						builder = builder.channel(resolvedDestination(beanFactory,
								dsl.getOutputMessage().getSentTo().getClientValue()));
					}
					else {
						builder = builder.handle(new DummyMessageHandler(), "handle");
					}
					beanFactory.initializeBean(builder.get(), flowName);
					beanFactory.getBean(flowName + ".filter", Lifecycle.class)
							.start();
					beanFactory.getBean(flowName + ".transformer", Lifecycle.class)
							.start();
				} else if (dsl.getOutputMessage() != null
						&& dsl.getOutputMessage().getSentTo() != null
						&& StringUtils.hasText(
						dsl.getOutputMessage().getSentTo().getClientValue())) {
					BinderAwareChannelResolver resolver = beanFactory.getBean(BinderAwareChannelResolver.class);
					resolver.resolveDestination(dsl.getOutputMessage().getSentTo().getClientValue());
				}
			}
		}
		return new FlowRegistrar();
	}

	private String resolvedDestination(AutowireCapableBeanFactory context,
			String destination) {
		Map<String, BindingProperties> bindings = bindingProperties(context);
		for (Map.Entry<String, BindingProperties> entry : bindings.entrySet()) {
			if (destination.equals(entry.getValue().getDestination())) {
				if (log.isDebugEnabled()) {
					log.debug("Found a channel named [{}] with destination [{}]",
							entry.getKey(), destination);
				}
				return entry.getKey();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(
					"No destination named [{}] was found. Assuming that the destination equals the channel name",
					destination);
		}
		return destination;
	}

	private Map<String, BindingProperties> bindingProperties(AutowireCapableBeanFactory context) {
		return context.getBean(BindingServiceProperties.class).getBindings();
	}

	private static class DummyMessageHandler {
		public void handle(Message<?> message) {}
	}

	static class FlowRegistrar {
	}
}
