package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

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

	ServerDslProperty producer(Object clientValue) {
		return new ServerDslProperty(clientValue)
	}

	ClientDslProperty consumer(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	void messageFrom(String messageFrom) {
		this.messageFrom = new DslProperty<>(messageFrom)
	}

	void messageFrom(DslProperty messageFrom) {
		this.messageFrom = messageFrom
	}

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

	public static class BodyType extends DslProperty {

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
