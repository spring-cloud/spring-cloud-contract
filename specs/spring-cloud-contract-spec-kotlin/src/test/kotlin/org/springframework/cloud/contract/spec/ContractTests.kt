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

package org.springframework.cloud.contract.spec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.RegexProperty

/**
 * Tests written based on the Java contract tests written in Groovy
 *
 * @author Tim Ysewyn
 */
class ContractTests {

	@Test
	fun `should work for http`() {
		val contract = contract {
			request {
				url = url("/foo")
				method = PUT
				headers {
					header {
						name = "foo"
						value = "bar"
					}
				}
				body = body("foo" to "bar")
			}
			response {
				status = OK
				headers {
					header {
						name = "foo2"
						value = "bar"
					}
				}
				body = body("foo2" to "bar")
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.url.clientValue).isEqualTo("/foo")
			assertThat(request.url.serverValue).isEqualTo("/foo")
			assertThat(request.method.clientValue).isEqualTo("PUT")
			assertThat(request.method.serverValue).isEqualTo("PUT")
			val headers = request.headers.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("foo")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("bar")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("bar")
			assertThat(request.body.clientValue).isEqualTo(mapOf("foo" to "bar"))
			assertThat(request.body.serverValue).isEqualTo(mapOf("foo" to "bar"))
		}.also {
			val response = contract.response
			assertThat(response.status.clientValue).isEqualTo(200)
			assertThat(response.status.serverValue).isEqualTo(200)
			val headers = response.headers.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("foo2")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("bar")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("bar")
			assertThat(response.body.clientValue).isEqualTo(mapOf("foo2" to "bar"))
			assertThat(response.body.serverValue).isEqualTo(mapOf("foo2" to "bar"))
		}
	}

	@Test
	fun `should fail when no method is present`() {
		val contract = contract {
			request {
				url = url("/foo")
			}
			response {
				status = OK
			}
		}

		assertThrows<IllegalStateException> {
			Contract.assertContract(contract)
		}.also {
			assertThat(it.message).contains("Method is missing for HTTP contract")
		}
	}

	@Test
	fun `should fail when no url is present`() {
		val contract = contract {
			request {
				method = GET
			}
			response {
				status = OK
			}
		}

		assertThrows<IllegalStateException> {
			Contract.assertContract(contract)
		}.also {
			assertThat(it.message).contains("URL is missing for HTTP contract")
		}
	}

	@Test
	fun `should fail when no status is present`() {
		val contract = contract {
			request {
				url = url("/foo")
				method = GET
			}
			response {
			}
		}

		assertThrows<IllegalStateException> {
			Contract.assertContract(contract)
		}.also {
			assertThat(it.message).contains("Status is missing for HTTP contract")
		}
	}

	@Test
	fun `should work for messaging`() {
		val contract = contract {
			input {
				messageFrom = messageFrom("input")
				messageBody = messageBody("foo" to "bar")
				headers {
					header {
						name = "foo"
						value = "bar"
					}
				}
			}
			outputMessage {
				sentTo = sentTo("output")
				body = body("foo2" to "bar")
				headers {
					header {
						name = "foo2"
						value = "bar"
					}
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val input = contract.input
			assertThat(input.messageFrom.clientValue).isEqualTo("input")
			assertThat(input.messageFrom.serverValue).isEqualTo("input")
			assertThat(input.messageBody.clientValue).isEqualTo(mapOf("foo" to "bar"))
			assertThat(input.messageBody.serverValue).isEqualTo(mapOf("foo" to "bar"))
			val headers = input.messageHeaders.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("foo")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("bar")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("bar")
		}.also {
			val output = contract.outputMessage
			assertThat(output.sentTo.clientValue).isEqualTo("output")
			assertThat(output.sentTo.serverValue).isEqualTo("output")
			assertThat(output.body.clientValue).isEqualTo("foo2" to "bar")
			assertThat(output.body.serverValue).isEqualTo("foo2" to "bar")
			val headers = output.headers.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("foo2")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("bar")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("bar")
		}
	}

	@Test
	fun `should work for messaging with pattern properties`() {
		val contract = contract {
			input {
				messageFrom("input")
				messageBody("foo" to anyNonBlankString())
				headers {
					header {
						name = "foo"
						value = anyNumber()
					}
				}
			}
			outputMessage {
				sentTo("output")
				body("foo2" to anyNonEmptyString())
				headers {
					header {
						name = "foo2"
						value = anyIpAddress()
					}
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}
	}

	@Test
	fun `should set a description`() {
		val contract = contract {
			description = """
given:
	An input
when:
	Sth happens
then:
	Output
"""
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.description).isEqualTo("""
given:
	An input
when:
	Sth happens
then:
	Output
""")
		}
	}

	@Test
	fun `should set a name`() {
		val contract = contract {
			name = "some_special_name"
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.name).isEqualTo("some_special_name")
		}
	}

	@Test
	fun `should mark a contract ignored`() {
		val contract = contract {
			ignored = true
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.ignored).isTrue()
		}
	}

	@Test
	fun `should mark a contract in progress`() {
		val contract = contract {
			inProgress = true
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.inProgress).isTrue()
		}
	}

	@Test
	fun `should make equals and hashcode work properly for URL`() {
		val a: Contract = contract {
			request {
				method = GET
				url = url("/1")
			}
		}
		val b: Contract = contract {
			request {
				method = GET
				url = url("/1")
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}.also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should make equals and hashcode work properly for URL with consumer producer`() {
		val a: Contract = contract {
			request {
				method = GET
				url = url(c("/1"), p("/1"))
			}
		}
		val b: Contract = contract {
			request {
				method = GET
				url = url(c("/1"), p("/1"))
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}.also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should return true when comparing two equal contracts with interpolated string`() {
		val index = 1
		val a: Contract = contract {
			request {
				method = PUT
				headers {
					contentType = APPLICATION_JSON
				}
				url = url("/$index")
			}
			response {
				status = OK
			}
		}
		val b: Contract = contract {
			request {
				method = PUT
				headers {
					contentType = APPLICATION_JSON
				}
				url = url("/$index")
			}
			response {
				status = OK
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}.also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should return false when comparing two unequal contracts with interpolated string`() {
		var index = 1
		val a: Contract = contract {
			request {
				method = PUT
				headers {
					contentType = APPLICATION_JSON
				}
				url = url("/$index")
			}
			response {
				status = OK
			}
		}
		index = 2
		val b: Contract = contract {
			request {
				method = PUT
				headers {
					contentType = APPLICATION_JSON
				}
				url = url("/$index")
			}
			response {
				status = OK
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}.also {
			assertThat(a).isNotEqualTo(b)
		}
	}

	@Test
	fun `should return true when comparing two equal complex contracts`() {
		val a: Contract = contract {
			request {
				method = GET
				url = url("/path")
				headers {
					header {
						name = "Accept"
						value = value(
							consumer(regex("text/.*")),
							producer("text/plain")
						)
					}
					header {
						name = "X-Custom-Header"
						value = value(
							consumer(regex("^.*2134.*$")),
							producer("121345")
						)
					}
				}
			}
			response {
				status = OK
				body = body("id" to mapOf("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					header {
						name = "Content-Type"
						value = "text/plain"
					}
				}
			}
		}
		val b: Contract = contract {
			request {
				method = GET
				url = url("/path")
				headers {
					header {
						name = "Accept"
						value = value(
								consumer(regex("text/.*")),
								producer("text/plain")
						)
					}
					header {
						name = "X-Custom-Header"
						value = value(
								consumer(regex("^.*2134.*$")),
								producer("121345")
						)
					}
				}
			}
			response {
				status = OK
				body = body("id" to mapOf("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					header {
						name = "Content-Type"
						value = "text/plain"
					}
				}
			}
		}
		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}.also {
			assertThat(a).isEqualTo(b)
		}. also {
			val request = a.request
			assertThat(request.url.clientValue).isEqualTo("/path")
			assertThat(request.url.serverValue).isEqualTo("/path")
			assertThat(request.method.clientValue).isEqualTo("GET")
			assertThat(request.method.serverValue).isEqualTo("GET")
			val headers = request.headers.entries
			assertThat(headers).hasSize(2)
			assertThat(headers.elementAt(0).name).isEqualTo("Accept")
			assertThat(headers.elementAt(0).clientValue).isInstanceOf(RegexProperty::class.java)
			assertThat((headers.elementAt(0).clientValue as RegexProperty).pattern()).isEqualTo("text/.*")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("text/plain")
			assertThat(headers.elementAt(1).name).isEqualTo("X-Custom-Header")
			assertThat(headers.elementAt(1).clientValue).isInstanceOf(RegexProperty::class.java)
			assertThat((headers.elementAt(1).clientValue as RegexProperty).pattern()).isEqualTo("^.*2134.*\$")
			assertThat(headers.elementAt(1).serverValue).isEqualTo("121345")
		}.also {
			val response = a.response
			assertThat(response.status.clientValue).isEqualTo(200)
			assertThat(response.status.serverValue).isEqualTo(200)
			val headers = response.headers.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("Content-Type")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("text/plain")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("text/plain")
			assertThat(response.body.clientValue).isEqualTo(mapOf("id" to mapOf("value" to "132"),
					"surname" to "Kowalsky",
					"name" to "Jan",
					"created" to "2014-02-02 12:23:43"
			))
			assertThat(response.body.serverValue).isEqualTo(mapOf("id" to mapOf("value" to "132"),
					"surname" to "Kowalsky",
					"name" to "Jan",
					"created" to "2014-02-02 12:23:43"
			))
		}
	}

	@Test
	fun `should support bodyMatchers`() {
		val contract = contract {
			request {
				method = GET
				url = url("/path")
				body = body("id" to mapOf("value" to "132"))
				bodyMatchers {
					jsonPath {
						path = "$.id.value"
						matcher = byRegex(anInteger())
					}
				}
			}
			response {
				status = OK
				body = body("id" to mapOf("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					contentType = APPLICATION_JSON
				}
				bodyMatchers {
					jsonPath {
						path = "$.id.value"
						matcher = byTimestamp()
					}
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.url.clientValue).isEqualTo("/path")
			assertThat(request.url.serverValue).isEqualTo("/path")
			assertThat(request.method.clientValue).isEqualTo("GET")
			assertThat(request.method.serverValue).isEqualTo("GET")
			assertThat(request.bodyMatchers.hasMatchers()).isTrue()
			val matchers = request.bodyMatchers.matchers()
			assertThat(matchers[0].path()).isEqualTo("$.id.value")
			assertThat(matchers[0].matchingType()).isEqualTo(MatchingType.REGEX)
		}.also {
			val response = contract.response
			assertThat(response.status.clientValue).isEqualTo(200)
			assertThat(response.status.serverValue).isEqualTo(200)
			assertThat(response.bodyMatchers.hasMatchers()).isTrue()
			val matchers = response.bodyMatchers.matchers()
			assertThat(matchers[0].path()).isEqualTo("$.id.value")
			assertThat(matchers[0].matchingType()).isEqualTo(MatchingType.TIMESTAMP)
		}
	}

	@Test
	fun `should throw error when body matcher is not configured correctly`() {
		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					bodyMatchers {
						jsonPath {
							matcher = byRegex(anInteger())
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Body matcher is missing its path or matcher")
		}

		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					bodyMatchers {
						jsonPath {
							path = "$.id.value"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Body matcher is missing its path or matcher")
		}
		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					bodyMatchers {
						xPath {
							matcher = byRegex(anInteger())
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Body matcher is missing its path or matcher")
		}

		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					bodyMatchers {
						xPath {
							path = "$.id.value"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Body matcher is missing its path or matcher")
		}
	}

	@Test
	fun `should support query parameters for url`() {
		val contract = contract {
			request {
				method = GET
				url = url("/path") withQueryParameters {
					parameter("foo", "bar")
				}
			}
			response {
				status = OK
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.url.clientValue).isEqualTo("/path")
			val queryParameters = request.url.queryParameters.parameters
			assertThat(queryParameters[0].name).isEqualTo("foo")
			assertThat(queryParameters[0].clientValue).isEqualTo("bar")
			assertThat(queryParameters[0].serverValue).isEqualTo("bar")
			assertThat(request.url.serverValue).isEqualTo("/path")
			assertThat(request.method.clientValue).isEqualTo("GET")
			assertThat(request.method.serverValue).isEqualTo("GET")
		}.also {
			val response = contract.response
			assertThat(response.status.clientValue).isEqualTo(200)
			assertThat(response.status.serverValue).isEqualTo(200)
		}
	}

	@Test
	fun `should support query parameters for url path`() {
		val contract = contract {
			request {
				method = GET
				urlPath = path("/path") withQueryParameters {
					parameter("foo", "bar")
				}
			}
			response {
				status = OK
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.urlPath.clientValue).isEqualTo("/path")
			assertThat(request.urlPath.serverValue).isEqualTo("/path")
			val queryParameters = request.urlPath.queryParameters.parameters
			assertThat(queryParameters[0].name).isEqualTo("foo")
			assertThat(queryParameters[0].clientValue).isEqualTo("bar")
			assertThat(queryParameters[0].serverValue).isEqualTo("bar")
			assertThat(request.method.clientValue).isEqualTo("GET")
			assertThat(request.method.serverValue).isEqualTo("GET")
		}.also {
			val response = contract.response
			assertThat(response.status.clientValue).isEqualTo(200)
			assertThat(response.status.serverValue).isEqualTo(200)
		}
	}

	@Test
	fun `should work with list as body`() {
		val contract = contract {
			request {
				method = PUT
				url = url("/path")
				body = body(listOf("foo", "bar"))
			}
			response {
				status = OK
				body = body(listOf("foo2", "bar2"))
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.body.clientValue).isEqualTo(listOf("foo", "bar"))
			assertThat(request.body.serverValue).isEqualTo(listOf("foo", "bar"))
		}.also {
			val response = contract.response
			assertThat(response.body.clientValue).isEqualTo(listOf("foo2", "bar2"))
			assertThat(response.body.serverValue).isEqualTo(listOf("foo2", "bar2"))
		}
	}

	@Test
	fun `should throw error when header is not configured correctly`() {
		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					headers {
						header {
							name = "foo"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Header is missing its name or value")
		}

		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					headers {
						header {
							value = "bar"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Header is missing its name or value")
		}
	}

	@Test
	fun `should work with cookies`() {
		val contract = contract {
			request {
				method = GET
				url = url("/cookie")
				cookies {
					cookie {
						name = "name"
						value = "foo"
					}
					cookie {
						name = "name2"
						value = "bar"
					}
				}
			}
			response {
				status = OK
				cookies {
					cookie {
						name = "name"
						value = "foo"
					}
					cookie {
						name = "name2"
						value = "bar"
					}
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			val cookies = request.cookies.entries
			assertThat(cookies).hasSize(2)
			assertThat(cookies).containsExactlyInAnyOrder(Cookie.build("name", "foo"), Cookie.build("name2", "bar"))
		}.also {
			val response = contract.response
			val cookies = response.cookies.entries
			assertThat(cookies).hasSize(2)
			assertThat(cookies).containsExactlyInAnyOrder(Cookie.build("name", "foo"), Cookie.build("name2", "bar"))
		}
	}

	@Test
	fun `should throw error when cookie is not configured correctly`() {
		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					cookies {
						cookie {
							value = "bar"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Cookie is missing its name or value")
		}

		assertThrows<IllegalStateException> {
			contract {
				request {
					method = GET
					url = url("/cookie")
					cookies {
						cookie {
							name = "foo"
						}
					}
				}
				response {
					status = OK
				}
			}
		}.also {
			assertThat(it.message).contains("Cookie is missing its name or value")
		}
	}

	@Test
	fun `should support fromRequest`() {
		val contract = contract {
			request {
				method = GET
				url = url("/path")
				body = body("id" to mapOf("value" to "132"))
				headers {
					accept = APPLICATION_JSON
				}
			}
			response {
				status = OK
				body = body("value is ${fromRequest().body("$.value")}")
				headers {
					contentType = fromRequest().header(ACCEPT)
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val response = contract.response
			assertThat(response.body.clientValue).isEqualTo("value is {{{jsonPath request.body '$.value'}}}")
			val headers = response.headers.entries
			assertThat(headers).hasSize(1)
			assertThat(headers.elementAt(0).name).isEqualTo("Content-Type")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("{{{request.headers.Accept.[0]}}}")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("{{{request.headers.Accept.[0]}}}")
		}
	}

}