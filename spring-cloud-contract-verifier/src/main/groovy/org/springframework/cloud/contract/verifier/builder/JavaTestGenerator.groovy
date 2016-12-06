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

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize

/**
 * Builds a single test for the given {@link ContractVerifierConfigProperties properties}
 *
 * @since 1.1.0
 */
@Slf4j
class JavaTestGenerator implements SingleTestGenerator {

	private static final String JSON_ASSERT_STATIC_IMPORT = 'com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson'
	private static final String JSON_ASSERT_CLASS = 'com.toomuchcoding.jsonassert.JsonAssertion'

	@Override
	String buildClass(ContractVerifierConfigProperties configProperties, Collection<ContractMetadata> listOfFiles, String className, String classPackage, String includedDirectoryRelativePath) {
		ClassBuilder clazz = ClassBuilder.createClass(capitalize(className), classPackage, configProperties, includedDirectoryRelativePath)

		if (configProperties.imports) {
			configProperties.imports.each {
				clazz.addImport(it)
			}
		}

		if (configProperties.staticImports) {
			configProperties.staticImports.each {
				clazz.addStaticImport(it)
			}
		}

		if (isScenarioClass(listOfFiles)) {
			clazz.addImport(configProperties.targetFramework.getOrderAnnotationImport())
			clazz.addClassLevelAnnotation(configProperties.targetFramework.getOrderAnnotation())
		}

		addJsonPathRelatedImports(clazz)

		Map<ParsedDsl, TestType> contracts = mapContractsToTheirTestTypes(listOfFiles)

		boolean conditionalImportsAdded = false
		boolean toIgnore = listOfFiles.ignored.find { it }
		contracts.each { ParsedDsl key, TestType value ->
			if (!conditionalImportsAdded) {
				if (contracts.values().contains(TestType.HTTP)) {
					if (configProperties.testMode == TestMode.JAXRSCLIENT) {
						clazz.addStaticImport('javax.ws.rs.client.Entity.*')
						if (configProperties.targetFramework == TestFramework.JUNIT) {
							clazz.addImport('javax.ws.rs.core.Response')
						}
					} else if (configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
					} else {
						clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
					}
				}
				if (configProperties.targetFramework == TestFramework.JUNIT) {
					if (contracts.values().contains(TestType.HTTP) && configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addImport('com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification')
						clazz.addImport('com.jayway.restassured.response.ResponseOptions')
					}
					clazz.addImport('org.junit.Test')
					clazz.addStaticImport('org.assertj.core.api.Assertions.assertThat')
				}
				if (configProperties.ruleClassForTests) {
					clazz.addImport('org.junit.Rule').addRule(configProperties.ruleClassForTests)
				}
				if (contracts.values().contains(TestType.MESSAGING)) {
					addMessagingRelatedEntries(clazz)
				}
				conditionalImportsAdded = true
				toIgnore = toIgnore ? true: key.groovyDsl.ignored
			}
			clazz.addMethod(MethodBuilder.createTestMethod(key.contract, key.stubsFile, key.groovyDsl, configProperties))
		}

		if (toIgnore) {
			clazz.addImport(configProperties.targetFramework.getIgnoreClass())
		}

		return clazz.build()
	}

	private Map<ParsedDsl, TestType> mapContractsToTheirTestTypes(Collection<ContractMetadata> listOfFiles) {
		Map<ParsedDsl, TestType> dsls = [:]
		listOfFiles.each { ContractMetadata metadata ->
			File stubsFile = metadata.path.toFile()
			if (log.isDebugEnabled()) {
				log.debug("Stub content from file [${stubsFile.text}]")
			}
			List<Contract> stubContents = metadata.convertedContract
			Map<ParsedDsl, TestType> entries = stubContents.collectEntries { Contract stubContent ->
				TestType testType = (stubContent.input || stubContent.outputMessage) ? TestType.MESSAGING : TestType.HTTP
				return [(new ParsedDsl(metadata, stubContent, stubsFile)): testType]
			}
			dsls.putAll(entries)
		}
		return dsls
	}

	@Canonical
	@EqualsAndHashCode(includeFields = true)
	private static class ParsedDsl {
		ContractMetadata contract
		Contract groovyDsl
		File stubsFile
	}

	private static enum TestType {
		MESSAGING, HTTP
	}

	private boolean isScenarioClass(Collection<ContractMetadata> listOfFiles) {
		return listOfFiles.find({ it.order != null }) != null
	}

	private void addJsonPathRelatedImports(ClassBuilder clazz) {
		clazz.addImport(['com.jayway.jsonpath.DocumentContext',
		                 'com.jayway.jsonpath.JsonPath',
		])
		if (jsonAssertPresent()) {
			clazz.addStaticImport(JSON_ASSERT_STATIC_IMPORT)
		}
	}

	private void addMessagingRelatedEntries(ClassBuilder clazz) {
		clazz.addField(['@Inject ContractVerifierMessaging contractVerifierMessaging',
						'@Inject ContractVerifierObjectMapper contractVerifierObjectMapper'
		])
		clazz.addImport([ 'javax.inject.Inject',
						  'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper',
						  'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage',
						  'org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging',
		])
		clazz.addStaticImport('org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers')
	}

	private static boolean jsonAssertPresent() {
		try {
			Class.forName(JSON_ASSERT_CLASS)
			return true
		} catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug("JsonAssert is not present on classpath. Will not add a static import.")
			}
			return false
		}
	}

}
