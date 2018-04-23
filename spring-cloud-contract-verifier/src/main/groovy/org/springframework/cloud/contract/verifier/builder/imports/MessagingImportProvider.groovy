package org.springframework.cloud.contract.verifier.builder.imports

import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * @author Olga Maciaszek-Sharma
 */
class MessagingImportProvider implements TestTypeSpecificImportProvider {

	@Override
	List<String> getImports(TestFramework testFramework, TestMode testMode) {
		throw new UnsupportedOperationException("Please, implement me.")
	}

	@Override
	List<String> getStaticImports(TestFramework testFramework, TestMode testMode) {
		throw new UnsupportedOperationException("Please, implement me.")
	}
}
