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

package org.springframework.cloud.contract.stubrunner.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessageBuilder;
import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierMessageBuilder;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
public class StubRunnerBackupAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ContractVerifierMessaging<?,?> noOpContractVerifierMessaging() {
		return new NoOpContractVerifierMessaging();
	}

	@Bean
	@ConditionalOnMissingBean
	public ContractVerifierMessageBuilder<?,?> noOpContractVerifierMessageBuilder() {
		return new NoOpContractVerifierMessageBuilder();
	}
}
