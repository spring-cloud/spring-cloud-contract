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

	def 'should set a description'() {
		given:
		// tag::description[]
		org.springframework.cloud.contract.spec.Contract.make {
			description('''
given:
	An input
when:
	Sth happens
then:
	Output
''')
		}
		// end::description[]
	}

	def 'should mark a contract ignored'() {
		given:
		// tag::ignored[]
		org.springframework.cloud.contract.spec.Contract.make {
			ignored()
		}
		// end::ignored[]
	}

	def 'should make equals and hashcode work properly for URL'() {
		expect:
			def a = Contract.make {
				request {
					url("/1")
			 	}
			}
			def b = Contract.make {
					request {
						url("/1")
					}
				}
			a == b
	}

	def 'should make equals and hashcode work properly for URL with consumer producer'() {
		expect:
			Contract.make {
				request {
					url($(c("/1"), p("/1")))
			 	}
			} == Contract.make {
				request {
					url($(c("/1"), p("/1")))
				}
			}
	}

	def 'should return true when comparing two equal contracts with gstring'() {
		expect:
			int index = 1
			def a = Contract.make {
				request {
					method(PUT())
					headers {
						contentType(applicationJson())
					}
					url "/${index}"
				}
				response {
					status 200
				}
			}
			def b = Contract.make {
				request {
					method(PUT())
					headers {
						contentType(applicationJson())
					}
					url "/${index}"
				}
				response {
					status 200
				}
			}
			a == b
	}

	def 'should return false when comparing two unequal contracts with gstring'() {
		expect:
			int index = 1
			def a = Contract.make {
				request {
					method(PUT())
					headers {
						contentType(applicationJson())
					}
					url "/${index}"
				}
				response {
					status 200
				}
			}
			int index2 = 2
			def b = Contract.make {
				request {
					method(PUT())
					headers {
						contentType(applicationJson())
					}
					url "/${index2}"
				}
				response {
					status 200
				}
			}
			a != b
	}

	def 'should return true when comparing two equal complex contracts'() {
		expect:
			def a = Contract.make {
				request {
					method 'GET'
					url '/path'
					headers {
						header('Accept': $(
								consumer(regex('text/.*')),
								producer('text/plain')
						))
						header('X-Custom-Header': $(
								consumer(regex('^.*2134.*$')),
								producer('121345')
						))
					}
				}
				response {
					status 200
					body(
							id: [value: '132'],
							surname: 'Kowalsky',
							name: 'Jan',
							created: '2014-02-02 12:23:43'
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
			def b = Contract.make {
				request {
					method 'GET'
					url '/path'
					headers {
						header('Accept': $(
								consumer(regex('text/.*')),
								producer('text/plain')
						))
						header('X-Custom-Header': $(
								consumer(regex('^.*2134.*$')),
								producer('121345')
						))
					}
				}
				response {
					status 200
					body(
							id: [value: '132'],
							surname: 'Kowalsky',
							name: 'Jan',
							created: '2014-02-02 12:23:43'
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
			a == b
	}
}
