package org.springframework.cloud.contract.stubrunner.messaging.camel

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerCamelPredicateSpec extends Specification {
	Exchange exchange = Stub(Exchange)
	Message message = Stub(Message)

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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate(dsl)
			exchange.in >> message
			message.headers >> [
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate(dsl)
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

	def "should return false if headers match and body doesn't when it's using matchers"() {
		given:
			Contract dsl = Contract.make {
				input {
					messageFrom "foo"
					messageHeaders {
						header("foo", 123)
					}
					messageBody(foo: "non matching stuff")
					stubMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate(dsl)
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate(dsl)
			exchange.in >> message
			message.headers >> [
					foo: 123
			]
			message.body >> [
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
			StubRunnerCamelPredicate predicate = new StubRunnerCamelPredicate(dsl)
			exchange.in >> message
			message.headers >> [
					foo: 123
			]
			message.body >> [
					foo: 123
			]
		expect:
			predicate.matches(exchange)
	}
}
