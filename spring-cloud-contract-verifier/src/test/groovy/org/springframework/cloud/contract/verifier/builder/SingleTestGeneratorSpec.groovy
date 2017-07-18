/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.cloud.contract.verifier.TestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import org.springframework.util.StringUtils
import spock.lang.Issue
import spock.lang.Specification

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter.convertAsCollection

class SingleTestGeneratorSpec extends Specification {

	@Rule
	TemporaryFolder tmpFolder = new TemporaryFolder()
	File file

	private static final List<String> mockMvcJUnitRestAssured2ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																			  'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
																			  'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;',
																			  '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																			  'import com.jayway.restassured.response.ResponseOptions;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> mockMvcJUnitRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
												   'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
												   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;',
												   '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
												   'import io.restassured.response.ResponseOptions;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']


	private static final List<String> explicitJUnitRestAssured2ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																			   'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static com.jayway.restassured.RestAssured.*;',
																			   '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import com.jayway.restassured.specification.RequestSpecification;',
																			   'import com.jayway.restassured.response.Response;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> explicitJUnitRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
																			   'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static io.restassured.RestAssured.*;',
																			   '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'import io.restassured.specification.RequestSpecification;',
																			   'import io.restassured.response.Response;', 'import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat']

	private static final List<String> spockClassRestAssured2Strings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																	   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																	   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*',
																	   '@Stepwise', '@Ignore']

	private static final List<String> spockClassRestAssured3Strings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																	   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																	   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static io.restassured.module.mockmvc.RestAssuredMockMvc.*',
																	   '@Stepwise', '@Ignore']

	private static final List<String> explicitSpockRestAssured2ClassStrings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																			   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static com.jayway.restassured.RestAssured.*',
																			   '@Stepwise', '@Ignore']

	private static final List<String> explicitSpockRestAssured3ClassStrings = ['import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
																			   'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
																			   'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static io.restassured.RestAssured.*',
																			   '@Stepwise', '@Ignore']

	public static final Closure JAVA_ASSERTER = { String classToTest ->
		String name = Math.abs(new Random().nextInt())
		String changedTest = classToTest
				.replace("public class Test", "public class Test${name}")
				.replace("public class ContractsTest", "public class Test${name}")
		String fqn = FQN(classToTest)
		SyntaxChecker.tryToCompileJavaWithoutImports("${fqn}${name}", changedTest)
	}

	static String FQN(String classToTest) {
		return classToTest.contains("0_1_0_dev_1_uncommitted_d1174dd") ?
				"org.springframework.cloud.contract.verifier.tests.com_uscm.dale_api44_spec._0_1_0_dev_1_uncommitted_d1174dd.Test" :
				"test.Test"
	}

	public static final Closure JAVA_JAXRS_ASSERTER = { String classToTest ->
		String name = Math.abs(new Random().nextInt())
		String changedTest = classToTest
				.replace("public class Test {", "public class Test${name} {\njavax.ws.rs.client.WebTarget webTarget;\n")
				.replace("public class ContractsTest {", "public class Test${name} {\njavax.ws.rs.client.WebTarget webTarget;\n")
		String fqn = FQN(classToTest)
		SyntaxChecker.tryToCompileJavaWithoutImports("${fqn}${name}", changedTest)
	}

	public static final Closure GROOVY_ASSERTER = { String classToTest ->
		SyntaxChecker.tryToCompileGroovyWithoutImports(classToTest)
	}

	def setup() {
		file = tmpFolder.newFile()
		wiriteContract(file)
	}

	private wiriteContract(File file) {
		file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'PUT'
						url 'url'
					}
					response {
						status 200
					}
				}
""")
	}

	def "should build test class for #testFramework"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
			properties.testMode = mode
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(new File("/"),file))
			contract.ignored >> true
			contract.order >> 2
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')

		then:
			classStrings.each { assert clazz.contains(it) }
		and:
			asserter(clazz)
		where:
			testFramework | mode              | classStrings                          | asserter
			JUNIT         | TestMode.MOCKMVC  | mockMvcJUnitRestAssured3ClassStrings  | JAVA_ASSERTER
			JUNIT         | TestMode.EXPLICIT | explicitJUnitRestAssured3ClassStrings | JAVA_ASSERTER
			SPOCK         | TestMode.MOCKMVC  | spockClassRestAssured3Strings         | GROOVY_ASSERTER
			SPOCK         | TestMode.EXPLICIT | explicitSpockRestAssured3ClassStrings | GROOVY_ASSERTER
	}

	def "should build test class for #testFramework when the path contains bizarre signs"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
			properties.basePackageForTests = "org.springframework.cloud.contract.verifier.tests"
		and:
			File newFolder = tmpFolder.newFolder("META_INF")
			File subfolders = new File(newFolder, "/com.uscm/dale_api44_spec/0.1.0_dev.1.uncommitted+d1174dd/contracts/")
			subfolders.mkdirs()
			File newFile = new File(subfolders, "contract.groovy")
			newFile.createNewFile()
			wiriteContract(newFile)
			properties.contractsDslDir = newFolder
			properties.generatedTestSourcesDir = newFolder.parentFile
		when:
			int size = new TestGenerator(properties).generate()
		then:
			size > 0
			asserter(new File(newFolder.parent, "/org/springframework/cloud/contract/verifier/tests/com_uscm/dale_api44_spec/0_1_0_dev_1_uncommitted_d1174dd/${testName}").text)
		where:
			testFramework | mode              | asserter        | testName
			JUNIT         | TestMode.MOCKMVC  | JAVA_ASSERTER   | "ContractsTest.java"
			JUNIT         | TestMode.EXPLICIT | JAVA_ASSERTER   | "ContractsTest.java"
			SPOCK         | TestMode.MOCKMVC  | GROOVY_ASSERTER | "ContractsSpec.groovy"
			SPOCK         | TestMode.EXPLICIT | GROOVY_ASSERTER | "ContractsSpec.groovy"
	}

	def "should build test class for #testFramework with Rest Assured 2.x"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
			properties.testMode = mode
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2,
					convertAsCollection(new File("/"),file) )
			contract.ignored >> true
			contract.order >> 2
			JavaTestGenerator testGenerator = new JavaTestGenerator(checker: new ClassPresenceChecker() {
				@Override
				boolean isClassPresent(String className) {
					return true
				}
			})

		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')

		then:
			classStrings.each { assert clazz.contains(it) }
			clazz.contains("com.jayway.restassured")
			!clazz.contains("io.restassured")

		where:
			testFramework | mode              | classStrings
			JUNIT         | TestMode.MOCKMVC  | mockMvcJUnitRestAssured2ClassStrings
			JUNIT         | TestMode.EXPLICIT | explicitJUnitRestAssured2ClassStrings
			SPOCK         | TestMode.MOCKMVC  | spockClassRestAssured2Strings
			SPOCK         | TestMode.EXPLICIT | explicitSpockRestAssured2ClassStrings
	}

	def "should build test class for #testFramework and mode #mode with two files"() {
		given:
			File file = tmpFolder.newFile()
			file.write("""
					org.springframework.cloud.contract.spec.Contract.make {
						request {
							method 'PUT'
							url 'url1'
							headers {
								contentType(applicationJson())
							}
						}
						response {
							status 200
							body(foo:"foo", bar:"bar")
							headers {
								contentType(applicationJson())
							}
						}
					}
	""")
		and:
		File file2 = tmpFolder.newFile()
		file2.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'PUT'
						url 'url2'
						headers {
							contentType(applicationJson())
						}
					}
					response {
						status 200
						body(foo:"foo", bar:"bar")
						headers {
							contentType(applicationJson())
						}
					}
				}
""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), false, 1, null,
					convertAsCollection(new File("/"),file) )
			contract.ignored >> false
		and:
			ContractMetadata contract2 = new ContractMetadata(file2.toPath(), false, 1, null,
					convertAsCollection(new File("/"),file2) )
			contract2.ignored >> false
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract, contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
		and:
			asserter(clazz)
		and:
			textAssertion(clazz)
		where:
			testFramework | mode              | classStrings                          | asserter        | textAssertion
			JUNIT         | TestMode.MOCKMVC  | mockMvcJUnitRestAssured3ClassStrings  | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, "\t\t\tMockMvcRequestSpecification") == 2 }
			JUNIT         | TestMode.EXPLICIT | explicitJUnitRestAssured3ClassStrings | JAVA_ASSERTER   | { String test -> StringUtils.countOccurrencesOf(test, "\t\t\tMockMvcRequestSpecification") == 2 }
			SPOCK         | TestMode.MOCKMVC  | spockClassRestAssured3Strings         | GROOVY_ASSERTER | { String test -> StringUtils.countOccurrencesOf(test, "\t\t\tdef request") == 2 }
			SPOCK         | TestMode.EXPLICIT | explicitSpockRestAssured2ClassStrings | GROOVY_ASSERTER | { String test -> StringUtils.countOccurrencesOf(test, "\t\t\tdef request") == 2 }
	}

	def "should build JaxRs test class for #testFramework"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.testMode = TestMode.JAXRSCLIENT
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(new File("/"),file))
			contract.ignored >> true
			contract.order >> 2
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                                                                        | asserter
			JUNIT         | ['import static javax.ws.rs.client.Entity.*', 'import javax.ws.rs.core.Response']   | JAVA_JAXRS_ASSERTER
			SPOCK         | ['import static javax.ws.rs.client.Entity.*']                                       | GROOVY_ASSERTER
	}

	def "should work if there is messaging and rest in one folder #testFramework"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write("""
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
		""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(new File("/"),file))
			contract.ignored >> true
			contract.order >> 2
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2, convertAsCollection(new File("/"),secondFile))
			contract2.ignored >> true
			contract2.order >> 2
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract, contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Inject ContractVerifierMessaging')

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                         | asserter
			JUNIT         | mockMvcJUnitRestAssured3ClassStrings | JAVA_ASSERTER
			SPOCK         | spockClassRestAssured3Strings        | GROOVY_ASSERTER
	}

	@Issue('#30')
	def "should ignore a test if the contract is ignored in the dsl"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write("""
						org.springframework.cloud.contract.spec.Contract.make {
							ignored()
							request {
								method 'PUT'
								url 'url'
							}
							response {
								status 200
							}
						}
		""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = testFramework
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2, convertAsCollection(new File("/"),file))
			contract2.ignored >> false
			contract2.order >> 2
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Ignore')

		and:
			asserter(clazz)

		where:
			testFramework | classStrings                         | asserter
			JUNIT         | mockMvcJUnitRestAssured3ClassStrings | JAVA_ASSERTER
			SPOCK         | spockClassRestAssured3Strings        | GROOVY_ASSERTER
	}

	@Issue('#117')
	def "should generate test in explicit test mode using JUnit"() {
		given:
			String baseClass = """
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
			"""
			SyntaxChecker.tryToCompileJavaWithoutImports("test.ContextPathTestingBaseClass", "package test;\n${baseClass}")
		and:
			File secondFile = tmpFolder.newFile()
			secondFile.write("""
						// tag::context_path_contract[]
						org.springframework.cloud.contract.spec.Contract.make {
							request {
								method 'GET'
								url '/my-context-path/url'
							}
							response {
								status 200
							}
						}
						// end::context_path_contract[]
		""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.targetFramework = JUNIT
			properties.testMode = TestMode.EXPLICIT
			properties.baseClassForTests = "test.ContextPathTestingBaseClass"
		and:
			ContractMetadata contract = new ContractMetadata(file.toPath(), false, 1,
					null, convertAsCollection(new File("/"),file))
		and:
			SingleTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')
		then:
			clazz.contains("RequestSpecification request = given();")
			clazz.contains("Response response = given().spec(request)")
	}

	def "should pick the contract's name as the test method"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write("""
							org.springframework.cloud.contract.spec.Contract.make {
								name("MySuperMethod")
								request {
									method 'PUT'
									url 'url'
								}
								response {
									status 200
								}
							}
			""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File("/"),secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')
		then:
			clazz.contains("validate_mySuperMethod()")
		where:
			testFramework << [JUNIT, SPOCK]
	}

	def "should pick the contract's name as the test method when there are multiple contracts"() {
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
			status 200
		}
	}
}''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File("/"),secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')
		then:
			clazz.contains("validate_shouldHaveIndex1()")
			clazz.contains("validate_shouldHaveIndex2()")
		where:
			testFramework << [JUNIT, SPOCK]
	}

	def "should generate the test method when there are multiple contracts without name field"() {
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
			status 200
		}
	}
}''')
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(new File("/"),secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')
		then:
			clazz.contains("_0() throws Exception")
			clazz.contains("_1() throws Exception")
		where:
			testFramework << [JUNIT, SPOCK]
	}

	@Issue("#359")
	def "should generate tests from a contract that references a file for [#testFramework]"() {
		given:
			File contractLocation = new File(SingleTestGeneratorSpec.class.getResource("/classpath/readFromFile.groovy").toURI())
			File temp = tmpFolder.newFolder()
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
				targetFramework: testFramework, contractsDslDir: contractLocation.parentFile,
					basePackageForTests: "a.b",
					generatedTestSourcesDir:  temp
			)
			TestGenerator testGenerator = new TestGenerator(properties)
		when:
			int count = testGenerator.generate()
		then:
			count == 1
		and:
			String test = new File(temp, "a/b/ContractVerifier" + (testFramework == JUNIT ? "Test.java" : "Spec.groovy")).text
			test.contains("REQUEST")
			test.contains("RESPONSE")
		where:
			testFramework << [JUNIT, SPOCK]
	}




}
