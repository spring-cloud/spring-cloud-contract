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

package org.springframework.cloud.contract.verifier.builder

import java.lang.invoke.MethodHandles

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.builder.imports.HttpImportProvider
import org.springframework.cloud.contract.verifier.builder.imports.MessagingImportProvider
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import static org.springframework.cloud.contract.verifier.builder.imports.BaseImportProvider.getImports
import static org.springframework.cloud.contract.verifier.builder.imports.BaseImportProvider.getRuleImport
import static org.springframework.cloud.contract.verifier.builder.imports.BaseImportProvider.getStaticImports
import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize

/**
 * Builds a single test for the given {@link ContractVerifierConfigProperties properties}
 *
 * @since 1.1.0
 */
@CompileStatic
class JavaTestGenerator implements SingleTestGenerator {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass())

	private static final String JSON_ASSERT_STATIC_IMPORT = 'com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson'
	private static final String JSON_ASSERT_CLASS = 'com.toomuchcoding.jsonassert.JsonAssertion'
	@Deprecated
	// TODO: Remove in next major
	private static final String REST_ASSURED_2_0_CLASS = 'com.jayway.restassured.RestAssured'

	@PackageScope
	ClassPresenceChecker checker = new ClassPresenceChecker()

	@Override
	String buildClass(ContractVerifierConfigProperties configProperties, Collection<ContractMetadata> listOfFiles, String includedDirectoryRelativePath, GeneratedClassData generatedClassData) {
		String className = generatedClassData.className
		String classPackage = generatedClassData.classPackage
		ClassBuilder clazz = ClassBuilder.createClass(
				capitalize(className), classPackage, configProperties, includedDirectoryRelativePath)
		if (configProperties.imports) {
			configProperties.imports.each { String it ->
				clazz.addImport(it)
			}
		}
		if (configProperties.staticImports) {
			configProperties.staticImports.each { String it ->
				clazz.addStaticImport(it)
			}
		}
		if (isScenarioClass(listOfFiles)) {
			clazz.addImports(configProperties.testFramework.getOrderAnnotationImports())
			clazz.addClassLevelAnnotation(configProperties.testFramework.
					getOrderAnnotation())
		}
		// FIXME: change during Hoxton refactoring: we should only add either Json or Xml imports and not both
		addJsonPathRelatedImports(clazz)
		addXPathRelatedImports(clazz)
		processContractFiles(listOfFiles, configProperties, clazz, generatedClassData)
		return clazz.build()
	}

	@Override
	String buildClass(ContractVerifierConfigProperties configProperties, Collection<ContractMetadata> listOfFiles, String className, String classPackage, String includedDirectoryRelativePath) {
		return buildClass(configProperties, listOfFiles, includedDirectoryRelativePath, new GeneratedClassData(className, classPackage, null))
	}

	private void processContractFiles(Collection<ContractMetadata> listOfFiles, ContractVerifierConfigProperties configProperties, ClassBuilder clazz, GeneratedClassData generatedClassData) {
		Map<ParsedDsl, TestType> contracts = mapContractsToTheirTestTypes(listOfFiles)
		boolean conditionalImportsAdded = false
		boolean toIgnore = listOfFiles.find { it.ignored }
		contracts.each { ParsedDsl key, TestType value ->
			if (!conditionalImportsAdded) {
				clazz.addImports(getImports(configProperties.testFramework))
				clazz.addStaticImports(getStaticImports(configProperties.testFramework))
				if (contracts.values().contains(TestType.HTTP)) {
					addHttpRelatedEntries(clazz, configProperties)
				}
				if (configProperties.ruleClassForTests) {
					addRule(configProperties, clazz)
				}
				if (contracts.values().contains(TestType.MESSAGING)) {
					addMessagingRelatedEntries(clazz)
				}
				conditionalImportsAdded = true
			}
			toIgnore = toIgnore ? true : key.groovyDsl.ignored
			clazz.addMethod(MethodBuilder.createTestMethod(key.contract, key.stubsFile,
					key.groovyDsl, configProperties, generatedClassData))
		}

		if (toIgnore) {
			clazz.addImport(configProperties.testFramework.getIgnoreClass())
		}
	}

	private void addRule(ContractVerifierConfigProperties configProperties, ClassBuilder clazz) {
		clazz.addImport(getRuleImport(configProperties.testFramework))
		if (configProperties.testFramework.annotationLevelRules()) {
			clazz.addClassLevelAnnotation(configProperties.testFramework
														  .getRuleAnnotation(configProperties.ruleClassForTests))
		}
		else {
			clazz.addRule(configProperties.ruleClassForTests)
		}
	}

	private void addHttpRelatedEntries(ClassBuilder clazz, ContractVerifierConfigProperties configProperties) {
		HttpImportProvider httpImportProvider = new HttpImportProvider(
				getRestAssuredPackage())
		clazz.addImports(httpImportProvider.
				getImports(configProperties.testFramework, configProperties.testMode))
		clazz.addStaticImports(httpImportProvider.
				getStaticImports(configProperties.testFramework, configProperties.testMode))
	}

	// TODO for 2.2: leave only RestAssured 3
	private String getRestAssuredPackage() {
		boolean restAssured2Present = this.checker.isClassPresent(REST_ASSURED_2_0_CLASS)
		String restAssuredPackage = restAssured2Present ? 'com.jayway.restassured' : 'io.restassured'
		if (log.isDebugEnabled()) {
			log.debug("Rest Assured version 2.x found [${restAssured2Present}]")
		}
		return restAssuredPackage
	}

	@Override
	String fileExtension(ContractVerifierConfigProperties properties) {
		return properties.testFramework.classExtension
	}

	private Map<ParsedDsl, TestType> mapContractsToTheirTestTypes(Collection<ContractMetadata> listOfFiles) {
		Map<ParsedDsl, TestType> dsls = [:]
		listOfFiles.each { ContractMetadata metadata ->
			File stubsFile = metadata.path.toFile()
			if (log.isDebugEnabled()) {
				log.debug("Stub content from file [${stubsFile.text}]")
			}
			Collection<Contract> stubContents = metadata.convertedContract
			Map<ParsedDsl, TestType> entries = stubContents.
					collectEntries { Contract stubContent ->
						TestType testType = (stubContent.input || stubContent.outputMessage) ? TestType.MESSAGING : TestType.HTTP
						return [(new ParsedDsl(metadata, stubContent, stubsFile)): testType]
					}
			dsls.putAll(entries)
		}
		return dsls
	}

	@Canonical
	@EqualsAndHashCode(includeFields = true)
	@CompileStatic
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
		clazz.addImports(['com.jayway.jsonpath.DocumentContext',
						  'com.jayway.jsonpath.JsonPath',
		])
		if (this.checker.isClassPresent(JSON_ASSERT_CLASS)) {
			clazz.addStaticImport(JSON_ASSERT_STATIC_IMPORT)
		}
	}

	private void addXPathRelatedImports(ClassBuilder clazz) {
		clazz.addImports(['javax.xml.parsers.DocumentBuilder',
						  'javax.xml.parsers.DocumentBuilderFactory',
						  'org.w3c.dom.Document',
						  'org.xml.sax.InputSource',
						  'java.io.StringReader'])
	}

	private void addMessagingRelatedEntries(ClassBuilder clazz) {
		clazz.addField(['@Inject ContractVerifierMessaging contractVerifierMessaging',
						'@Inject ContractVerifierObjectMapper contractVerifierObjectMapper'
		])
		clazz.addImports(MessagingImportProvider.getImports())
		clazz.addStaticImports(MessagingImportProvider.getStaticImports())
	}
}

class ClassPresenceChecker {

	private static final Log log = LogFactory.getLog(ClassPresenceChecker)

	boolean isClassPresent(String className) {
		try {
			Class.forName(className)
			return true
		}
		catch (ClassNotFoundException ex) {
			if (log.isDebugEnabled()) {
				log.debug("[${className}] is not present on classpath. Will not add a static import.")
			}
			return false
		}
	}
}
