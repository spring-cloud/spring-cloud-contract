package org.springframework.cloud.contract.verifier.builder.imports

import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * @author Olga Maciaszek-Sharma
 */
interface TestTypeSpecificImportProvider {

	List<String> getImports(TestFramework testFramework, TestMode testMode)

	List<String> getStaticImports(TestFramework testFramework, TestMode testMode)

}