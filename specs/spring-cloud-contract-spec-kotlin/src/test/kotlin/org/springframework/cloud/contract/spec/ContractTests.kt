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

package org.springframework.cloud.contract.spec

import java.io.File

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.KotlinContractConverter
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.NamedProperty
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
					header("foo", "bar")
				}
				body = body("foo" to "bar")
			}
			response {
				status = OK
				headers {
					header("foo2", "bar")
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
	fun `should set a description`() {
		val contract =
// @formatter: off
// tag::description[]
contract {
	description = """
given:
	An input
when:
	Sth happens
then:
	Output
"""
}
// end::description[]
// @formatter: on
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
		val contract =
		// tag::name[]
		contract {
			name = "some_special_name"
		}
		// end::name[]

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.name).isEqualTo("some_special_name")
		}
	}

	@Test
	fun `should mark a contract ignored`() {
		val contract =
		// tag::ignored[]
		contract {
			ignored = true
		}
		// end::ignored[]

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.ignored).isTrue()
		}
	}

	@Test
	fun `should mark a contract in progress`() {
		val contract =
		// tag::in_progress[]
		contract {
			inProgress = true
		}
		// end::in_progress[]

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.inProgress).isTrue()
		}
	}

	@Test
	fun `should set contract metadata`() {
		val contract =
		// tag::metadata[]
		contract {
			metadata("wiremock" to ("stubmapping" to """
		{
		  "response" : {
			"fixedDelayMilliseconds": 2000
		  }
		}"""))
		}
		// end::metadata[]

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.metadata).isNotEmpty
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
					header(name = "Accept",
						value = value(
							consumer(regex("text/.*")),
							producer("text/plain")
						)
					)
					header(name = "X-Custom-Header",
						value = value(
							consumer(regex("^.*2134.*$")),
							producer("121345")
						)
					)
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
					header("Content-Type", "text/plain")
					header("X-Custom-Header",
							value(
									stub("121345"),
									test(regex("^.*2134.*$"))
							)
					)
				}
			}
		}
		val b: Contract = contract {
			request {
				method = GET
				url = url("/path")
				headers {
					header(name = "Accept",
						value = value(
								consumer(regex("text/.*")),
								producer("text/plain")
						)
					)
					header(name = "X-Custom-Header",
						value = value(
								consumer(regex("^.*2134.*$")),
								producer("121345")
						)
					)
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
					header("Content-Type", "text/plain")
					header("X-Custom-Header",
							value(
								stub("121345"),
								test(regex("^.*2134.*$"))
							)
					)
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
			assertThat(headers).hasSize(2)
			assertThat(headers.elementAt(0).name).isEqualTo("Content-Type")
			assertThat(headers.elementAt(0).clientValue).isEqualTo("text/plain")
			assertThat(headers.elementAt(0).serverValue).isEqualTo("text/plain")
			assertThat(headers.elementAt(1).name).isEqualTo("X-Custom-Header")
			assertThat(headers.elementAt(1).clientValue).isEqualTo("121345")
			assertThat(headers.elementAt(1).serverValue).isInstanceOf(RegexProperty::class.java)
			assertThat((headers.elementAt(1).serverValue as RegexProperty).pattern()).isEqualTo("^.*2134.*\$")
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
					jsonPath( "$.id.value", byRegex(anInteger))
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
					jsonPath("$.id.value", byTimestamp)
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
	fun `should work with cookies`() {
		val contract = contract {
			request {
				method = GET
				url = url("/cookie")
				cookies {
					cookie("name", "foo")
					cookie("name2", "bar")
				}
			}
			response {
				status = OK
				cookies {
					cookie("name", "foo")
					cookie("name2", "bar")
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

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `should support multipart`() {
		val contract = KotlinContractConverter()
				.convertFrom(File(javaClass.classLoader.getResource("contracts/multipart.kts")!!.toURI()))
				.elementAt(0)

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val request = contract.request
			assertThat(request.multipart).isNotNull()
			assertThat(request.multipart.clientValue).isNotNull()
			assertThat(request.multipart.clientValue).isInstanceOf(LinkedHashMap::class.java)
			val multipartData = request.multipart.clientValue as LinkedHashMap<String, Any>
			assertThat(multipartData).hasSize(3)
			assertThat(multipartData.keys).containsExactly("file1", "file2", "test")
			var namedProperty: NamedProperty

			assertThat(multipartData["file1"]).isInstanceOf(NamedProperty::class.java)
			namedProperty = multipartData["file1"] as NamedProperty
			assertThat(((namedProperty.filename as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat((namedProperty.filename as DslProperty<Any>).serverValue as String).isEqualTo("filename1")
			assertThat(((namedProperty.body as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat((namedProperty.body as DslProperty<Any>).serverValue as String).isEqualTo("content1")
			assertThat(namedProperty.contentType.serverValue).isNull()
			assertThat(namedProperty.contentType.clientValue).isNull()

			assertThat(multipartData["file2"]).isInstanceOf(NamedProperty::class.java)
			namedProperty = multipartData["file2"] as NamedProperty
			assertThat(((namedProperty.filename as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat((namedProperty.filename as DslProperty<Any>).serverValue as String).isEqualTo("filename2")
			assertThat(((namedProperty.body as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat((namedProperty.body as DslProperty<Any>).serverValue as String).isEqualTo("content2")
			assertThat(namedProperty.contentType.serverValue).isNull()
			assertThat(namedProperty.contentType.clientValue).isNull()

			assertThat(multipartData["test"]).isInstanceOf(NamedProperty::class.java)
			namedProperty = multipartData["test"] as NamedProperty
			assertThat(((namedProperty.filename as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat((namedProperty.filename as DslProperty<Any>).serverValue as String).isEqualTo("filename3")
			assertThat(((namedProperty.body as DslProperty<Any>).clientValue as RegexProperty).pattern()).isEqualTo("[\\S\\s]+")
			assertThat(((namedProperty.body as DslProperty<Any>).serverValue as FromFileProperty).fileName()).isEqualTo("test.json")
			assertThat((namedProperty.contentType as DslProperty<Any>).clientValue).isEqualTo("application/json")
			assertThat((namedProperty.contentType as DslProperty<Any>).serverValue).isEqualTo("application/json")
		}
	}

	@Test
	fun `should use filename as fallback for single unnamed contract`() {
		val contract = KotlinContractConverter()
				.convertFrom(File(javaClass.classLoader.getResource("contracts/unnamed_single.kts")!!.toURI()))
				.single()
		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.name).isEqualTo("unnamed_single")
		}
	}

	@Test
	fun `should use filename with index as fallback for multiple unnamed contracts`() {
		val contracts = KotlinContractConverter()
				.convertFrom(File(javaClass.classLoader.getResource("contracts/unnamed_multiple.kts")!!.toURI()))
				.toList()
		assertDoesNotThrow {
			contracts.forEach(Contract::assertContract)
		}.also {
			assertThat(contracts[0].name).isEqualTo("unnamed_multiple_0")
			assertThat(contracts[1].name).isEqualTo("unnamed_multiple_1")
		}
	}

	@Test
	/**
	 * See issue https://github.com/spring-cloud/spring-cloud-contract/issues/1668
	 */
	fun `should convert delay from long to int`() {
		val contract = contract {
			name = "Test Controller"
			description = "Some description"
			request {
				method = GET
				url = url("/credentials") withQueryParameters {
					parameter("type", "foo")
				}
			}
			response {
				delay = fixedMilliseconds(1000L)
				status = OK
				body = body(
						listOf(
								mapOf(
										"type" to "test1"
								)
						)
				)
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			val response = contract.response
			assertThat(response.delay.clientValue).isInstanceOf(java.lang.Integer::class.java)
			assertThat(response.delay.clientValue).isEqualTo(1000)
			assertThat(response.delay.serverValue).isInstanceOf(java.lang.Integer::class.java)
			assertThat(response.delay.serverValue).isEqualTo(1000)
		}
	}

}
