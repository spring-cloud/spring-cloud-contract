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

	def 'should generate a value if only regex is passed for client'() {
		given:
			Request request = new Request()
			DslProperty property
		when:
			request.with {
				property = value(consumer(regex("[0-9]{5}")))
			}
			def value = Integer.valueOf(property.serverValue as String)
		then:
			value >= 0
			value <= 99_999
	}
}
