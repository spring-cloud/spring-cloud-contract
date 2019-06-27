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

package org.springframework.cloud.contract.verifier.file

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.NamesUtil
import org.springframework.util.Assert

/**
 * Contains metadata for a particular file with a DSL
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@CompileStatic
class ContractMetadata {
	/**
	 * Path to the file
	 */
	final Path path
	/**
	 * Should the contract be ignored
	 */
	final boolean ignored
	/**
	 * How many files are there in the folder
	 */
	final int groupSize
	/**
	 * If scenario related will contain an order of execution
	 */
	final Integer order
	/**
	 * The list of contracts for the given file
	 */
	final Collection<Contract> convertedContract = []
	/**
	 * Converted contracts with meta data information
	 */
	final Collection<SingleContractMetadata> convertedContractWithMetadata = []

	ContractMetadata(Path path, boolean ignored, int groupSize, Integer order, Contract convertedContract) {
		this(path, ignored, groupSize, order, [convertedContract])
	}

	ContractMetadata(Path path, boolean ignored, int groupSize, Integer order, Collection<Contract> convertedContract) {
		this.groupSize = groupSize
		this.path = path
		this.ignored = ignored
		this.order = order
		this.convertedContract.addAll(convertedContract)
		this.convertedContractWithMetadata.addAll(
				this.convertedContract
						.findAll { it != null }
						.collect { new SingleContractMetadata(it, this) })
	}
}

@CompileStatic
@EqualsAndHashCode(excludes = ["contractMetadata"])
@ToString(excludes = ["contractMetadata"])
class SingleContractMetadata {

	private static final Log log = LogFactory.getLog(SingleContractMetadata)

	final ContractMetadata contractMetadata
	private final File stubsFile
	final Contract contract
	private final Collection<Contract> allContracts
	final ContentType inputStubContentType
	final ContentType evaluatedInputStubContentType
	final ContentType outputStubContentType
	final ContentType evaluatedOutputStubContentType
	final ContentType inputTestContentType
	final ContentType evaluatedInputTestContentType
	final ContentType outputTestContentType
	final ContentType evaluatedOutputTestContentType
	private final boolean http

	SingleContractMetadata(Contract currentContract, ContractMetadata contractMetadata) {
		this.allContracts = contractMetadata.convertedContract
		this.contract = currentContract
		Assert.notNull(currentContract, "Contract must not be null")
		Headers inputHeaders = inputHeaders(currentContract)
		DslProperty inputBody = inputBody(currentContract)
		Headers outputHeaders = outputHeaders(currentContract)
		DslProperty outputBody = outputBody(currentContract)
		this.evaluatedInputTestContentType = ContentUtils.evaluateContentType(inputHeaders, inputBody?.getServerValue())
		this.inputTestContentType = inputBody != null ? this.evaluatedInputTestContentType : ContentType.UNKNOWN
		this.evaluatedOutputTestContentType = ContentUtils.evaluateContentType(outputHeaders, outputBody?.getServerValue())
		this.outputTestContentType = outputBody != null ? this.evaluatedOutputTestContentType : ContentType.UNKNOWN
		this.evaluatedInputStubContentType = ContentUtils.evaluateContentType(inputHeaders, inputBody?.getClientValue())
		this.inputStubContentType = inputBody != null ? this.evaluatedInputStubContentType : ContentType.UNKNOWN
		this.evaluatedOutputStubContentType = ContentUtils.evaluateContentType(outputHeaders, outputBody?.getClientValue())
		this.outputStubContentType = outputBody != null ? this.evaluatedOutputStubContentType : ContentType.UNKNOWN
		this.http = currentContract.request != null
		this.contractMetadata = contractMetadata
		this.stubsFile = contractMetadata.getPath() != null ? contractMetadata.getPath().toFile() : null
	}

	boolean isJson() {
		return this.inputTestContentType == ContentType.JSON ||
				this.outputTestContentType == ContentType.JSON ||
				this.inputStubContentType == ContentType.JSON ||
				this.outputStubContentType == ContentType.JSON
	}

	boolean evaluatesToJson() {
		return isJson() || this.evaluatedInputTestContentType == ContentType.JSON ||
				this.evaluatedOutputTestContentType == ContentType.JSON ||
				this.evaluatedInputStubContentType == ContentType.JSON ||
				this.evaluatedOutputStubContentType == ContentType.JSON
	}

	boolean isIgnored() {
		return this.contract.ignored || this.contractMetadata.ignored
	}

	boolean isXml() {
		return this.inputTestContentType == ContentType.XML ||
				this.outputTestContentType == ContentType.XML ||
				this.inputStubContentType == ContentType.XML ||
				this.outputStubContentType == ContentType.XML
	}

	boolean isHttp() {
		return this.http
	}

	boolean isMessaging() {
		return !isHttp()
	}

	private DslProperty inputBody(Contract contract) {
		return contract.request?.body ?: contract.input?.messageBody
	}

	private Headers inputHeaders(Contract contract) {
		return contract.request?.headers ?: contract.input?.messageHeaders
	}

	private DslProperty outputBody(Contract contract) {
		return contract.response?.body ?: contract.outputMessage?.body
	}

	private Headers outputHeaders(Contract contract) {
		return contract.response?.headers ?: contract.outputMessage?.headers
	}

	String methodName() {
		if (contract.name) {
			String name = NamesUtil.
					camelCase(NamesUtil.convertIllegalPackageChars(contract.name))
			if (log.isDebugEnabled()) {
				log.debug("Overriding the default test name with [" + name + "]")
			}
			return name
		}
		else if (allContracts.size() > 1) {
			int index = allContracts.findIndexOf { it == contract }
			String name = "${camelCasedMethodFromFileName(stubsFile)}_${index}"
			if (log.isDebugEnabled()) {
				log.debug("Scenario found. The method name will be [" + name + "]")
			}
			return name
		}
		String name = camelCasedMethodFromFileName(stubsFile)
		if (log.isDebugEnabled()) {
			log.debug("The method name will be [" + name + "]")
		}
		return name
	}

	private static String camelCasedMethodFromFileName(File stubsFile) {
		return NamesUtil.camelCase(NamesUtil.convertIllegalMethodNameChars(NamesUtil.
				toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator))))
	}
}
