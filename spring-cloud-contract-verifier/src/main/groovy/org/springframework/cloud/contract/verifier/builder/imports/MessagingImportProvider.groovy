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
		return ['org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers']
	}
}
