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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.NamesUtil

/**
 * Builds a test method. Adds an ignore annotation on a method if necessary.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@Slf4j
@CompileStatic
@PackageScope
class MethodBuilder {

	private final String methodName
	private final Contract stubContent
	private final ContractVerifierConfigProperties configProperties
	private final boolean ignored

	private MethodBuilder(String methodName, Contract stubContent, ContractVerifierConfigProperties configProperties, boolean ignored) {
		this.ignored = ignored
		this.stubContent = stubContent
		this.methodName = methodName
		this.configProperties = configProperties
	}

	/**
	 * A factory method that creates a {@link MethodBuilder} for the given arguments
	 */
	static MethodBuilder createTestMethod(ContractMetadata contract, File stubsFile, Contract stubContent, ContractVerifierConfigProperties configProperties) {
		if (log.isDebugEnabled()) {
			log.debug("Stub content Groovy DSL [$stubContent]")
		}
		String methodName = methodName(contract, stubsFile, stubContent)
		return new MethodBuilder(methodName, stubContent, configProperties, contract.ignored || stubContent.ignored)
	}

	private static String methodName(ContractMetadata contract, File stubsFile, Contract stubContent) {
		if (stubContent.name) {
			return NamesUtil.camelCase(NamesUtil.convertIllegalPackageChars(stubContent.name))
		} else if (contract.convertedContract.size() > 1) {
			int index = contract.convertedContract.findIndexOf { it == stubContent}
			return "${camelCasedMethodFromFileName(stubsFile)}_${index}"
		}
		return camelCasedMethodFromFileName(stubsFile)
	}

	private static String camelCasedMethodFromFileName(File stubsFile) {
		return NamesUtil.camelCase(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
	}

	/**
	 * Appends to the {@link BlockBuilder} the contents of the test
	 */
	void appendTo(BlockBuilder blockBuilder) {
		if (configProperties.targetFramework == TestFramework.JUNIT) {
			blockBuilder.addLine('@Test')
		}
		if (ignored) {
			blockBuilder.addLine('@Ignore')
		}
		blockBuilder.addLine(configProperties.targetFramework.methodModifier + "validate_$methodName() throws Exception {")
		getMethodBodyBuilder().appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}

	private MethodBodyBuilder getMethodBodyBuilder() {
		if (stubContent.input || stubContent.outputMessage) {
			if (configProperties.targetFramework == TestFramework.JUNIT) {
				return new JUnitMessagingMethodBodyBuilder(stubContent, configProperties)
			}
			return new SpockMessagingMethodBodyBuilder(stubContent, configProperties)
		}
		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			if (configProperties.targetFramework == TestFramework.JUNIT) {
				return new JaxRsClientJUnitMethodBodyBuilder(stubContent, configProperties)
			}
			return new JaxRsClientSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties)
		} else if (configProperties.testMode == TestMode.EXPLICIT) {
			if (configProperties.targetFramework == TestFramework.JUNIT) {
				return new ExplicitJUnitMethodBodyBuilder(stubContent, configProperties)
			}
			// in Groovy we're using def so we don't have to update the imports
			return new MockMvcSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties)
		} else if (configProperties.targetFramework == TestFramework.SPOCK) {
			return new MockMvcSpockMethodRequestProcessingBodyBuilder(stubContent, configProperties)
		}
		return new MockMvcJUnitMethodBodyBuilder(stubContent, configProperties)
	}

}
