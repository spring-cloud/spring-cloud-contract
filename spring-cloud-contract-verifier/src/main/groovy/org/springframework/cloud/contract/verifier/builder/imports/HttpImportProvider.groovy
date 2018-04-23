package org.springframework.cloud.contract.verifier.builder.imports

import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestMode.EXPLICIT
import static org.springframework.cloud.contract.verifier.config.TestMode.JAXRSCLIENT
import static org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC

/**
 * @author Olga Maciaszek-Sharma
 */
class HttpImportProvider implements TestTypeSpecificImportProvider {

	private static final Map<TestMode, ImportDefinitions> TEST_MODE_SPECIFIC_IMPORTS = [
			(JAXRSCLIENT): new ImportDefinitions([], ['javax.ws.rs.client.Entity.*']),
			(MOCKMVC)    : new ImportDefinitions([], ["${restAssuredPackage}.module.mockmvc.RestAssuredMockMvc.*"]),
			(EXPLICIT)   : new ImportDefinitions([], ["${restAssuredPackage}.RestAssured.*"])]
	private static
	final Map<Tuple2<TestFramework, TestMode>, ImportDefinitions> FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS = [
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
			                                                          "${restAssuredPackage}.response.Response"])
	]

	private final String restAssuredPackage

	HttpImportProvider(String restAssuredPackage) {
		this.restAssuredPackage = restAssuredPackage
	}

	static List<String> getImports(TestMode testMode) {
		return TEST_MODE_SPECIFIC_IMPORTS.get(testMode).imports
	}

	static List<String> getStaticImports(TestMode testMode) {
		return TEST_MODE_SPECIFIC_IMPORTS.get(testMode).staticImports
	}

	static List<String> getImports(TestFramework testFramework, TestMode testMode) {
		return FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(new Tuple2(testFramework, testMode)).imports
	}

	static List<String> getStaticImports(TestFramework testFramework, TestMode testMode) {
		return FRAMEWORK_AND_TEST_MODE_SPECIFIC_IMPORTS.get(new Tuple2(testFramework, testMode)).staticImports
	}

}
