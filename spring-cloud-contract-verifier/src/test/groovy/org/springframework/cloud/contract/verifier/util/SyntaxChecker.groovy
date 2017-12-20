package org.springframework.cloud.contract.verifier.util

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.mdkt.compiler.InMemoryJavaCompiler
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Method

/**
 * checking the syntax of produced scripts
 */
@CompileStatic
class SyntaxChecker {

	private static final String[] DEFAULT_IMPORTS = [
			"org.springframework.cloud.contract.spec.Contract",
			"io.restassured.response.ResponseOptions",
			"io.restassured.module.mockmvc.specification.*",
			"io.restassured.module.mockmvc.*",
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
			"io.restassured.module.mockmvc.RestAssuredMockMvc.given",
			"io.restassured.module.mockmvc.RestAssuredMockMvc.when",
			"io.restassured.RestAssured.*",
			"javax.ws.rs.client.Entity.*",
			"org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers",
			"com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson",
			"org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat"
	].collect { "import static ${it};"}.join("\n")

	static void tryToCompile(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(test)
		} else {
			tryToCompileJava(test)
		}
	}

	static void tryToRun(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			Script script = tryToCompileGroovy(test)
			script.run()
		} else {
			Class clazz = tryToCompileJava(test)
			Method method = ReflectionUtils.findMethod(clazz, "method")
			method.invoke(clazz.newInstance())
		}
	}

	// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
	static void tryToCompileWithoutCompileStatic(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(test, false)
		} else {
			tryToCompileJava(test)
		}
	}

	static Script tryToCompileGroovy(String test, boolean compileStatic = true) {
		def imports = new ImportCustomizer()
		CompilerConfiguration configuration = new CompilerConfiguration()
		if (compileStatic) {
			configuration.addCompilationCustomizers(
					new ASTTransformationCustomizer(CompileStatic))
		}
		configuration.addCompilationCustomizers(imports)
		StringBuilder sourceCode = new StringBuilder()
		sourceCode.append("${DEFAULT_IMPORTS_AS_STRING}\n")
		sourceCode.append("${STATIC_IMPORTS}\n")
		sourceCode.append("\n")
		sourceCode.append("WebTarget webTarget")
		sourceCode.append("\n")
		sourceCode.append(test)
		return new GroovyShell(SyntaxChecker.classLoader, configuration).parse(sourceCode.toString())
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
		sourceCode.append("public class ${className} {\n")
		sourceCode.append("\n")
		sourceCode.append("   WebTarget webTarget;")
		sourceCode.append("\n")
		sourceCode.append("   public void method() {\n")
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
