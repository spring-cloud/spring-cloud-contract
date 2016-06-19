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

package org.springframework.cloud.contract.stubrunner.messaging.camel

import org.apache.camel.RoutesBuilder
import org.apache.camel.spring.SpringRouteBuilder
import org.springframework.cloud.contract.verifier.dsl.Contract
import org.springframework.cloud.contract.stubrunner.BatchStubRunner
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Camel configuration that iterates over the downloaded Groovy DSLs
 * and registers a route for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
class StubRunnerCamelConfiguration {

	@Bean
	RoutesBuilder myRouter(BatchStubRunner batchStubRunner) {
		return new SpringRouteBuilder() {
			@Override
			public void configure() throws Exception {
				Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner.contracts
				(contracts.values().flatten() as Collection<Contract>).findAll { it?.input?.messageFrom?.clientValue && it?.outputMessage?.sentTo }.each {
					from(it.input.messageFrom.clientValue)
							.filter(new StubRunnerCamelPredicate(it))
							.process(new StubRunnerCamelProcessor(it))
							.to(it.outputMessage.sentTo.clientValue)
				}
			}
		};
	}
}
