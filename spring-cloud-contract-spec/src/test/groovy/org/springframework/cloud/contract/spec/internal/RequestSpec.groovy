package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class RequestSpec extends Specification {

	def 'should throw exception when on request side a value contains regex for server'() {
		given:
			Request contract = new Request()
		when:
			contract.with {
				value(consumer("foo"), producer(regex("foo")))
			}
		then:
			thrown(IllegalStateException)
		when:
			contract.with {
				value(producer(regex("foo")), consumer("foo"))
			}
		then:
			thrown(IllegalStateException)
	}
	def 'should set property when using the $() convenience method'() {
		given:
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
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
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
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
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
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
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
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
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
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
			Request contract = new Request()
			DslProperty property
		when:
			contract.with {
				property = $(consumer(regex("[0-9]{5}").asFloat()))
			}
			def value = property.serverValue
			value instanceof Float
		then:
			value >= 0
			value <= 99_999
	}
}
