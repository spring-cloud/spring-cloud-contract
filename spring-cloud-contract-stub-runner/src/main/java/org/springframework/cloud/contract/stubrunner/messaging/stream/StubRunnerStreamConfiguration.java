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

package org.springframework.cloud.contract.stubrunner.messaging.stream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
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
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.FilterEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Spring Cloud Stream configuration that iterates over the downloaded Groovy DSLs and
 * registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ IntegrationFlows.class, InputDestination.class })
@ConditionalOnProperty(name = "stubrunner.stream.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(StubRunnerIntegrationConfiguration.class)
public class StubRunnerStreamConfiguration {

	private static final Log log = LogFactory.getLog(StubRunnerStreamConfiguration.class);

	static String resolvedDestination(BeanFactory context, String destination) {
		Map<String, BindingProperties> bindings = bindingProperties(context);
		for (Map.Entry<String, BindingProperties> entry : bindings.entrySet()) {
			if (destination.equals(entry.getValue().getDestination())) {
				if (log.isDebugEnabled()) {
					log.debug("Found a channel named [" + entry.getKey() + "] with destination [" + destination + "]");
				}
				return entry.getKey();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("No destination named [" + destination
					+ "] was found. Assuming that the destination equals the channel name");
		}
		return destination;
	}

	private static Map<String, BindingProperties> bindingProperties(BeanFactory context) {
		return context.getBean(BindingServiceProperties.class).getBindings();
	}

	@Bean
	@ConditionalOnMissingBean(name = "stubFlowRegistrar")
	@ConditionalOnBean(BindingServiceProperties.class)
	public FlowRegistrar stubFlowRegistrar(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner.getContracts();
		for (Entry<StubConfiguration, Collection<Contract>> entry : contracts.entrySet()) {
			StubConfiguration key = entry.getKey();
			Collection<Contract> value = entry.getValue();
			String name = key.getGroupId() + "_" + key.getArtifactId();
			MultiValueMap<String, Contract> map = new LinkedMultiValueMap<>();
			for (Contract dsl : value) {
				if (dsl == null) {
					continue;
				}
				if (dsl.getInput() != null && dsl.getInput().getMessageFrom() != null
						&& StringUtils.hasText(dsl.getInput().getMessageFrom().getClientValue())) {
					String from = resolvedDestination(beanFactory, dsl.getInput().getMessageFrom().getClientValue());
					map.add(from, dsl);
				}
			}
			for (Entry<String, List<Contract>> entries : map.entrySet()) {
				final String flowName = name + "_" + entries.getKey() + "_" + entries.getValue().hashCode();
				IntegrationFlowBuilder builder = IntegrationFlows.from(entries.getKey())
						.filter(new StubRunnerStreamMessageSelector(entries.getValue()),
								new Consumer<FilterEndpointSpec>() {
									@Override
									public void accept(FilterEndpointSpec e) {
										e.id(flowName + ".filter");
									}
								})
						.transform(new StubRunnerStreamTransformer(entries.getValue()))
						.route(new StubRunnerMessageRouter(entries.getValue(), beanFactory));
				beanFactory.initializeBean(builder.get(), flowName);
				beanFactory.getBean(flowName + ".filter", Lifecycle.class).start();
			}

		}
		return new FlowRegistrar();
	}

	static class FlowRegistrar {

	}

}
