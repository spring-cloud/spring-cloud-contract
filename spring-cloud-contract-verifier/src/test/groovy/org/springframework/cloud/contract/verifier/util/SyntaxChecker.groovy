/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util

import java.lang.reflect.Method

import javax.inject.Inject
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

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
import org.w3c.dom.Document
import org.xml.sax.InputSource

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil
import org.springframework.util.ReflectionUtils

/**
 * checking the syntax of produced scripts
 */
@CompileStatic
class SyntaxChecker {

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
			WebTestClientResponse.name,
			DocumentBuilder.name,
			DocumentBuilderFactory.name,
			Document.name,
			InputSource.name,
			StringReader.name
	]

	private static final String DEFAULT_IMPORTS_AS_STRING = DEFAULT_IMPORTS.collect {
		"import ${it};"
	}.join("\n")

	private static final String STATIC_IMPORTS = [
			"${RestAssuredMockMvc.name}.given",
			"${RestAssuredMockMvc.name}.when",
			"${RestAssured.name}.*",
			"${Entity.name}.*",
			"${ContractVerifierUtil.name}.*",
			"${ContractVerifierMessagingUtil.name}.headers",
			"${JsonAssertion.name}.assertThatJson",
			"${SpringCloudContractAssertions.name}.assertThat",
	].collect { "import static ${it};" }.join("\n")

	private static final String WEB_TEST_CLIENT_STATIC_IMPORTS = [
			"${RestAssuredWebTestClient.name}.*",
			"${Entity.name}.*",
			"${ContractVerifierUtil.name}.*",
			"${ContractVerifierMessagingUtil.name}.headers",
			"${JsonAssertion.name}.assertThatJson",
			"${SpringCloudContractAssertions.name}.assertThat"
	].collect { "import static ${it};" }.join("\n")

	private static final String dummyMethod = '''
private void test(String test) {
\t\tassertThat(test).isEqualTo("123");
\t}'''

	static void tryToCompile(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(builderName, test)
		}
		else {
			tryToCompileJava(builderName, test)
		}
	}

	static void tryToRun(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			Script script = tryToCompileGroovy(builderName, test)
			script.run()
		}
		else {
			Class clazz = tryToCompileJava(builderName, test)
			Method method = ReflectionUtils.findMethod(clazz, "method")
			method.invoke(clazz.newInstance())
		}
	}

	// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
	static void tryToCompileWithoutCompileStatic(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(builderName, test, false)
		}
		else {
			tryToCompileJava(builderName, test)
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
		sourceCode.append(dummyMethod)
		return new GroovyShell(SyntaxChecker.classLoader, configuration).parse(sourceCode.toString())
	}

	private static GString getStaticImports(String builderName) {
		if (builderName.toLowerCase().contains('webtestclient')) {
			return "$WEB_TEST_CLIENT_STATIC_IMPORTS\n"
		}
		return "$STATIC_IMPORTS\n"
	}

	static Class tryToCompileJava(String builderName, String test) {
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
		sourceCode.append("   public void method() throws Exception {\n")
		sourceCode.append("   ${test}\n")
		sourceCode.append("   }\n")
		sourceCode.append(dummyMethod)
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
