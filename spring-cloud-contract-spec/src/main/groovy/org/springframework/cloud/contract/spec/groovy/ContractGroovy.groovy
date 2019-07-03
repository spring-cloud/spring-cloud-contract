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

package org.springframework.cloud.contract.spec.groovy

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.Contract
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
class ContractGroovy {

	Contract contract

	/**
	 * Factory method to create the DSL
	 */
	static Contract make(@DelegatesTo(Contract) Closure closure) {
		ContractGroovy contractGroovy = new ContractGroovy()
		Contract dsl = new Contract()
		closure.delegate = dsl
		closure()
		Contract.assertContract(dsl)
		contractGroovy.contract = dsl
		return dsl
	}


	/**
	 * The HTTP request part of the contract
	 */
	void request(@DelegatesTo(Request) Closure closure) {
		this.contract.request = new Request()
		closure.delegate = this.contract.request
		closure()
	}

	/**
	 * The HTTP response part of the contract
	 */
	void response(@DelegatesTo(Response) Closure closure) {
		this.contract.response = new Response()
		closure.delegate = this.contract.response
		closure()
	}

	void input(@DelegatesTo(Input) Closure closure) {
		this.contract.input = new Input()
		closure.delegate = this.contract.input
		closure()
	}

	/**
	 * The output side of a messaging contract.
	 */
	void outputMessage(@DelegatesTo(OutputMessage) Closure closure) {
		this.contract.outputMessage = new OutputMessage()
		closure.delegate = this.contract.outputMessage
		closure()
	}

}
