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
	 * take precedence
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
		return dsl
	}

	void priority(int priority) {
		this.priority = priority
	}

	void name(String name) {
		this.name = name
	}

	void label(String label) {
		this.label = label
	}

	void description(String description) {
		this.description = description
	}

	void request(@DelegatesTo(Request) Closure closure) {
		this.request = new Request()
		closure.delegate = request
		closure()
	}

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

	void outputMessage(@DelegatesTo(OutputMessage) Closure closure) {
		this.outputMessage = new OutputMessage()
		closure.delegate = outputMessage
		closure()
	}

	void ignored() {
		this.ignored = true
	}

}
