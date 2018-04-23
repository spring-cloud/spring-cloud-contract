package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.PackageScope

import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator
import org.springframework.cloud.contract.verifier.config.TestFramework

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5

/**
 * @author Olga Maciaszek-Sharma
 */
@PackageScope
class BaseImportProvider {

	private static final ImportDefinitions GENERAL_IMPORTS = new ImportDefinitions([], [
			'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat'
	])

	private static
	final Map<TestFramework, ImportDefinitions> TEST_FRAMEWORK_SPECIFIC_IMPORTS = [
			(JUNIT) : new ImportDefinitions(['org.junit.Test']),
			(JUNIT5): new ImportDefinitions(['org.junit.jupiter.api.Test'])]

	static List<String> getImports(JavaTestGenerator.TestType testType) {
		HttpImportProvider h
	}

	static List<String> getImports() {
		return GENERAL_IMPORTS.imports
	}

	static List<String> getStaticImports() {
		return GENERAL_IMPORTS.staticImports
	}

	static List<String> getImports(TestFramework testFramework) {
		return TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).imports
	}

	static List<String> getStaticImports(TestFramework testFramework) {
		return TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).staticImports
	}


}

