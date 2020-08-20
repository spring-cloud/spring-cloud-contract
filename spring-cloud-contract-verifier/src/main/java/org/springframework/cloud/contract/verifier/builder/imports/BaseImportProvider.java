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

package org.springframework.cloud.contract.verifier.builder.imports;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.springframework.cloud.contract.verifier.config.TestFramework.CUSTOM;
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT;
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5;
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK;
import static org.springframework.cloud.contract.verifier.config.TestFramework.TESTNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.verifier.config.TestFramework;

/**
 * Provides imports based on test framework.
 *
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 * @deprecated
 */
@Deprecated
public class BaseImportProvider {

	private static final ImportDefinitions GENERAL_IMPORTS = new ImportDefinitions(
			emptyList(),
			Arrays.asList(
					"org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat",
					"org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*"));
	private static final Map<TestFramework, String> RULE_IMPORT;
	private static final Map<TestFramework, ImportDefinitions> TEST_FRAMEWORK_SPECIFIC_IMPORTS;

	static {
		TEST_FRAMEWORK_SPECIFIC_IMPORTS = new HashMap<>(5);
		TEST_FRAMEWORK_SPECIFIC_IMPORTS.put(JUNIT,
				new ImportDefinitions(singletonList("org.junit.Test")));
		TEST_FRAMEWORK_SPECIFIC_IMPORTS.put(JUNIT5,
				new ImportDefinitions(singletonList("org.junit.jupiter.api.Test")));
		TEST_FRAMEWORK_SPECIFIC_IMPORTS.put(SPOCK, new ImportDefinitions(emptyList()));
		TEST_FRAMEWORK_SPECIFIC_IMPORTS.put(TESTNG,
				new ImportDefinitions(singletonList("org.testng.annotations.Test")));
		TEST_FRAMEWORK_SPECIFIC_IMPORTS.put(CUSTOM, new ImportDefinitions(emptyList()));

		RULE_IMPORT = new HashMap<>(5);
		RULE_IMPORT.put(JUNIT, "org.junit.Rule");
		RULE_IMPORT.put(JUNIT5, "org.junit.jupiter.api.extension.ExtendWith");
		RULE_IMPORT.put(SPOCK, "org.junit.Rule");
		RULE_IMPORT.put(TESTNG, "org.junit.Rule");
		RULE_IMPORT.put(CUSTOM, "org.junit.Rule");
	}

	/**
	 * Returns list of imports for provided test framework.
	 * @param testFramework
	 * @return list of imports
	 */
	public static List<String> getImports(TestFramework testFramework) {
		List<String> result = new ArrayList<>(GENERAL_IMPORTS.getImports());
		result.addAll(TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).getImports());
		return result;
	}

	/**
	 * @param testFramework test framework to pick the static imports for
	 * @return list of static imports for provided test framework.
	 */
	public static List<String> getStaticImports(TestFramework testFramework) {
		List<String> result = new ArrayList<>(GENERAL_IMPORTS.getStaticImports());
		result.addAll(
				TEST_FRAMEWORK_SPECIFIC_IMPORTS.get(testFramework).getStaticImports());
		return result;
	}

	public static String getRuleImport(TestFramework testFramework) {
		return RULE_IMPORT.get(testFramework);
	}

}
