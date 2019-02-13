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

package org.springframework.cloud.contract.spec

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response

/**
 * The point of entry to the DSL
 *
 * @since 1.0.0
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Contract {

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of 2.
	 */
	Integer priority
	/**
	 * The HTTP request part of the contract
	 */
	Request request
	/**
	 * The HTTP response part of the contract
	 */
	Response response
	/**
	 * The label by which you'll reference the contract on the message consumer side
	 */
	String label
	/**
	 * Description of a contract. May be used in the documentation generation.
	 */
	String description
	/**
	 * Name of the generated test / stub. If not provided then the file name will be used.
	 * If you have multiple contracts in a single file and you don't provide this value
	 * then a prefix will be added to the file with the index number while iterating
	 * over the collection of contracts.
	 *
	 * Remember to have a unique name for every single contract. Otherwise you might
	 * generate tests that have two identical methods or you will override the stubs.
	 */
	String name
	/**
	 * The input side of a messaging contract.
	 */
	Input input
	/**
	 * The output side of a messaging contract.
	 */
	OutputMessage outputMessage

	/**
	 * Whether the contract should be ignored or not.
	 */
	boolean ignored

	protected Contract() {}

	/**
	 * Factory method to create the DSL
	 */
	static Contract make(@DelegatesTo(Contract) Closure closure) {
		Contract dsl = new Contract()
		closure.delegate = dsl
		closure()
		assertContract(dsl)
		return dsl
	}

	static void assertContract(Contract dsl) {
		if (dsl.request) {
			if (!dsl.request.url && !dsl.request.urlPath) {
				throw new IllegalStateException("URL is missing for HTTP contract")
			}
			if (!dsl.request.method) {
				throw new IllegalStateException("Method is missing for HTTP contract")
			}
		}
		if (dsl.response) {
			if (!dsl.response.status) {
				throw new IllegalStateException("Status is missing for HTTP contract")
			}
		}
		// Can't assert messaging part cause Pact doesn't require destinations it seems
	}

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of 2.
	 */
	void priority(int priority) {
		this.priority = priority
	}

	/**
	 * Name of the generated test / stub. If not provided then the file name will be used.
	 * If you have multiple contracts in a single file and you don't provide this value
	 * then a prefix will be added to the file with the index number while iterating
	 * over the collection of contracts.
	 *
	 * Remember to have a unique name for every single contract. Otherwise you might
	 * generate tests that have two identical methods or you will override the stubs.
	 */
	void name(String name) {
		this.name = name
	}

	/**
	 * Label used by the messaging contracts to trigger a message on the consumer side
	 *
	 * @param label - name of the label of a messaging contract to trigger
	 */
	void label(String label) {
		this.label = label
	}

	/**
	 * Description text. Might be used to describe the usage scenario.
	 *
	 * @param description - value of the description
	 */
	void description(String description) {
		this.description = description
	}

	/**
	 * The HTTP request part of the contract
	 */
	void request(@DelegatesTo(Request) Closure closure) {
		this.request = new Request()
		closure.delegate = request
		closure()
	}

	/**
	 * The HTTP response part of the contract
	 */
	void response(@DelegatesTo(Response) Closure closure) {
		this.response = new Response()
		closure.delegate = response
		closure()
	}

	void input(@DelegatesTo(Input) Closure closure) {
		this.input = new Input()
		closure.delegate = input
		closure()
	}

	/**
	 * The output side of a messaging contract.
	 */
	void outputMessage(@DelegatesTo(OutputMessage) Closure closure) {
		this.outputMessage = new OutputMessage()
		closure.delegate = outputMessage
		closure()
	}

	/**
	 * Whether the contract should be ignored or not.
	 */
	void ignored() {
		this.ignored = true
	}

}
