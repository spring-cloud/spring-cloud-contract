package org.springframework.cloud.contract.spec.internal

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class ContractSpec extends Specification {

	def 'should work for http'() {
		when:
			Contract.make {
				request {
					url('/foo')
					method('PUT')
					headers {
						header([
						        foo: 'bar'
						])
					}
					body([
					        foo: 'bar'
					])
				}
				response {
					headers {
						header([
								foo2: 'bar'
						])
					}
					body([
							foo2: 'bar'
					])
				}
			}
		then:
			noExceptionThrown()
	}
	def 'should work for messaging'() {
		when:
			Contract.make {
				input {
					messageFrom('input')
					messageBody([
							foo: 'bar'
					])
					messageHeaders {
						header([
								foo: 'bar'
						])
					}
				}
				outputMessage {
					sentTo('output')
					body([
							foo2: 'bar'
					])
					headers {
						header([
								foo2: 'bar'
						])
					}
				}
			}
		then:
			noExceptionThrown()
	}

	def 'should generate a value if only regex is passed for client'() {
		given:
			Request request = new Request()
			DslProperty property
		when:
			request.with {
				property = value(consumer(regex("[0-9]{5}")))
			}
		then:
			(property.serverValue as String).matches(/[0-9]{5}/)
	}
}
