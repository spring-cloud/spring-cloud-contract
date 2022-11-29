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

package org.springframework.cloud.contract.spec.internal

import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
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
					status 200
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

	def 'should fail when no method is present'() {
		when:
			Contract.make {
				request {
					url('/foo')
				}
				response {
					status 200
				}
			}
		then:
			IllegalStateException ex = thrown(IllegalStateException)
			ex.message.contains("Method is missing for HTTP contract")
	}

	def 'should fail when no url is present'() {
		when:
			Contract.make {
				request {
					method("GET")
				}
				response {
					status 200
				}
			}
		then:
			IllegalStateException ex = thrown(IllegalStateException)
			ex.message.contains("URL is missing for HTTP contract")
	}

	def 'should fail when no status is present'() {
		when:
			Contract.make {
				request {
					url("/foo")
					method("GET")
				}
				response {
				}
			}
		then:
			IllegalStateException ex = thrown(IllegalStateException)
			ex.message.contains("Status is missing for HTTP contract")
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

	def 'should set a name'() {
		given:
			// tag::name[]
			org.springframework.cloud.contract.spec.Contract.make {
				name("some_special_name")
			}
			// end::name[]
	}

	def 'should mark a contract ignored'() {
		given:
			// tag::ignored[]
			org.springframework.cloud.contract.spec.Contract.make {
				ignored()
			}
			// end::ignored[]
	}

	def 'should mark a contract in progress'() {
		given:
			// tag::in_progress[]
			org.springframework.cloud.contract.spec.Contract.make {
				inProgress()
			}
			// end::in_progress[]
	}

	def 'should make equals and hashcode work properly for URL'() {
		expect:
			def a = Contract.make {
				request {
					method("GET")
					url("/1")
				}
			}
			def b = Contract.make {
				request {
					method("GET")
					url("/1")
				}
			}
			a == b
	}

	def 'should make equals and hashcode work properly for URL with consumer producer'() {
		expect:
			Contract.make {
				request {
					method("GET")
					url($(c("/1"), p("/1")))
				}
			} == Contract.make {
				request {
					method("GET")
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
					status OK()
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
					status OK()
				}
			}
			a.request.method == b.request.method
			a.request.url == b.request.url
			def firstHeader = a.request.headers.entries.first()
			def secondHeader = b.request.headers.entries.first()
			firstHeader == secondHeader
			a.request.headers.entries == b.request.headers.entries
			a.request.headers == b.request.headers
			a.request == b.request
			a.response.status == b.response.status
			a.response == b.response
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
					status OK()
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
					status OK()
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
					status OK()
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
					status OK()
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
			a.request.method == b.request.method
			a.request.url == b.request.url
			def aFirstHeader = a.request.headers.entries.first()
			def bFirstHeader = b.request.headers.entries.first()
			aFirstHeader == bFirstHeader
			def aLastHeader = a.request.headers.entries.last()
			def bLastHeader = b.request.headers.entries.last()
			aLastHeader == bLastHeader
			a.request.headers.entries == b.request.headers.entries
			a.request.headers == b.request.headers
			a.request.body == b.request.body
			a.request == b.request
			a.response.status == b.response.status
			def aRespFirstHeader = a.response.headers.entries.first()
			def bRespFirstHeader = b.response.headers.entries.first()
			aRespFirstHeader == bRespFirstHeader
			a.response.body == b.response.body
			a.response == b.response
			a == b
	}

	def 'should work with optional and null value of a field'() {
		given:
			def contract = Contract.make {
				description("Creating user")
				name("Create user")
				request {
					method 'POST'
					url '/api/user'
					body(
						address: $(consumer(optional(regex(alphaNumeric()))), producer(null)),
						name: $(consumer(optional(regex(alphaNumeric()))), producer(''))
					)
					headers {
						contentType(applicationJson())
					}
				}
				response {
					status 201
				}
			}
		expect:
			contract != null
	}

	@Issue("1200")
	def 'should fail when regex do not match the concrete value'() {
		when:
			Contract.make {
				request {
					method 'GET'
					url '/any'
				}
				response {
					status OK()
					body([
							time: $(p(regex(iso8601WithOffset())),c( "thisIsNotADate"))
					])
				}
			}
		then:
			thrown(IllegalStateException)
	}

	@Issue("1215")
	def 'should work fine when dealing with anyOf'() {
		when:
			Contract.make {
				request {
					method 'GET'
					url '/any'
					body (
							foo: $(consumer(optional(anyOf('WORKS','MIGHTY', 'DESPAIR'))), producer('DESPAIR'))
					)
				}
				response {
					status OK()
				}
			}
		then:
			noExceptionThrown()
	}
}
