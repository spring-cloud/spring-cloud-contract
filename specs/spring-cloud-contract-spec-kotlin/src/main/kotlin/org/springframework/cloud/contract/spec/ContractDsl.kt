/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.spec.internal.*

/**
 * @author Tim Ysewyn
 * @since 2.2.0
 */
@ContractDslMarker
class ContractDsl {

	companion object {
		fun contract(dsl: ContractDsl.() -> Unit): Contract = ContractDsl().apply(dsl).get()
	}

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of
	 * 2.
	 */
	var priority: Int? = null

	/**
	 * The label by which you'll reference the contract on the message consumer side.
	 */
	var label: String? = null

	/**
	 * Description of a contract. May be used in the documentation generation.
	 */
	var description: String? = null

	/**
	 * Name of the generated test / stub. If not provided then the file name will be used.
	 * If you have multiple contracts in a single file and you don't provide this value
	 * then a prefix will be added to the file with the index number while iterating over
	 * the collection of contracts.
	 *
	 * Remember to have a unique name for every single contract. Otherwise you might
	 * generate tests that have two identical methods or you will override the stubs.
	 */
	var name: String? = null

	/**
	 * Whether the contract should be ignored or not.
	 */
	var ignored: Boolean = false

	/**
	 * Whether the contract is in progress. It's not ignored, but the feature is not yet
	 * finished. Used together with the {@code generateStubs} option.
	 */
	var inProgress: Boolean = false

	/**
	 * The HTTP request part of the contract.
	 */
	var request: Request? = null

	/**
	 * The HTTP response part of the contract.
	 */
	var response: Response? = null

	/**
	 * The input side of a messaging contract.
	 */
	var input: Input? = null

	/**
	 * The output side of a messaging contract.
	 */
	var outputMessage: OutputMessage? = null

	/**
	 * Mapping of metadata. Can be used for external integrations.
	 */
	var metadata: Map<String, Any> = HashMap()

	/**
	 * The HTTP request part of the contract.
	 * @param configurer lambda to configure the HTTP request
	 */
	fun request(configurer: RequestDsl.() -> Unit) {
		request = RequestDsl().apply(configurer).get()
	}

	/**
	 * The HTTP response part of the contract.
	 * @param configurer lambda to configure the HTTP response
	 */
	fun response(configurer: ResponseDsl.() -> Unit) {
		response = ResponseDsl().apply(configurer).get()
	}

	/**
	 * The input part of the contract.
	 * @param configurer lambda to configure the input message
	 */
	fun input(configurer: InputDsl.() -> Unit) {
		input = InputDsl().apply(configurer).get()
	}

	/**
	 * The output part of the contract.
	 * @param configurer lambda to configure the output message
	 */
	fun outputMessage(configurer: OutputMessageDsl.() -> Unit) {
		outputMessage = OutputMessageDsl().apply(configurer).get()
	}

	/**
	 * The output part of the contract.
	 * @param configurer lambda to configure the output message
	 */
	fun metadata(map: Map<String, Object>) {
		metadata = metadata.plus(map)
	}

	private fun get(): Contract {
		val contract = Contract()
		priority?.also { contract.priority = priority }
		label?.also { contract.label = label }
		description?.also { contract.description = description }
		name?.also { contract.name = name }
		contract.metadata = metadata
		contract.ignored = ignored
		contract.inProgress = inProgress
		request?.also { contract.request = request }
		response?.also { contract.response = response }
		input?.also { contract.input = input }
		outputMessage?.also { contract.outputMessage = outputMessage }
		return contract
	}

}
