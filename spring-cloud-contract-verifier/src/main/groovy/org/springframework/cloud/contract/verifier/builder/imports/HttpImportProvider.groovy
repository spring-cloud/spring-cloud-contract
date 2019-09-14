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

import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

import static org.springframework.cloud.contract.verifier.config.TestFramework.CUSTOM
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.config.TestFramework.TESTNG
import static org.springframework.cloud.contract.verifier.config.TestMode.EXPLICIT
import static org.springframework.cloud.contract.verifier.config.TestMode.JAXRSCLIENT
import static org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC
import static org.springframework.cloud.contract.verifier.config.TestMode.WEBTESTCLIENT

/**
 * Provides imports based on test framework and test mode.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@Deprecated
class HttpImportProvider {

	private final Map<TestMode, ImportDefinitions> TEST_MODE_SPECIFIC_IMPORTS = [
			(JAXRSCLIENT)  : new ImportDefinitions([], ['javax.ws.rs.client.Entity.*']),
			(MOCKMVC)      : new ImportDefinitions([], ["${restAssuredPackage}.module.mockmvc.RestAssuredMockMvc.*"]),
			(EXPLICIT)     : new ImportDefinitions([], ["${restAssuredPackage}.RestAssured.*"]),
			(WEBTESTCLIENT): new ImportDefinitions([], ['io.restassured.module.webtestclient.RestAssuredWebTestClient.*'])]

	private final Map<Tuple2<TestFramework, TestMode>, ImportDefinitions> FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS = [
			(new Tuple2(JUNIT, JAXRSCLIENT))   : new ImportDefinitions(['javax.ws.rs.core.Response']),
			(new Tuple2(JUNIT5, JAXRSCLIENT))  : new ImportDefinitions(['javax.ws.rs.core.Response']),
			(new Tuple2(TESTNG, JAXRSCLIENT))  : new ImportDefinitions(['javax.ws.rs.core.Response']),
			(new Tuple2(JUNIT, MOCKMVC))       : new ImportDefinitions([
					"${restAssuredPackage}.module.mockmvc.specification.MockMvcRequestSpecification",
					"${restAssuredPackage}.response.ResponseOptions"]),
			(new Tuple2(JUNIT, WEBTESTCLIENT)) : new ImportDefinitions([
					'io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification',
					'io.restassured.module.webtestclient.response.WebTestClientResponse']),
			(new Tuple2(JUNIT5, MOCKMVC))      : new ImportDefinitions([
					"${restAssuredPackage}.module.mockmvc.specification.MockMvcRequestSpecification",
					"${restAssuredPackage}.response.ResponseOptions"]),
			(new Tuple2(JUNIT5, WEBTESTCLIENT)): new ImportDefinitions([
					'io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification',
					'io.restassured.module.webtestclient.response.WebTestClientResponse']),
			(new Tuple2(TESTNG, MOCKMVC))      : new ImportDefinitions([
					"${restAssuredPackage}.module.mockmvc.specification.MockMvcRequestSpecification",
					"${restAssuredPackage}.response.ResponseOptions"]),
			(new Tuple2(TESTNG, WEBTESTCLIENT)): new ImportDefinitions([
					'io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification',
					'io.restassured.module.webtestclient.response.WebTestClientResponse']),
			(new Tuple2(JUNIT, EXPLICIT))      : new ImportDefinitions(["${restAssuredPackage}.specification.RequestSpecification",
																		"${restAssuredPackage}.response.Response"]),
			(new Tuple2(JUNIT5, EXPLICIT))     : new ImportDefinitions(["${restAssuredPackage}.specification.RequestSpecification",
																		"${restAssuredPackage}.response.Response"]),
			(new Tuple2(TESTNG, EXPLICIT))     : new ImportDefinitions(["${restAssuredPackage}.specification.RequestSpecification",
																		"${restAssuredPackage}.response.Response"]),
			(new Tuple2(SPOCK, JAXRSCLIENT))   : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, JAXRSCLIENT))  : new ImportDefinitions([]),
			(new Tuple2(SPOCK, MOCKMVC))       : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, MOCKMVC))      : new ImportDefinitions([]),
			(new Tuple2(SPOCK, EXPLICIT))      : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, EXPLICIT))     : new ImportDefinitions([]),
			(new Tuple2(SPOCK, WEBTESTCLIENT)) : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, WEBTESTCLIENT)): new ImportDefinitions([])
	]

	private final String restAssuredPackage

	HttpImportProvider(String restAssuredPackage) {
		this.restAssuredPackage = restAssuredPackage
	}

	/**
	 * Returns list of imports for http test contracts for provided test framework and test mode.
	 * @param testFramework
	 * @param testMode
	 * @return list of imports
	 */
	List<String> getImports(TestFramework testFramework, TestMode testMode) {
		return TEST_MODE_SPECIFIC_IMPORTS.get(testMode).imports +
				FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(new Tuple2(testFramework, testMode)).imports
	}

	/**
	 * Returns list of static imports for http test contracts for provided test framework and test mode.
	 * @param testFramework
	 * @param testMode
	 * @return list of static imports
	 */
	List<String> getStaticImports(TestFramework testFramework, TestMode testMode) {
		return TEST_MODE_SPECIFIC_IMPORTS.get(testMode).staticImports +
				FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(new Tuple2(testFramework, testMode)).staticImports
	}
}
