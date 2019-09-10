/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.GenericMessageListener;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Spring Integration configuration that iterates over the downloaded Groovy DSLs and
 * registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@ConditionalOnClass({ KafkaTemplate.class, EmbeddedKafkaBroker.class })
@ConditionalOnProperty(name = "stubrunner.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(EmbeddedKafkaBroker.class)
public class StubRunnerKafkaConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = "stubFlowRegistrar")
	public FlowRegistrar stubFlowRegistrar(ConfigurableListableBeanFactory beanFactory,
			BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner
				.getContracts();
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
				List<Contract> matchingContracts = entries.getValue();
				final String flowName = name + "_" + entries.getKey() + "_"
						+ Math.abs(matchingContracts.hashCode());
				// listener
				StubRunnerKafkaRouter router = new StubRunnerKafkaRouter(
						matchingContracts, beanFactory);
				StubRunnerKafkaRouter listener = (StubRunnerKafkaRouter) beanFactory
						.initializeBean(router, flowName);
				beanFactory.registerSingleton(flowName, listener);
				registerContainers(beanFactory, matchingContracts, flowName, listener);
			}

		}
		return new FlowRegistrar();
	}

	private void registerContainers(ConfigurableListableBeanFactory beanFactory,
			List<Contract> matchingContracts, String flowName,
			StubRunnerKafkaRouter listener) {
		// listener's container
		ConsumerFactory consumerFactory = beanFactory.getBean(ConsumerFactory.class);
		for (Contract matchingContract : matchingContracts) {
			if (matchingContract.getInput() == null) {
				continue;
			}
			String destination = MapConverter.getStubSideValuesForNonBody(
					matchingContract.getInput().getMessageFrom()).toString();
			ContainerProperties containerProperties = new ContainerProperties(
					destination);
			KafkaMessageListenerContainer container = listenerContainer(destination,
					consumerFactory, containerProperties, listener);
			String containerName = flowName + ".container";
			Object initializedContainer = beanFactory.initializeBean(container,
					containerName);
			beanFactory.registerSingleton(containerName, initializedContainer);
		}
	}

	private KafkaMessageListenerContainer listenerContainer(String queueName,
			ConsumerFactory consumerFactory, ContainerProperties containerProperties,
			GenericMessageListener listener) {
		KafkaMessageListenerContainer container = new KafkaMessageListenerContainer(
				consumerFactory, containerProperties);
		container.setupMessageListener(listener);
		return container;
	}

	static class FlowRegistrar {

	}

}
