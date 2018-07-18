package org.springframework.cloud.contract.verifier.builder.imports

import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

import static org.springframework.cloud.contract.verifier.config.TestFramework.CUSTOM
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.config.TestMode.EXPLICIT
import static org.springframework.cloud.contract.verifier.config.TestMode.JAXRSCLIENT
import static org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC

/**
 * Provides imports based on test framework and test mode.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
class HttpImportProvider {

	private final Map<TestMode, ImportDefinitions> TEST_MODE_SPECIFIC_IMPORTS = [
			(JAXRSCLIENT): new ImportDefinitions([], ['javax.ws.rs.client.Entity.*']),
			(MOCKMVC)    : new ImportDefinitions([], ["${restAssuredPackage}.module.mockmvc.RestAssuredMockMvc.*"]),
			(EXPLICIT)   : new ImportDefinitions([], ["${restAssuredPackage}.RestAssured.*"])]

	private final Map<Tuple2<TestFramework, TestMode>, ImportDefinitions> FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS = [
			(new Tuple2(JUNIT, JAXRSCLIENT)) : new ImportDefinitions(['javax.ws.rs.core.Response']),
			(new Tuple2(JUNIT5, JAXRSCLIENT)): new ImportDefinitions(['javax.ws.rs.core.Response']),
			(new Tuple2(JUNIT, MOCKMVC))     : new ImportDefinitions([
					"${restAssuredPackage}.module.mockmvc.specification.MockMvcRequestSpecification",
					"${restAssuredPackage}.response.ResponseOptions"]),
			(new Tuple2(JUNIT5, MOCKMVC))    : new ImportDefinitions([
					"${restAssuredPackage}.module.mockmvc.specification.MockMvcRequestSpecification",
					"${restAssuredPackage}.response.ResponseOptions"]),
			(new Tuple2(JUNIT, EXPLICIT))    : new ImportDefinitions(["${restAssuredPackage}.specification.RequestSpecification",
			                                                          "${restAssuredPackage}.response.Response"]),
			(new Tuple2(JUNIT5, EXPLICIT))   : new ImportDefinitions(["${restAssuredPackage}.specification.RequestSpecification",
			                                                          "${restAssuredPackage}.response.Response"]),
			(new Tuple2(SPOCK, JAXRSCLIENT)) : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, JAXRSCLIENT)): new ImportDefinitions([]),
			(new Tuple2(SPOCK, MOCKMVC))     : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, MOCKMVC))    : new ImportDefinitions([]),
			(new Tuple2(SPOCK, EXPLICIT))    : new ImportDefinitions([]),
			(new Tuple2(CUSTOM, EXPLICIT))   : new ImportDefinitions([])
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
