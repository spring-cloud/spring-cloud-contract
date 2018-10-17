package org.springframework.cloud.contract.verifier.util

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.transform.CompileStatic
import io.restassured.RestAssured
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import io.restassured.module.webtestclient.response.WebTestClientResponse
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification
import io.restassured.response.ResponseOptions
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Rule
import org.junit.Test
import org.mdkt.compiler.InMemoryJavaCompiler
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil
import org.springframework.util.ReflectionUtils

import javax.inject.Inject
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response
import java.lang.reflect.Method

/**
 * checking the syntax of produced scripts
 */
@CompileStatic
class SyntaxChecker {

	WebTarget webTarget
	Entity entity

	private static final String[] DEFAULT_IMPORTS = [
			Contract.name,
			ResponseOptions.name,
			'io.restassured.module.mockmvc.specification.*',
			'io.restassured.module.mockmvc.*',
			Test.name,
			Rule.name,
			DocumentContext.name,
			JsonPath.name,
			Inject.name,
			ContractVerifierObjectMapper.name,
			ContractVerifierMessage.name,
			ContractVerifierMessaging.name,
			WebTarget.name,
			Response.name,
			WebTestClientRequestSpecification.name,
			WebTestClientResponse.name
	]

	private static final String DEFAULT_IMPORTS_AS_STRING = DEFAULT_IMPORTS.collect {
		"import ${it};"
	}.join("\n")

	private static final String STATIC_IMPORTS = [
			"${RestAssuredMockMvc.name}.given",
			"${RestAssuredMockMvc.name}.when",
			"${RestAssured.name}.*",
			"${Entity.name}.*",
			"${ContractVerifierMessagingUtil.name}.headers",
			"${JsonAssertion.name}.assertThatJson",
			"${SpringCloudContractAssertions.name}.assertThat"
	].collect { "import static ${it};"}.join("\n")

	private static final String WEB_TEST_CLIENT_STATIC_IMPORTS = [
			"${RestAssuredWebTestClient.name}.*",
			"${Entity.name}.*",
			"${ContractVerifierMessagingUtil.name}.headers",
			"${JsonAssertion.name}.assertThatJson",
			"${SpringCloudContractAssertions.name}.assertThat"
	].collect { "import static ${it};" }.join("\n")

	static void tryToCompile(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(builderName, test)
		} else {
			tryToCompileJava(test, builderName)
		}
	}

	static void tryToRun(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			Script script = tryToCompileGroovy(builderName, test)
			script.run()
		} else {
			Class clazz = tryToCompileJava(test, builderName)
			Method method = ReflectionUtils.findMethod(clazz, "method")
			method.invoke(clazz.newInstance())
		}
	}

	// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
	static void tryToCompileWithoutCompileStatic(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(builderName, test, false)
		} else {
			tryToCompileJava(test, builderName)
		}
	}

	static Script tryToCompileGroovy(String builderName, String test, boolean compileStatic = true) {
		def imports = new ImportCustomizer()
		CompilerConfiguration configuration = new CompilerConfiguration()
		if (compileStatic) {
			configuration.addCompilationCustomizers(
					new ASTTransformationCustomizer(CompileStatic))
		}
		configuration.addCompilationCustomizers(imports)
		StringBuilder sourceCode = new StringBuilder()
		sourceCode.append("${DEFAULT_IMPORTS_AS_STRING}\n")
		sourceCode.append(getStaticImports(builderName))
		sourceCode.append("\n")
		sourceCode.append("WebTarget webTarget")
		sourceCode.append("\n")
		sourceCode.append(test)
		return new GroovyShell(SyntaxChecker.classLoader, configuration).parse(sourceCode.toString())
	}

	private static GString getStaticImports(String builderName) {
		if (builderName.toLowerCase().contains('webtestclient')) {
			return "$WEB_TEST_CLIENT_STATIC_IMPORTS\n"
		}
		"$STATIC_IMPORTS\n"
	}

	static Class tryToCompileJava(String test, String builderName) {
		Random random = new Random()
		int first = Math.abs(random.nextInt())
		int hashCode = Math.abs(test.hashCode())
		StringBuffer sourceCode = new StringBuffer()
		String className = "TestClass_${first}_${hashCode}"
		String fqnClassName = "com.example.${className}"
		sourceCode.append("package com.example;\n")
		sourceCode.append("${DEFAULT_IMPORTS_AS_STRING}\n")
		sourceCode.append(getStaticImports(builderName))
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
