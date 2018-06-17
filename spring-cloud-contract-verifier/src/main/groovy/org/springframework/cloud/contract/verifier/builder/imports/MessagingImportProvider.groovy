package org.springframework.cloud.contract.verifier.builder.imports
/**
 * @author Olga Maciaszek-Sharma
 */
class MessagingImportProvider {

	/**
	 * Returns list of imports for messaging test contracts.
	 * @return list of imports
	 */
	static List<String> getImports() {
		return ['javax.inject.Inject',
		        'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper',
		        'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage',
		        'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging',
		]
	}

	/**
	 * Returns list of static imports for messaging test contracts.
	 * @return list of static imports
	 */
	static List<String> getStaticImports() {
		return ['org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers']
	}
}
