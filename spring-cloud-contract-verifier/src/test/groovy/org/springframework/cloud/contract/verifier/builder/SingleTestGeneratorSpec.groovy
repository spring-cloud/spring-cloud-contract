/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import spock.lang.Issue
import spock.lang.Specification

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK
import static org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter.convertAsCollection

class SingleTestGeneratorSpec extends Specification {

	@Rule
	TemporaryFolder tmpFolder = new TemporaryFolder()
	File file

	static List<String> jUnitClassStrings = ['package test;', 'import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
	                                         'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
	                                         'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;',
	                                         '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'mport com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																						'import com.jayway.restassured.response.ResponseOptions;', 'import static org.assertj.core.api.Assertions.assertThat;']

	static List<String> spockClassStrings = ['package test', 'import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
	                                         'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
	                                         'import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*',
	                                         '@Stepwise', '@Ignore']

	def setup() {
		file = tmpFolder.newFile()
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

	def "should build MockMvc test class for #testFramework"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(file))
			contract.ignored >> true
			contract.order >> 2
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }

		where:
			testFramework | classStrings
			JUNIT         | jUnitClassStrings
			SPOCK         | spockClassStrings
	}

	def "should build JaxRs test class for #testFramework"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.testMode = TestMode.JAXRSCLIENT
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(file))
			contract.ignored >> true
			contract.order >> 2
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }

		where:
			testFramework | classStrings
			JUNIT         | ['import static javax.ws.rs.client.Entity.*;', 'import javax.ws.rs.core.Response;']
			SPOCK         | ['import static javax.ws.rs.client.Entity.*;']
	}

	def "should work if there is messaging and rest in one folder #testFramework"() {
		given:
			File secondFile = tmpFolder.newFile()
			secondFile.write("""
						org.springframework.cloud.contract.spec.Contract.make {
						  label 'some_label'
						  input {
							messageFrom('delete')
							messageBody([
								bookName: 'foo'
							])
							messageHeaders {
							  header('sample', 'header')
							}
							assertThat('bookWasDeleted()')
						  }
						}
		""")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2, convertAsCollection(file))
			contract.ignored >> true
			contract.order >> 2
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2, convertAsCollection(secondFile))
			contract2.ignored >> true
			contract2.order >> 2
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract, contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Inject ContractVerifierMessaging')

		where:
			testFramework | classStrings
			JUNIT         | jUnitClassStrings
			SPOCK         | spockClassStrings
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
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
			properties.targetFramework = testFramework
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2, convertAsCollection(file))
			contract2.ignored >> false
			contract2.order >> 2
		and:
			JavaTestGenerator testGenerator = new JavaTestGenerator()

		when:
			String clazz = testGenerator.buildClass(properties, [contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Ignore')

		where:
			testFramework | classStrings
			JUNIT         | jUnitClassStrings
			SPOCK         | spockClassStrings
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
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(secondFile))
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
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(secondFile))
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
			ContractMetadata contract = new ContractMetadata(secondFile.toPath(), false, 1, null, convertAsCollection(secondFile))
			JavaTestGenerator testGenerator = new JavaTestGenerator()
		when:
			String clazz = testGenerator.buildClass(properties, [contract], "test", "test", 'com/foo')
		then:
			clazz.contains("_0() throws Exception")
			clazz.contains("_1() throws Exception")
		where:
			testFramework << [JUNIT, SPOCK]
	}


}
