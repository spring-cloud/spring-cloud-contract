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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import repackaged.nl.flotsam.xeger.Xeger

import java.util.regex.Pattern

/**
 * Represents an input for messaging. The input can be a message or some
 * action inside the application.
 *
 * @since 1.0.0
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Input extends Common {

	DslProperty<String> messageFrom
	ExecutionProperty triggeredBy
	Headers messageHeaders
	BodyType messageBody
	ExecutionProperty assertThat

	Input() {}

	Input(Input input) {
		this.messageFrom = input.messageFrom
		this.messageHeaders = input.messageHeaders
		this.messageBody = input.messageBody
	}

	/**
	 * Name of a destination from which message would come to trigger action in the system
	 */
	void messageFrom(String messageFrom) {
		this.messageFrom = new DslProperty<>(messageFrom)
	}

	/**
	 * Name of a destination from which message would come to trigger action in the system
	 */
	void messageFrom(DslProperty messageFrom) {
		this.messageFrom = messageFrom
	}

	/**
	 * Function that needs to be executed to trigger action in the system
	 */
	void triggeredBy(String triggeredBy) {
		this.triggeredBy = new ExecutionProperty(triggeredBy)
	}

	BodyType messageBody(Object bodyAsValue) {
		this.messageBody = new BodyType(bodyAsValue)
	}

	void messageHeaders(@DelegatesTo(Headers) Closure closure) {
		this.messageHeaders = new Headers()
		closure.delegate = messageHeaders
		closure()
	}

	DslProperty value(ClientDslProperty client) {
		Object clientValue = client.clientValue
		if (client.clientValue instanceof Pattern) {
			clientValue = new Xeger(((Pattern)client.clientValue).pattern()).generate()
		}
		return new DslProperty(client.clientValue, clientValue)
	}

	@EqualsAndHashCode(includeFields = true, callSuper = true)
	@ToString(includeSuper = true)
	static class BodyType extends DslProperty {

		BodyType(Object clientValue, Object serverValue) {
			super(clientValue, serverValue)
		}

		BodyType(Object singleValue) {
			super(singleValue)
		}
	}

	void assertThat(String assertThat) {
		this.assertThat = new ExecutionProperty(assertThat)
	}
}


@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ServerInput extends Input {
	ServerInput(Input request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ClientInput extends Input {
	ClientInput(Input request) {
		super(request)
	}
}
