package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.CompileStatic

import org.springframework.cloud.contract.verifier.config.TestFramework

import static org.springframework.cloud.contract.verifier.config.TestFramework.CUSTOM
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK

/**
 * Provides imports based on test framework.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class BaseImportProvider {

	private static final ImportDefinitions GENERAL_IMPORTS = new ImportDefinitions([], [
			'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat'
	])

	private static
	final Map<TestFramework, ImportDefinitions> TEST_FRAMEWORK_SPECIFIC_IMPORTS = [
			(JUNIT) : new ImportDefinitions(['org.junit.Test']),
			(JUNIT5): new ImportDefinitions(['org.junit.jupiter.api.Test']),
			(SPOCK) : new ImportDefinitions([]),
			(CUSTOM): new ImportDefinitions([])]

	private static final Map<TestFramework, String> RULE_IMPORT = [
			(JUNIT) : 'org.junit.Rule',
			(JUNIT5): 'org.junit.jupiter.api.extension.ExtendWith',
			(SPOCK) : 'org.junit.Rule',
			(CUSTOM): 'org.junit.Rule'
	]

	/**
	 * Returns list of imports for provided test framework.
	 * @param testFramework
	 * @return list of imports
	 */
	static List<String> getImports(TestFramework testFramework) {
		return GENERAL_IMPORTS.imports + TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).imports
	}

	/**
	 * Returns list of static imports for provided test framework.
	 * @param testFramework
	 * @return
	 */
	static List<String> getStaticImports(TestFramework testFramework) {
		return GENERAL_IMPORTS.staticImports + TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).staticImports
	}

	static String getRuleImport(TestFramework testFramework) {
		return RULE_IMPORT.get(testFramework)
	}
}

