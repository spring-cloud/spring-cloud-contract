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

package org.springframework.cloud.contract.stubrunner.messaging.camel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Camel configuration that iterates over the downloaded Groovy DSLs and registers a route
 * for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RoutesBuilder.class)
@ConditionalOnProperty(name = "stubrunner.camel.enabled", havingValue = "true",
		matchIfMissing = true)
public class StubRunnerCamelConfiguration {

	static final String STUBRUNNER_DESTINATION_URL_HEADER_NAME = "STUBRUNNER_DESTINATION_URL";

	@Bean
	public RoutesBuilder myRouter(final BatchStubRunner batchStubRunner) {
		return new SpringRouteBuilder() {
			@Override
			public void configure() throws Exception {
				Map<StubConfiguration, Collection<Contract>> contracts = batchStubRunner
						.getContracts();
				for (Map.Entry<StubConfiguration, Collection<Contract>> entry : contracts
						.entrySet()) {
					Collection<Contract> value = entry.getValue();
					MultiValueMap<String, Contract> map = new LinkedMultiValueMap<>();
					for (Contract dsl : value) {
						if (dsl == null) {
							continue;
						}
						if (dsl.getInput() != null
								&& dsl.getInput().getMessageFrom() != null
								&& StringUtils.hasText(dsl.getInput().getMessageFrom()
										.getClientValue())) {
							String from = dsl.getInput().getMessageFrom()
									.getClientValue();
							map.add(from, dsl);
						}
					}
					for (Map.Entry<String, List<Contract>> entries : map.entrySet()) {
						from(entries.getKey())
								.filter(new StubRunnerCamelPredicate(entries.getValue()))
								.process(new StubRunnerCamelProcessor())
								.dynamicRouter(header(
										StubRunnerCamelConfiguration.STUBRUNNER_DESTINATION_URL_HEADER_NAME));
					}
				}
			}
		};
	}

	@Bean
	DummyProcessor dummyStubRunnerProcessor() {
		return new DummyProcessor();
	}

	private static class DummyProcessor implements Processor {

		private static final Log log = LogFactory.getLog(DummyProcessor.class);

		@Override
		public void process(Exchange exchange) {
			if (log.isDebugEnabled()) {
				log.debug("Got exchange [" + exchange + "]");
			}
		}

	}

}
