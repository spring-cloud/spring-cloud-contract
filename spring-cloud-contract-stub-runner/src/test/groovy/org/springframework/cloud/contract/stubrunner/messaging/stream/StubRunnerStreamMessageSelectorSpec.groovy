/*
 *  Copyright 2013-2018 the original author or authors.
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
package org.springframework.cloud.contract.stubrunner.messaging.stream

import org.springframework.cloud.contract.spec.Contract
import org.springframework.messaging.Message
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerStreamMessageSelectorSpec extends Specification {
	Message message = Mock(Message)

	def "should return false if headers don't match"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageBody(foo: "bar")
					messageHeaders {
						header("foo", $(c(regex("[0-9]{3}")), p(123)))
					}
				}
			}
		and:
			StubRunnerStreamMessageSelector predicate = new StubRunnerStreamMessageSelector(dsl)
			message.headers >> [
					foo: "non matching stuff"
			]
		expect:
			!predicate.accept(message)
	}

	def "should return false if headers match and body doesn't"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
					}
					messageBody(foo: $(c(regex("[0-9]{3}")), p(123)))
				}
			}
		and:
			StubRunnerStreamMessageSelector predicate = new StubRunnerStreamMessageSelector(dsl)
			message.headers >> [
					foo: 123
			]
			message.payload >> [
					foo: "non matching stuff"
			]
		expect:
			!predicate.accept(message)
	}

	def "should return false if headers match and body doesn't when it's using matchers"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
					}
					messageBody(foo: "non matching stuff")
					bodyMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerStreamMessageSelector predicate = new StubRunnerStreamMessageSelector(dsl)
			message.headers >> [
					foo: 123
			]
			message.payload >> [
					foo: "non matching stuff"
			]
		expect:
			!predicate.accept(message)
	}

	def "should return true if headers and body match"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
					}
					messageBody(foo: $(c(regex("[0-9]{3}")), p(123)))

				}
			}
		and:
			StubRunnerStreamMessageSelector predicate = new StubRunnerStreamMessageSelector(dsl)
			message.headers >> [
					foo: 123
			]
			message.payload >> [
					foo: 123
			]
		expect:
			predicate.accept(message)
	}

	def "should return true if headers and body using matchers match"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
					}
					messageBody(foo: 123)
					bodyMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerStreamMessageSelector predicate = new StubRunnerStreamMessageSelector(dsl)
			message.headers >> [
					foo: 123
			]
			message.payload >> [
					foo: 123
			]
		expect:
			predicate.accept(message)
	}
}
