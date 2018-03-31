/*
 *  Copyright 2013-2017 the original author or authors.
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

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import repackaged.nl.flotsam.xeger.Xeger
/**
 * Represents an input for messaging. The input can be a message or some
 * action inside the application.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Slf4j
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Input extends Common {

	@Delegate ClientPatternValueDslProperty property = new ClientPatternValueDslProperty()

	DslProperty<String> messageFrom
	ExecutionProperty triggeredBy
	Headers messageHeaders = new Headers()
	BodyType messageBody
	ExecutionProperty assertThat
	BodyMatchers bodyMatchers

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

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void stubMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	void bodyMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		this.bodyMatchers = new BodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ClientPatternValueDslProperty extends PatternValueDslProperty<ClientDslProperty> {

		@Override
		protected ClientDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ClientDslProperty(pattern, generatedValue)
		}
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
