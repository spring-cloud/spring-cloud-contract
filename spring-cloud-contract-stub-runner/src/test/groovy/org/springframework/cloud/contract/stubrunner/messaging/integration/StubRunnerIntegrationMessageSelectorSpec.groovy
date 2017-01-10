package org.springframework.cloud.contract.stubrunner.messaging.integration

import org.springframework.cloud.contract.spec.Contract
import org.springframework.messaging.Message
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class StubRunnerIntegrationMessageSelectorSpec extends Specification {
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
			StubRunnerIntegrationMessageSelector predicate = new StubRunnerIntegrationMessageSelector(dsl)
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
			StubRunnerIntegrationMessageSelector predicate = new StubRunnerIntegrationMessageSelector(dsl)
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
					stubMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerIntegrationMessageSelector predicate = new StubRunnerIntegrationMessageSelector(dsl)
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
			StubRunnerIntegrationMessageSelector predicate = new StubRunnerIntegrationMessageSelector(dsl)
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
					stubMatchers {
						jsonPath('$.foo', byRegex("[0-9]{3}"))
					}
				}
			}
		and:
			StubRunnerIntegrationMessageSelector predicate = new StubRunnerIntegrationMessageSelector(dsl)
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
