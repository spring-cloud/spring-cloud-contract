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

package org.springframework.cloud.contract.verifier.builder


import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.verifier.TestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.config.TestFramework.TESTNG
import static org.springframework.cloud.contract.verifier.config.TestMode.EXPLICIT
import static org.springframework.cloud.contract.verifier.config.TestMode.JAXRSCLIENT
import static org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC
import static org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter.convertAsCollection

class SingleTestGeneratorSpec extends Specification {

	@Rule
	TemporaryFolder tmpFolder = new TemporaryFolder()
	File file
	File tmp

	private static final List<String> mockMvcJUnitRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																			  'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
																			  'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;',
																			  '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																			  'import io.restassured.response.ResponseOptions;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> explicitJUnitRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																			   'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.RestAssured.*;',
																			   '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import io.restassured.specification.RequestSpecification;',
																			   'import io.restassured.response.Response;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static
	final List<String> mockMvcJUnit5RestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																'import org.junit.jupiter.api.Disabled;', 'import org.junit.jupiter.api.Test;',
																'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;',
																'@Test', '@Disabled', 'import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																'import io.restassured.response.ResponseOptions;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat',
																'import org.junit.jupiter.api.TestMethodOrder', 'import org.junit.jupiter.api.MethodOrderer', '@TestMethodOrder(MethodOrderer.Alphanumeric.class)']

	private static
	final List<String> explicitJUnit5RestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																 'import org.junit.jupiter.api.Disabled;', 'import org.junit.jupiter.api.Test;',
																 'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.RestAssured.*;',
																 '@Test', '@Disabled', 'import io.restassured.specification.RequestSpecification;',
																 'import io.restassured.response.Response;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat',
																 'org.junit.jupiter.api.TestMethodOrder', 'org.junit.jupiter.api.MethodOrderer', '@TestMethodOrder(MethodOrderer.Alphanumeric.class)']

	private static
	final List<String> mockMvcTestNGRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																'import org.testng.annotations.Test;',
																'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;',
																'@Test', 'import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																'import io.restassured.response.ResponseOptions;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static
	final List<String> explicitTestNGRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																 'import org.testng.annotations.Test;',
																 'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.RestAssured.*;',
																 '@Test', 'import io.restassured.specification.RequestSpecification;',
																 'import io.restassured.response.Response;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> spockClassRestAssured3Strings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																	   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																	   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*',
																	   '@Stepwise', '@Ignore', 'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> explicitSpockRestAssured2ClassStrings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																			   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static com.jayway.restassured.RestAssured.*',
																			   '@Stepwise', '@Ignore', 'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> explicitSpockRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																			   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static io.restassured.RestAssured.*',
																			   '@Stepwise', '@Ignore', 'org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	public static final Closure JAVA_ASSERTER = { String classToTest ->
		String name = Math.abs(new Random().nextInt())
		String changedTest = classToTest
				.replace('public class Test', "public class Test${name}")
				.replace('public class ContractsTest', "public class Test${name}")
		String fqn = FQN(classToTest)
		SyntaxChecker.tryToCompileJavaWithoutImports("${fqn}${name}", changedTest)
	}

	static String FQN(String classToTest) {
		return classToTest.contains('0_1_0_dev_1_uncommitted_d1174dd') ?
				'org.springframework.cloud.contract.verifier.tests.com_uscm.dale_api44_spec._0_1_0_dev_1_uncommitted_d1174dd.Test' :
				'test.Test'
	}

	public static final Closure JAVA_JAXRS_ASSERTER = { String classToTest ->
		String name = Math.abs(new Random().nextInt())
		String changedTest = classToTest
				.replace('public class Test {', "public class Test${name} {\njavax.ws.rs.client.WebTarget webTarget;\n")
				.replace('public class ContractsTest {', "public class Test${name} {\njavax.ws.rs.client.WebTarget webTarget;\n")
		String fqn = FQN(classToTest)
		SyntaxChecker.tryToCompileJavaWithoutImports("${fqn}${name}", changedTest)
	}

	public static final Closure GROOVY_ASSERTER = { String classToTest ->
		SyntaxChecker.tryToCompileGroovyWithoutImports(classToTest)
	}

	def setup() {
		file = tmpFolder.newFile()
		writeContract(file)
		tmp = tmpFolder.newFolder()
		File classpath = new File(SingleTestGeneratorSpec.class.getResource('/classpath/').toURI())
		FileSystemUtils.copyRecursively(classpath, tmp)
	}

	private static writeContract(File file) {
		file.write('''
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'PUT'
						url 'url'
					}
					response {
						status OK()
						body(["foo" : "bar"])
					}
				}
''')
	}

	def 'should build test class for #testFramework and mode #mode'() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			properties.testMode = mode
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2,
					convertAsCollection(new File('/'), file))
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))

		then:
			classStrings.each { assert clazz.contains(it) }
		and:
			asserter(clazz)
		where:
			testFramework | mode     | classStrings                           | asserter
			JUNIT         | MOCKMVC  | mockMvcJUnitRestAssured3ClassStrings   | JAVA_ASSERTER
			JUNIT         | EXPLICIT | explicitJUnitRestAssured3ClassStrings  | JAVA_ASSERTER
			JUNIT5        | MOCKMVC  | mockMvcJUnit5RestAssured3ClassStrings  | JAVA_ASSERTER
			JUNIT5        | EXPLICIT | explicitJUnit5RestAssured3ClassStrings | JAVA_ASSERTER
			TESTNG        | MOCKMVC  | mockMvcTestNGRestAssured3ClassStrings  | JAVA_ASSERTER
			TESTNG        | EXPLICIT | explicitTestNGRestAssured3ClassStrings | JAVA_ASSERTER
			SPOCK         | MOCKMVC  | spockClassRestAssured3Strings          | GROOVY_ASSERTER
			SPOCK         | EXPLICIT | explicitSpockRestAssured3ClassStrings  | GROOVY_ASSERTER
	}

	def 'should build test class for #testFramework when the path contains bizarre signs'() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			properties.basePackageForTests = 'org.springframework.cloud.contract.verifier.tests'
		and:
			File newFolder = tmpFolder.newFolder('META_INF')
			File subfolders = new File(newFolder, '/com.uscm/dale_api44_spec/0.1.0_dev.1.uncommitted+d1174dd/contracts/')
			subfolders.mkdirs()
			File newFile = new File(subfolders, 'contract.groovy')
			newFile.createNewFile()
			writeContract(newFile)
			properties.contractsDslDir = newFolder
			properties.generatedTestSourcesDir = newFolder.parentFile
			properties.generatedTestResourcesDir = newFolder.parentFile
		when:
			int size = new TestGenerator(properties).generate()
		then:
			size > 0
			asserter(new File(newFolder.parent, '/org/springframework/cloud/contract/verifier/tests/com_uscm/dale_api44_spec/_0_1_0_dev_1_uncommitted_d1174dd/' + testName).text)
		where:
			testFramework | mode     | asserter        | testName
			JUNIT         | MOCKMVC  | JAVA_ASSERTER   | 'ContractsTest.java'
			JUNIT         | EXPLICIT | JAVA_ASSERTER   | 'ContractsTest.java'
			JUNIT5        | MOCKMVC  | JAVA_ASSERTER   | 'ContractsTest.java'
			JUNIT5        | EXPLICIT | JAVA_ASSERTER   | 'ContractsTest.java'
			TESTNG        | MOCKMVC  | JAVA_ASSERTER   | 'ContractsTest.java'
			TESTNG        | EXPLICIT | JAVA_ASSERTER   | 'ContractsTest.java'
			SPOCK         | MOCKMVC  | GROOVY_ASSERTER | 'ContractsSpec.groovy'
			SPOCK         | EXPLICIT | GROOVY_ASSERTER | 'ContractsSpec.groovy'
	}

	def 'should build test class for #testFramework and mode #mode with two files'() {
		given:
			File file = tmpFolder.newFile()
			file.write('''
					org.springframework.cloud.contract.spec.Contract.make {
						request {
							method 'PUT'
							url 'url1'
							headers {
								contentType(applicationJson())
							}
						}
						response {
							status OK()
							body(foo:"foo", bar:"bar")
							headers {
								contentType(applicationJson())
							}
						}
					}
	''')
		and:
			File file2 = tmpFolder.newFile()
			file2.write('''
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'PUT'
						url 'url2'
						headers {
							contentType(applicationJson())
						}
					}
					response {
						status OK()
						body(foo:"foo", bar:"bar")
						headers {
							contentType(applicationJson())
						}
					}
				}
''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), false, 1, null,
					convertAsCollection(new File('/'), file))
			contract.ignored >> false
		and:
			ContractMetadata contract2 = new ContractMetadata(file2.toPath(), false, 1, null,
					convertAsCollection(new File('/'), file2))
			contract2.ignored >> false
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract, contract2], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))

		then:
			classStrings.each { clazz.contains(it) }
		and:
			asserter(clazz)
		and:
			textAssertion(clazz)
		where:
			testFramework | mode     | classStrings                           | asserter        | textAssertion
			JUNIT         | MOCKMVC  | mockMvcJUnitRestAssured3ClassStrings   | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			JUNIT         | EXPLICIT | explicitJUnitRestAssured3ClassStrings  | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			JUNIT5        | MOCKMVC  | mockMvcJUnit5RestAssured3ClassStrings  | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			JUNIT5        | EXPLICIT | explicitJUnit5RestAssured3ClassStrings | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			TESTNG        | MOCKMVC  | mockMvcTestNGRestAssured3ClassStrings  | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			TESTNG        | EXPLICIT | explicitTestNGRestAssured3ClassStrings | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			SPOCK         | MOCKMVC  | spockClassRestAssured3Strings          | GROOVY_ASSERTER | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification') == 2 }
			SPOCK         | EXPLICIT | explicitSpockRestAssured2ClassStrings  | GROOVY_ASSERTER | { String test -> StringUtils.countOccurrencesOf(test, '\t\t\tMockMvcRequestSpecification request') == 2 }
	}

	def 'should build JaxRs test class for #testFramework'() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testMode = JAXRSCLIENT
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, null, convertAsCollection(new File('/'), file))
			contract.ignored >> true
			SingleTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))

		then:
			classStrings.each { clazz.contains(it) }

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                                                                      | asserter
			JUNIT         | ['import static javax.ws.rs.client.Entity.*', 'import javax.ws.rs.core.Response'] | JAVA_JAXRS_ASSERTER
			JUNIT5        | ['import static javax.ws.rs.client.Entity.*', 'import javax.ws.rs.core.Response'] | JAVA_JAXRS_ASSERTER
			TESTNG        | ['import static javax.ws.rs.client.Entity.*', 'import javax.ws.rs.core.Response'] | JAVA_JAXRS_ASSERTER
			SPOCK         | ['import static javax.ws.rs.client.Entity.*']                                     | GROOVY_ASSERTER
	}

	def 'should work if there is messaging and rest in one folder #testFramework'() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
						org.springframework.cloud.contract.spec.Contract.make {
						  ignored()
						  label 'some_label'
						  input {
							messageFrom('delete')
							messageBody([
								bookName: 'foo'
							])
							messageHeaders {
							  header('sample', 'header')
							}
							assertThat('hashCode()')
						  }
						}
		''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2,
					convertAsCollection(new File('/'), file))
			contract.ignored >> true
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.
					toPath(), true, 1, 2, convertAsCollection(new File('/'), secondFile))
			contract2.ignored >> true
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract, contract2], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Inject ContractVerifierMessaging')

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                          | asserter
			JUNIT         | mockMvcJUnitRestAssured3ClassStrings  | JAVA_ASSERTER
			JUNIT5        | mockMvcJUnit5RestAssured3ClassStrings | JAVA_ASSERTER
			TESTNG        | mockMvcTestNGRestAssured3ClassStrings | JAVA_ASSERTER
			SPOCK         | spockClassRestAssured3Strings         | GROOVY_ASSERTER
	}

	@Issue('#30')
	def 'should ignore a test if the contract is ignored in the dsl'() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
						org.springframework.cloud.contract.spec.Contract.make {
							ignored()
							request {
								method 'PUT'
								url 'url'
							}
							response {
								status OK()
							}
						}
		''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.
					toPath(), true, 1, 2, convertAsCollection(new File('/'), file))
			contract2.ignored >> false
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract2], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains(ignoreAnnotation)

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                          | ignoreAnnotation         | asserter
			JUNIT         | mockMvcJUnitRestAssured3ClassStrings  | '@Ignore'                | JAVA_ASSERTER
			JUNIT5        | mockMvcJUnit5RestAssured3ClassStrings | '@Disabled'              | JAVA_ASSERTER
			TESTNG        | mockMvcTestNGRestAssured3ClassStrings | '@Test(enabled = false)' | JAVA_ASSERTER
			SPOCK         | spockClassRestAssured3Strings         | '@Ignore'                | GROOVY_ASSERTER
	}

	def 'should not allow the usage of ignore annotations for TestNG '() {
		given:
			TestFramework testNG = TESTNG

		when:
			testNG.getIgnoreClass()
		then:
			thrown UnsupportedOperationException

		when:
			testNG.getIgnoreAnnotation()
		then:
			thrown UnsupportedOperationException
	}

	@Issue('#117')
	def 'should generate test in explicit test mode using JUnit'() {
		given:
			String baseClass = '''
			// tag::context_path_baseclass[]
			import io.restassured.RestAssured;
			import org.junit.Before;
			import org.springframework.boot.web.server.LocalServerPort;
			import org.springframework.boot.test.context.SpringBootTest;
			
			@SpringBootTest(classes = ContextPathTestingBaseClass.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
			class ContextPathTestingBaseClass {
				
				@LocalServerPort int port;
				
				@Before
				public void setup() {
					RestAssured.baseURI = "http://localhost";
					RestAssured.port = this.port;
				}
			}
			// end::context_path_baseclass[]
			'''
			SyntaxChecker.tryToCompileJavaWithoutImports('test.ContextPathTestingBaseClass', "package test;\n${baseClass}")
		and:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
						// tag::context_path_contract[]
						org.springframework.cloud.contract.spec.Contract.make {
							request {
								method 'GET'
								url '/my-context-path/url'
							}
							response {
								status OK()
							}
						}
						// end::context_path_contract[]
		''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = JUNIT
			properties.testMode = EXPLICIT
			properties.baseClassForTests = 'test.ContextPathTestingBaseClass'
		and:
			ContractMetadata contract = new ContractMetadata(file.toPath(), false, 1,
					null, convertAsCollection(new File('/'), file))
		and:
			SingleTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))
		then:
			clazz.contains('RequestSpecification request = given();')
			clazz.contains('Response response = given().spec(request)')
	}

	def "should pick the contract's name as the test method for #testFramework"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
							org.springframework.cloud.contract.spec.Contract.make {
								name("MySuperMethod")
								request {
									method 'PUT'
									url 'url'
								}
								response {
									status OK()
								}
							}
			''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File('/'), secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))
		then:
			clazz.contains('validate_mySuperMethod()')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	def "should pick the contract's name as the test method when there are multiple contracts for #testFramework"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
							(1..2).collect { int index ->
	org.springframework.cloud.contract.spec.Contract.make {
		name("shouldHaveIndex${index}")
		request {
			method(PUT())
			headers {
				contentType(applicationJson())
			}
			url "/${index}"
		}
		response {
			status OK()
		}
	}
}''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File('/'), secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))
		then:
			clazz.contains('validate_shouldHaveIndex1()')
			clazz.contains('validate_shouldHaveIndex2()')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	def 'should generate the test method when there are multiple contracts without name field for #testFramework'() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write('''
							(1..2).collect { int index ->
	org.springframework.cloud.contract.spec.Contract.make {
		request {
			method(PUT())
			headers {
				contentType(applicationJson())
			}
			url "/${index}"
		}
		response {
			status OK()
		}
	}
}''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File('/'), secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], 'com/foo', new SingleTestGenerator.GeneratedClassData('test', 'test', file.toPath()))
		then:
			clazz.contains('_0() throws Exception')
			clazz.contains('_1() throws Exception')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	@Issue('#359')
	def 'should generate tests from a contract that references a file for [#testFramework]'() {
		given:
			File output = new File(tmp, 'readFromFile.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					basePackageForTests: 'a.b',
					generatedTestSourcesDir: temp,
					generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFile_request_request.json')
			test.contains('RESPONSE')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	@Issue('#260')
	def 'should generate tests in a folder taken from basePackageForTests when it is set for [#testFramework]'() {
		given:
			File output = new File(tmp, 'readFromFile.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					basePackageForTests: 'a.b', generatedTestSourcesDir: temp,
					generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFile_request_request.json')
			test.contains('RESPONSE')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	@Issue('#1199')
	def 'should generate tests with body from file with custom charset [#testFramework]'() {
		given:
			file = tmpFolder.newFile()
			writeContract(file)
			tmp = tmpFolder.newFolder()
			File classpath = new File(SingleTestGeneratorSpec.class.getResource('/charset/').toURI())
			FileSystemUtils.copyRecursively(classpath, tmp)
		and:
			File output = new File(tmp, 'readFromFileWithCharset.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					basePackageForTests: 'a.b', generatedTestSourcesDir: temp,
					generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFileWithCharset_request_request.json')
			test.contains('RESPONSE')
			test.contains('body(new String(fileToBytes(this, "readFromFileWithCharset_request_request.json"), "US-ASCII"))')
		where:
			testFramework << [JUNIT, JUNIT5, SPOCK]
	}

	@Issue('#260')
	def "should generate tests in a folder taken from baseClassForTests's package when it is set for [#testFramework]"() {
		given:
			File output = new File(tmp, 'readFromFile.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					baseClassForTests: 'a.b.SomeClass', generatedTestSourcesDir: temp,
					generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFile_request_request.json')
			test.contains('RESPONSE')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	@Issue('#260')
	def 'should generate tests in a folder taken from packageWithBaseClasses when it is set for [#testFramework]'() {
		given:
			File output = new File(tmp, 'readFromFile.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					packageWithBaseClasses: 'a.b', generatedTestSourcesDir: temp,
					generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFile_request_request.json')
			test.contains('RESPONSE')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	@Issue('#260')
	def 'should generate tests in a default folder when no property was passed for [#testFramework]'() {
		given:
			File output = new File(tmp, 'readFromFile.groovy')
			File contractLocation = output
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
					testFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					generatedTestSourcesDir: temp, generatedTestResourcesDir: tmpFolder.newFolder()
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "org/springframework/cloud/contract/verifier/tests/ContractVerifier${getTestName(testFramework)}").text
			test.contains('readFromFile_request_request.json')
			test.contains('RESPONSE')
		where:
			testFramework << [JUNIT, JUNIT5, TESTNG, SPOCK]
	}

	private static String getTestName(TestFramework testFramework) {
		testFramework == SPOCK ? 'Spec.groovy' : 'Test.java'
	}
}
