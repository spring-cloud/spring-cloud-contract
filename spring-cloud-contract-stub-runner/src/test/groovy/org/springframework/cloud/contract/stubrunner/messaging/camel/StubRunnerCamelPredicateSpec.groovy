/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.camel

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import org.apache.camel.impl.DefaultMessage
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelPredicateSpec extends Specification {
	Exchange exchange = new DefaultExchange(new DefaultCamelContext())
	Message message = new DefaultMessage()

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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo: "non matching stuff"
			]
		expect:
			!predicate.matches(exchange)
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo: 123
			]
			message.body = [
					foo: "non matching stuff"
			]
		expect:
			!predicate.matches(exchange)
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in >> message
			message.headers >> [
					foo: 123
			]
			message.body >> [
					foo: "non matching stuff"
			]
		expect:
			!predicate.matches(exchange)
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo: 123
			]
			message.body = [
					foo: 123
			]
		expect:
			predicate.matches(exchange)
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
					stubMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo: 123
			]
			message.body = [
					foo: 123
			]
		expect:
			predicate.matches(exchange)
	}

	def "should return true if headers and byte body matches"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
						messagingContentType(applicationOctetStream())
					}
					messageBody(fileAsBytes("request.pdf"))
				}
			}
		and:
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo        : 123,
					contentType: "application/octet-stream"
			]
			message.body = StubRunnerCamelPredicate.getResource("/request.pdf").bytes
		expect:
			predicate.matches(exchange)
	}

	def "should return false if byte body types don't match for binary"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
						messagingContentType(applicationOctetStream())
					}
					messageBody(fileAsBytes("request.pdf"))
				}
			}
		and:
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate([dsl])
			exchange.in = message
			message.headers = [
					foo        : 123,
					contentType: "application/octet-stream"
			]
			message.body = "hello world"
		expect:
			!predicate.matches(exchange)
	}
}
