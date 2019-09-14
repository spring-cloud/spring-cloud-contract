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

package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.CompileStatic

import org.springframework.cloud.contract.verifier.config.TestFramework

import static org.springframework.cloud.contract.verifier.config.TestFramework.CUSTOM
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.config.TestFramework.TESTNG

/**
 * Provides imports based on test framework.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 * @deprecated
 */
@CompileStatic
@Deprecated
class BaseImportProvider {

	private static final ImportDefinitions GENERAL_IMPORTS = new ImportDefinitions([], [
			'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat',
			'org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*'
	])

	private static
	final Map<TestFramework, ImportDefinitions> TEST_FRAMEWORK_SPECIFIC_IMPORTS = [
			(JUNIT) : new ImportDefinitions(['org.junit.Test']),
			(JUNIT5): new ImportDefinitions(['org.junit.jupiter.api.Test']),
			(SPOCK) : new ImportDefinitions([]),
			(TESTNG): new ImportDefinitions(['org.testng.annotations.Test']),
			(CUSTOM): new ImportDefinitions([])]

	private static final Map<TestFramework, String> RULE_IMPORT = [
			(JUNIT) : 'org.junit.Rule',
			(JUNIT5): 'org.junit.jupiter.api.extension.ExtendWith',
			(SPOCK) : 'org.junit.Rule',
			(TESTNG): 'org.junit.Rule',
			(CUSTOM): 'org.junit.Rule'
	]

	/**
	 * Returns list of imports for provided test framework.
	 * @param testFramework
	 * @return list of imports
	 */
	static List<String> getImports(TestFramework testFramework) {
		return GENERAL_IMPORTS.imports +
				TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).imports
	}

	/**
	 * @param testFramework test framework to pick the static imports for
	 * @return list of static imports for provided test framework.
	 */
	static List<String> getStaticImports(TestFramework testFramework) {
		return GENERAL_IMPORTS.staticImports +
				TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).staticImports
	}

	static String getRuleImport(TestFramework testFramework) {
		return RULE_IMPORT.get(testFramework)
	}
}

