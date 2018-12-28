package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Tim Ysewyn
 */
class InputSpec extends Specification {

	def 'should set property when using the $() convenience method'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}")))
			}
			def generatedValue = property.serverValue
			generatedValue instanceof String
			def value = Integer.valueOf(generatedValue as String)
		then:
			value >= 0
			value <= 99_999
	}

	def 'should set property when using the $() convenience method for Double'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}").asDouble()))
			}
			def value = property.serverValue
			value instanceof Double
		then:
			value >= 0
			value <= 99_999
	}

	def 'should set property when using the $() convenience method for Short'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{1}").asShort()))
			}
			def value = property.serverValue
			value instanceof Short
		then:
			value >= 0
			value <= 9
	}

	def 'should set property when using the $() convenience method for Long'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}").asLong()))
			}
			def value = property.serverValue
			value instanceof Long
		then:
			value >= 0
			value <= 99_999
	}

	def 'should set property when using the $() convenience method for Integer'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}").asInteger()))
			}
			def value = property.serverValue
			value instanceof Integer
		then:
			value >= 0
			value <= 99_999
	}

	def 'should set property when using the $() convenience method for Float'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}").asFloat()))
			}
			def value = property.serverValue
			value instanceof Float
		then:
			value >= 0
			value <= 99_999
	}
}
