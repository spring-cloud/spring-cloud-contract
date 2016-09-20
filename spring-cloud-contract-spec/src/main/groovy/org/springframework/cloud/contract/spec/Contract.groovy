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
@ToString(includeFields = true, includePackage = false, includeNames = true)
class Contract {

	Integer priority
	Request request
	Response response
	String label
	String description
	Input input
	OutputMessage outputMessage

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

}
