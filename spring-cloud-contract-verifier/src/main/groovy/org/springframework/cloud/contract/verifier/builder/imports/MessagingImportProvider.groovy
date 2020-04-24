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

package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.CompileStatic

/**
 * Provides imports based on test framework and test mode.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
@Deprecated
class MessagingImportProvider {

	/**
	 * @return list of imports for messaging test contracts.
	 */
	static List<String> getImports() {
		return ['javax.inject.Inject',
				'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper',
				'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage',
				'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging',
		]
	}

	/**
	 * @return list of static imports for messaging test contracts.
	 */
	static List<String> getStaticImports() {
		return ['org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers', 'org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes']
	}
}
