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
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2)
			contract.ignored >> true
			contract.order >> 2
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract], "test", "test", 'com/foo')

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
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2)
			contract.ignored >> true
			contract.order >> 2
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract], "test", "test", 'com/foo')

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
			ContractMetadata contract = new ContractMetadata(file.toPath(), true, 1, 2)
			contract.ignored >> true
			contract.order >> 2
		and:
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2)
			contract2.ignored >> true
			contract2.order >> 2
		and:
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract, contract2], "test", "test", 'com/foo')

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
			ContractMetadata contract2 = new ContractMetadata(secondFile.toPath(), true, 1, 2)
			contract2.ignored >> false
			contract2.order >> 2
		and:
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract2], "test", "test", 'com/foo')

		then:
			classStrings.each { clazz.contains(it) }
			clazz.contains('@Ignore')

		where:
			testFramework | classStrings
			JUNIT         | jUnitClassStrings
			SPOCK         | spockClassStrings
	}


}
