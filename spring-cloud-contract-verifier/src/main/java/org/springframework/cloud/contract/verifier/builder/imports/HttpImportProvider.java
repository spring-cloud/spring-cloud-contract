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
import static org.springframework.cloud.contract.verifier.config.TestMode.EXPLICIT;
import static org.springframework.cloud.contract.verifier.config.TestMode.JAXRSCLIENT;
import static org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC;
import static org.springframework.cloud.contract.verifier.config.TestMode.WEBTESTCLIENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;

/**
 * Provides imports based on test framework and test mode.
 *
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@Deprecated
public class HttpImportProvider {

	private final Map<Pair<TestFramework, TestMode>, ImportDefinitions> FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS;

	private final Map<TestMode, ImportDefinitions> TEST_MODE_SPECIFIC_IMPORTS;

	public HttpImportProvider(String restAssuredPackage) {
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS = new HashMap<>(20);
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT, JAXRSCLIENT),
				new ImportDefinitions(singletonList("javax.ws.rs.core.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT5, JAXRSCLIENT),
				new ImportDefinitions(singletonList("javax.ws.rs.core.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(TESTNG, JAXRSCLIENT),
				new ImportDefinitions(singletonList("javax.ws.rs.core.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT, MOCKMVC),
				new ImportDefinitions(
						Arrays.asList(restAssuredPackage + ".module.mockmvc.specification.MockMvcRequestSpecification",
								restAssuredPackage + ".response.ResponseOptions")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT, WEBTESTCLIENT),
				new ImportDefinitions(Arrays.asList(
						"io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
						"io.restassured.module.webtestclient.response.WebTestClientResponse")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT5, MOCKMVC),
				new ImportDefinitions(
						Arrays.asList(restAssuredPackage + ".module.mockmvc.specification.MockMvcRequestSpecification",
								restAssuredPackage + ".response.ResponseOptions")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT5, WEBTESTCLIENT),
				new ImportDefinitions(Arrays.asList(
						"io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
						"io.restassured.module.webtestclient.response.WebTestClientResponse")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(TESTNG, MOCKMVC),
				new ImportDefinitions(
						Arrays.asList(restAssuredPackage + ".module.mockmvc.specification.MockMvcRequestSpecification",
								restAssuredPackage + ".response.ResponseOptions")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(TESTNG, WEBTESTCLIENT),
				new ImportDefinitions(Arrays.asList(
						"io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
						"io.restassured.module.webtestclient.response.WebTestClientResponse")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT, EXPLICIT),
				new ImportDefinitions(Arrays.asList(restAssuredPackage + ".specification.RequestSpecification",
						restAssuredPackage + ".response.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(JUNIT5, EXPLICIT),
				new ImportDefinitions(Arrays.asList(restAssuredPackage + ".specification.RequestSpecification",
						restAssuredPackage + ".response.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(TESTNG, EXPLICIT),
				new ImportDefinitions(Arrays.asList(restAssuredPackage + ".specification.RequestSpecification",
						restAssuredPackage + ".response.Response")));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(SPOCK, JAXRSCLIENT), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(CUSTOM, JAXRSCLIENT), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(SPOCK, MOCKMVC), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(CUSTOM, MOCKMVC), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(SPOCK, EXPLICIT), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(CUSTOM, EXPLICIT), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(SPOCK, WEBTESTCLIENT), new ImportDefinitions(emptyList()));
		FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.put(Pair.of(CUSTOM, WEBTESTCLIENT),
				new ImportDefinitions(emptyList()));

		TEST_MODE_SPECIFIC_IMPORTS = new HashMap<>(4);
		TEST_MODE_SPECIFIC_IMPORTS.put(JAXRSCLIENT,
				new ImportDefinitions(emptyList(), singletonList("javax.ws.rs.client.Entity.*")));
		TEST_MODE_SPECIFIC_IMPORTS.put(MOCKMVC, new ImportDefinitions(emptyList(),
				singletonList(restAssuredPackage + ".module.mockmvc.RestAssuredMockMvc.*")));
		TEST_MODE_SPECIFIC_IMPORTS.put(EXPLICIT,
				new ImportDefinitions(emptyList(), singletonList(restAssuredPackage + ".RestAssured.*")));
		TEST_MODE_SPECIFIC_IMPORTS.put(WEBTESTCLIENT, new ImportDefinitions(emptyList(),
				singletonList("io.restassured.module.webtestclient.RestAssuredWebTestClient.*")));
	}

	/**
	 * Returns list of imports for http test contracts for provided test framework and
	 * test mode.
	 * @param testFramework
	 * @param testMode
	 * @return list of imports
	 */
	public List<String> getImports(TestFramework testFramework, TestMode testMode) {
		List<String> result = new ArrayList<>(TEST_MODE_SPECIFIC_IMPORTS.get(testMode).getImports());
		result.addAll(FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(Pair.of(testFramework, testMode)).getImports());
		return result;
	}

	/**
	 * Returns list of static imports for http test contracts for provided test framework
	 * and test mode.
	 * @param testFramework
	 * @param testMode
	 * @return list of static imports
	 */
	public List<String> getStaticImports(TestFramework testFramework, TestMode testMode) {
		List<String> result = new ArrayList<>(TEST_MODE_SPECIFIC_IMPORTS.get(testMode).getStaticImports());
		result.addAll(
				FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(Pair.of(testFramework, testMode)).getStaticImports());
		return result;
	}

}
