package io.codearte.accurest.dsl.internal

class OptionalProperty {
	final Object value

	OptionalProperty(Object value) {
		this.value = value
	}

	String optionalPattern() {
		return "($value)?"
	}
}
