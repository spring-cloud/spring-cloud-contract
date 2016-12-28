package org.springframework.cloud.contract.verifier.util

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.mdkt.compiler.InMemoryJavaCompiler

/**
 * checking the syntax of produced scripts
 */
@CompileStatic
class SyntaxChecker {

	private static final String[] DEFAULT_IMPORTS = [
			"org.springframework.cloud.contract.spec.Contract",
			"com.jayway.restassured.response.ResponseOptions",
			"com.jayway.restassured.module.mockmvc.specification.*",
			"com.jayway.restassured.module.mockmvc.*",
			"org.junit.Test",
			"org.junit.Rule",
			"com.jayway.jsonpath.DocumentContext",
			"com.jayway.jsonpath.JsonPath",
			"javax.inject.Inject",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage",
			"org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging",
			"javax.ws.rs.client.WebTarget",
			"javax.ws.rs.core.Response"
	]

	private static final String DEFAULT_IMPORTS_AS_STRING = DEFAULT_IMPORTS.collect {
		"import ${it};"
	}.join("\n")

	private static final String STATIC_IMPORTS = [
			"com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given",
			"com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.when",
			"com.jayway.restassured.RestAssured.*",
			"javax.ws.rs.client.Entity.*",
			"org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers",
			"com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson",
			"org.assertj.core.api.Assertions.assertThat"
	].collect { "import static ${it};"}.join("\n")


	static void tryToCompile(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(test)
		} else {
			tryToCompileJava(test)
		}
	}

	static void tryToCompileGroovy(String test) {
		def imports = new ImportCustomizer()
		CompilerConfiguration configuration = new CompilerConfiguration()
		configuration.addCompilationCustomizers(imports)
		StringBuffer sourceCode = new StringBuffer()
		sourceCode.append("${DEFAULT_IMPORTS_AS_STRING}\n")
		sourceCode.append("${STATIC_IMPORTS}\n")
		sourceCode.append("\n")
		sourceCode.append("WebTarget webTarget")
		sourceCode.append("\n")
		sourceCode.append(test)
		new GroovyShell(SyntaxChecker.classLoader, configuration).parse(sourceCode.toString())
	}

	static Class tryToCompileJava(String test) {
		Random random = new Random()
		int first = Math.abs(random.nextInt())
		int hashCode = Math.abs(test.hashCode())
		StringBuffer sourceCode = new StringBuffer()
		String className = "TestClass_${first}_${hashCode}"
		String fqnClassName = "com.example.${className}"
		sourceCode.append("package com.example;\n")
		sourceCode.append("${DEFAULT_IMPORTS_AS_STRING}\n")
		sourceCode.append("${STATIC_IMPORTS}\n")
		sourceCode.append("\n")
		sourceCode.append("class ${className} {\n")
		sourceCode.append("\n")
		sourceCode.append("   WebTarget webTarget;")
		sourceCode.append("\n")
		sourceCode.append("   void method() {\n")
		sourceCode.append("   ${test}\n")
		sourceCode.append("   }\n")
		sourceCode.append("}")
		return InMemoryJavaCompiler.compile(fqnClassName, sourceCode.toString())
	}

	static boolean tryToCompileJavaWithoutImports(String fqn, String test) {
		InMemoryJavaCompiler.compile(fqn, test)
		return true
	}

	static boolean tryToCompileGroovyWithoutImports(String test) {
		new GroovyShell(SyntaxChecker.classLoader).parse(test)
		return true
	}

}
