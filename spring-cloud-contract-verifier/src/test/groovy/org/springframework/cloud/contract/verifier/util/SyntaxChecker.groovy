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

package org.springframework.cloud.contract.verifier.util

import java.lang.reflect.Method

import javax.inject.Inject
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaCompiler
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Rule
import org.junit.Test
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
@Commons
class SyntaxChecker {

	private static final String[] DEFAULT_IMPORTS = [
			Contract.name,
			"io.restassured.response.ResponseOptions",
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
			"io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
			"io.restassured.module.webtestclient.response.WebTestClientResponse",
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
			"io.restassured.module.mockmvc.RestAssuredMockMvc.given",
			"io.restassured.module.mockmvc.RestAssuredMockMvc.when",
			"io.restassured.RestAssured.*",
			"${Entity.name}.*",
			"${ContractVerifierUtil.name}.*",
			"${ContractVerifierMessagingUtil.name}.headers",
			"${JsonAssertion.name}.assertThatJson",
			"${SpringCloudContractAssertions.name}.assertThat",
	].collect { "import static ${it};" }.join("\n")

	private static final String WEB_TEST_CLIENT_STATIC_IMPORTS = [
			"io.restassured.module.webtestclient.RestAssuredWebTestClient.*",
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
		try {
			if (builderName.toLowerCase().contains("spock")) {
				tryToCompileGroovy(builderName, test)
			} else {
				tryToCompileJava(builderName, test)
			}
		} catch (Throwable t) {
			log.error("Exception occurred while trying to compile the test [\n" + test + "\n]")
			throw t
		}
	}

	static void tryToRun(String builderName, String test) {
		try {
			if (builderName.toLowerCase().contains("spock")) {
				Script script = tryToCompileGroovy(builderName, test)
				script.invokeMethod("validate_method()", null)
			} else {
				Class clazz = tryToCompileJava(builderName, test)
				Method method = ReflectionUtils.findMethod(clazz, "validate_method")
				method.invoke(clazz.newInstance())
			}
		} catch (Throwable t) {
			log.error("Exception occurred while trying to run the test [\n" + test + "\n]")
			throw t
		}
	}

	// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
	static void tryToCompileWithoutCompileStatic(String builderName, String test) {
		if (builderName.toLowerCase().contains("spock")) {
			tryToCompileGroovy(builderName, test, false)
		} else {
			tryToCompileJava(builderName, test)
		}
	}

	static Script tryToCompileGroovy(String builderName, String test, boolean compileStatic = false) {
		def imports = new ImportCustomizer()
		CompilerConfiguration configuration = new CompilerConfiguration()
		if (compileStatic) {
			configuration.addCompilationCustomizers(
					new ASTTransformationCustomizer(CompileStatic))
		}
		configuration.addCompilationCustomizers(imports)
		String className = className(test)
		test = updatedTest(test, className)
		return new GroovyShell(SyntaxChecker.classLoader, configuration).parse(test)
	}

	private static String updatedTest(String test, String className) {
		test.replaceAll("class FooTest", "class " + className)
				.replaceAll("import javax.ws.rs.core.Response", "import javax.ws.rs.core.Response; import javax.ws.rs.client.WebTarget;")
	}

	private static GString getStaticImports(String builderName) {
		if (builderName.toLowerCase().contains('webtestclient')) {
			return "$WEB_TEST_CLIENT_STATIC_IMPORTS\n"
		}
		return "$STATIC_IMPORTS\n"
	}

	static Class tryToCompileJava(String builderName, String test) {
		String className = className(test)
		String fqnClassName = "com.example.${className}"
		test = test.replaceAll("class FooTest", "class " + className)
				.replaceAll("import javax.ws.rs.core.Response", "import javax.ws.rs.core.Response; import javax.ws.rs.client.WebTarget;")
		return compileJava(fqnClassName, test)

	}

	@CompileStatic
	private static Class<?> compileJava(String fqnClassName, String sourceCode) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>()
		InMemoryClassLoader classLoader = new InMemoryClassLoader()
		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null)
		JavaFileManager fileManager = new InMemoryFileManager(standardFileManager, classLoader)
		JavaFileObject javaFile = new InMemorySourceFile(fqnClassName, sourceCode)
		JavaCompiler.CompilationTask task = compiler.getTask(
				new StringWriter(),
				fileManager,
				diagnostics,
				null,
				null,
				Collections.singletonList(javaFile)
		)

		boolean success = task.call()
		if (!success) {
			StringBuilder errorMsg = new StringBuilder("Compilation failed:")
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				errorMsg.append("\nLine ").append(diagnostic.getLineNumber())
						.append(": ").append(diagnostic.getMessage(null))
			}
			throw new IllegalStateException(errorMsg.toString())
		}

		try {
			return classLoader.loadClass(fqnClassName)
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to load compiled class", e)
		}
	}

	private static String className(String test) {
		Random random = new Random()
		int first = Math.abs(random.nextInt())
		int hashCode = Math.abs(test.hashCode())
		String className = "TestClass_${first}_${hashCode}"
		return className
	}

	static boolean tryToCompileJavaWithoutImports(String fqn, String test) {
		compileJava(fqn, test)
		return true
	}

	static boolean tryToCompileGroovyWithoutImports(String test) {
		try {
			new GroovyShell(SyntaxChecker.classLoader).parse(test)
		} catch (Throwable t) {
			log.error("Exception occurred while trying to parse [\n${test}\n]", t)
			throw t
		}
		return true
	}

	@CompileStatic
	private static class InMemorySourceFile extends SimpleJavaFileObject {
		private final String sourceCode

		InMemorySourceFile(String className, String sourceCode) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
					Kind.SOURCE)
			this.sourceCode = sourceCode
		}

		@Override
		CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return sourceCode
		}
	}

	@CompileStatic
	private static class InMemoryClassLoader extends ClassLoader {
		private final Map<String, byte[]> classData = new HashMap<>()

		void addClass(String name, byte[] data) {
			classData.put(name, data)
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] data = classData.get(name)
			if (data == null) {
				throw new ClassNotFoundException(name)
			}
			return defineClass(name, data, 0, data.length)
		}
	}

	@CompileStatic
	private static class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
		private final InMemoryClassLoader classLoader

		InMemoryFileManager(StandardJavaFileManager fileManager, InMemoryClassLoader classLoader) {
			super(fileManager)
			this.classLoader = classLoader
		}

		@Override
		JavaFileObject getJavaFileForOutput(Location location,
											String className,
											JavaFileObject.Kind kind,
											FileObject sibling) {
			return new InMemoryClassFile(className, classLoader)
		}
	}

	@CompileStatic
	private static class InMemoryClassFile extends SimpleJavaFileObject {
		private final String className
		private final InMemoryClassLoader classLoader
		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		InMemoryClassFile(String className, InMemoryClassLoader classLoader) {
			super(URI.create("byte:///" + className.replace('.', '/') + Kind.CLASS.extension),
					Kind.CLASS)
			this.className = className
			this.classLoader = classLoader
		}

		@Override
		OutputStream openOutputStream() {
			outputStream.reset()
			return new FilterOutputStream(outputStream) {
				@Override
				void close() throws IOException {
					super.close()
					classLoader.addClass(className, outputStream.toByteArray())
				}
			}
		}
	}
}
