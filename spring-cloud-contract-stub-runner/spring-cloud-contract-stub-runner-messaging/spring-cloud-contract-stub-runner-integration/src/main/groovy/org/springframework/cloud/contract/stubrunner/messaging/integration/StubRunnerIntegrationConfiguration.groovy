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

package org.springframework.cloud.contract.stubrunner.messaging.integration

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.cloud.contract.stubrunner.BatchStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration
import org.springframework.context.Lifecycle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.integration.dsl.FilterEndpointSpec
import org.springframework.integration.dsl.GenericEndpointSpec
import org.springframework.integration.dsl.IntegrationFlowBuilder
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.Message

/**
 * Spring Integration configuration that iterates over the downloaded Groovy DSLs
 * and registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@CompileStatic
class StubRunnerIntegrationConfiguration {

	@Bean
	FlowRegistrar flowRegistrar(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner.contracts
		contracts.each { StubConfiguration key, Collection<Contract> value ->
			String name = "${key.groupId}_${key.artifactId}"
			value.findAll { it?.input?.messageFrom?.clientValue }.each { Contract dsl ->
				String flowName = "${name}_${dsl.label}_${dsl.hashCode()}"
				IntegrationFlowBuilder builder = IntegrationFlows.from(dsl.input.messageFrom.clientValue)
					.filter(new StubRunnerIntegrationMessageSelector(dsl), { FilterEndpointSpec e -> e.id("${flowName}.filter") } )
					.transform(new StubRunnerIntegrationTransformer(dsl), { GenericEndpointSpec e -> e.id("${flowName}.transformer") })
				if (dsl.outputMessage?.sentTo) {
					builder = builder.channel(dsl.outputMessage.sentTo.clientValue)
				} else {
					builder = builder.handle(new DummyMessageHandler(), "handle")
				}
				beanFactory.initializeBean(builder.get(), flowName)
				beanFactory.getBean("${flowName}.filter", Lifecycle.class).start();
				beanFactory.getBean("${flowName}.transformer", Lifecycle.class).start();
			}
		}
		return new FlowRegistrar()
	}

	@CompileStatic
	private static class DummyMessageHandler {
		void handle(Message message) {}
	}

	static class FlowRegistrar {}
}
