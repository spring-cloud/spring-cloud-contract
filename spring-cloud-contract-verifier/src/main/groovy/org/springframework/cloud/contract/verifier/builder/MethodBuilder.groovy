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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.NamesUtil

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK

/**
 * Builds a test methodBuilder. Adds an ignore annotation on a methodBuilder if necessary.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@Commons
@CompileStatic
@PackageScope
class MethodBuilder {

	private final String methodName
	private final Contract stubContent
	private final ContractVerifierConfigProperties configProperties
	private final GeneratedClassDataForMethod generatedClassDataForMethod
	private final boolean ignored

	private MethodBuilder(String methodName, Contract stubContent, ContractVerifierConfigProperties configProperties,
			GeneratedClassDataForMethod generatedClassDataForMethod, boolean ignored) {
		this.ignored = ignored
		this.stubContent = stubContent
		this.methodName = methodName
		this.configProperties = configProperties
		this.generatedClassDataForMethod = generatedClassDataForMethod
	}

	/**
	 * A factory methodBuilder that creates a {@link MethodBuilder} for the given arguments
	 */
	static MethodBuilder createTestMethod(ContractMetadata contract, File stubsFile, Contract stubContent, ContractVerifierConfigProperties configProperties, SingleTestGenerator.GeneratedClassData generatedClassData) {
		if (log.isDebugEnabled()) {
			log.debug("Stub content Groovy DSL [$stubContent]")
		}
		String methodName = methodName(contract, stubsFile, stubContent)
		return new MethodBuilder(methodName, stubContent, configProperties,
				new GeneratedClassDataForMethod(generatedClassData, methodName), contract.ignored || stubContent.ignored)
	}

	static String methodName(ContractMetadata contract, File stubsFile, Contract stubContent) {
		if (stubContent.name) {
			String name = NamesUtil.
					camelCase(NamesUtil.convertIllegalPackageChars(stubContent.name))
			if (log.isDebugEnabled()) {
				log.debug("Overriding the default test name with [" + name + "]")
			}
			return name
		}
		else if (contract.convertedContract.size() > 1) {
			int index = contract.convertedContract.findIndexOf { it == stubContent }
			String name = "${camelCasedMethodFromFileName(stubsFile)}_${index}"
			if (log.isDebugEnabled()) {
				log.debug("Scenario found. The methodBuilder name will be [" + name + "]")
			}
			return name
		}
		String name = camelCasedMethodFromFileName(stubsFile)
		if (log.isDebugEnabled()) {
			log.debug("The methodBuilder name will be [" + name + "]")
		}
		return name
	}

	private static String camelCasedMethodFromFileName(File stubsFile) {
		return NamesUtil.camelCase(NamesUtil.convertIllegalMethodNameChars(NamesUtil.
				toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator))))
	}

	/**
	 * Appends to the {@link BlockBuilder} the contents of the test
	 */
	void appendTo(BlockBuilder blockBuilder) {
		if (isJUnitType()) {
			blockBuilder.addLine('@Test')
		}
		if (ignored) {
			blockBuilder.addLine(configProperties.testFramework.ignoreAnnotation)
		}
		blockBuilder.
				addLine(configProperties.testFramework.methodModifier + "validate_$methodName() throws Exception {")
		getMethodBodyBuilder().appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}

	private MethodBodyBuilder getMethodBodyBuilder() {
		if (stubContent.input || stubContent.outputMessage) {
			if (isJUnitType()) {
				return new JUnitMessagingMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
			}
			return new SpockMessagingMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
		}
		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			if (isJUnitType()) {
				return new JaxRsClientJUnitMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
			}
			return new JaxRsClientSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
		}
		else if (configProperties.testMode == TestMode.WEBTESTCLIENT) {
			if (isJUnitType()) {
				return new WebTestClientJUnitMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
			}
			return new HttpSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
		}
		else if (configProperties.testMode == TestMode.EXPLICIT) {
			if (isJUnitType()) {
				return new ExplicitJUnitMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
			}
			// in Groovy we're using def so we don't have to update the imports
			return new HttpSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
		}
		else if (configProperties.testFramework == SPOCK) {
			return new HttpSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
		}
		return new MockMvcJUnitMethodBodyBuilder(stubContent, configProperties, this.generatedClassDataForMethod)
	}

	private boolean isJUnitType() {
		return JUNIT == configProperties.testFramework || JUNIT5 == configProperties.testFramework
	}
}
