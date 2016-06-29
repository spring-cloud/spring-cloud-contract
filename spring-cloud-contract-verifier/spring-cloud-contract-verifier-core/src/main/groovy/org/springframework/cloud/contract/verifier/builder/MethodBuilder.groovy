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
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.util.NamesUtil
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata

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
	private final org.springframework.cloud.contract.spec.Contract stubContent
	private final ContractVerifierConfigProperties configProperties
	private final boolean ignored

	private MethodBuilder(String methodName, org.springframework.cloud.contract.spec.Contract stubContent, ContractVerifierConfigProperties configProperties, boolean ignored) {
		this.ignored = ignored
		this.stubContent = stubContent
		this.methodName = methodName
		this.configProperties = configProperties
	}

	/**
	 * A factory method that creates a {@link MethodBuilder} for the given arguments
	 */
	static MethodBuilder createTestMethod(ContractMetadata contract, File stubsFile, org.springframework.cloud.contract.spec.Contract stubContent, ContractVerifierConfigProperties configProperties) {
		log.debug("Stub content Groovy DSL [$stubContent]")
		String methodName = NamesUtil.camelCase(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, configProperties, contract.ignored)
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
			if (configProperties.targetFramework == TestFramework.JUNIT){
				return new JUnitMessagingMethodBodyBuilder(stubContent)
			}
			return new SpockMessagingMethodBodyBuilder(stubContent)
		}
		if (configProperties.testMode == TestMode.MOCKMVC && configProperties.targetFramework == TestFramework.JUNIT){
				return new MockMvcJUnitMethodBodyBuilder(stubContent)
		}
		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			if (configProperties.targetFramework == TestFramework.JUNIT){
				return new JaxRsClientJUnitMethodBodyBuilder(stubContent)
			}
			return new JaxRsClientSpockMethodRequestProcessingBodyBuilder(stubContent)
		}
		return new MockMvcSpockMethodRequestProcessingBodyBuilder(stubContent)
	}

}
