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
import org.springframework.cloud.contract.spec.Contract.make
import org.springframework.cloud.contract.spec.internal.DslProperty

/**
 * Tests written based on the Java contract tests written in Groovy
 *
 * @author Tim Ysewyn
 */
class ContractTests {

	@Test
	fun `should work for http`() {
		val contract = ContractDsl.make {
			request {
				url("/foo")
				method("PUT")
				headers {
					header("foo", "bar")
				}
				body("foo" to "bar")
			}
			response {
				status(200)
				headers {
					header("foo2", "bar")
				}
				body("foo2" to "bar")
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}
	}

	@Test
	fun `should fail when no method is present`() {
		val contract = ContractDsl.make {
			request {
				url("/foo")
			}
			response {
				status(200)
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
		val contract = ContractDsl.make {
			request {
				method("GET")
			}
			response {
				status(200)
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
		val contract = ContractDsl.make {
			request {
				url("/foo")
				method("GET")
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
		val contract = ContractDsl.make {
			input {
				messageFrom("input")
				messageBody("foo" to "bar")
				messageHeaders {
					header("foo", "bar")
				}
			}
			outputMessage {
				sentTo("output")
				body("foo2" to "bar")
				headers {
					header("foo2", "bar")
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}
	}

	@Test
	fun `should work for messaging with pattern properties`() {
		val contract = ContractDsl.make {
			input {
				messageFrom("input")
				messageBody("foo" to anyNonBlankString())
				messageHeaders {
					header("foo", anyNumber())
				}
			}
			outputMessage {
				sentTo("output")
				body("foo2" to anyNonEmptyString())
				headers {
					header("foo2", anyIpAddress())
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}
	}

	@Test
	fun `should set a description`() {
		val contract = ContractDsl.make {
			description("""
given:
	An input
when:
	Sth happens
then:
	Output
""")
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
		val contract = ContractDsl.make {
			name("some_special_name")
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.name).isEqualTo("some_special_name")
		}
	}

	@Test
	fun `should mark a contract ignored`() {
		val contract = ContractDsl.make {
			ignored()
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}.also {
			assertThat(contract.ignored).isTrue()
		}
	}

	@Test
	fun `should make equals and hashcode work properly for URL`() {
		val a: Contract = ContractDsl.make {
			request {
				method("GET")
				url("/1")
			}
		}
		val b: Contract = ContractDsl.make {
			request {
				method("GET")
				url("/1")
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}. also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should make equals and hashcode work properly for URL with consumer producer`() {
		val a: Contract = ContractDsl.make {
			request {
				method("GET")
				url(value(c("/1"), p("/1")))
			}
		}
		val b: Contract = ContractDsl.make {
			request {
				method("GET")
				url(value(c("/1"), p("/1")))
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}. also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should return true when comparing two equal contracts with interpolated string`() {
		val index = 1
		val a: Contract = ContractDsl.make {
			request {
				method(PUT())
				headers {
					contentType(applicationJson())
				}
				url( "/$index")
			}
			response {
				status(OK())
			}
		}
		val b: Contract = ContractDsl.make {
			request {
				method(PUT())
				headers {
					contentType(applicationJson())
				}
				url("/$index")
			}
			response {
				status(OK())
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}. also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should return false when comparing two unequal contracts with interpolated string`() {
		var index = 1
		val a: Contract = ContractDsl.make {
			request {
				method(PUT())
				headers {
					contentType(applicationJson())
				}
				url( "/$index")
			}
			response {
				status(OK())
			}
		}
		index = 2
		val b: Contract = ContractDsl.make {
			request {
				method(PUT())
				headers {
					contentType(applicationJson())
				}
				url("/$index")
			}
			response {
				status(OK())
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}. also {
			assertThat(a).isNotEqualTo(b)
		}
	}

	@Test
	fun `should return true when comparing two equal complex contracts`() {
		val a: Contract = ContractDsl.make {
			request {
				method(GET())
				url("/path")
				headers {
					header("Accept", value(
							consumer(regex("text/.*")),
							producer("text/plain")
					))
					header("X-Custom-Header", value(
							consumer(regex("^.*2134.*$")),
							producer("121345")
					))
				}
			}
			response {
				status(OK())
				body("id" to ("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					header("Content-Type", "text/plain")
				}
			}
		}
		val b: Contract = ContractDsl.make {
			request {
				method(GET())
				url("/path")
				headers {
					header("Accept", value(
							consumer(regex("text/.*")),
							producer("text/plain")
					))
					header("X-Custom-Header", value(
							consumer(regex("^.*2134.*$")),
							producer("121345")
					))
				}
			}
			response {
				status(OK())
				body("id" to ("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					header("Content-Type", "text/plain")
				}
			}
		}
		assertDoesNotThrow {
			Contract.assertContract(a)
			Contract.assertContract(b)
		}. also {
			assertThat(a).isEqualTo(b)
		}
	}

	@Test
	fun `should support bodyMatchers`() {
		val contract = ContractDsl.make {
			request {
				method(GET())
				url("/path")
				body("id" to ("value" to "132"))
				bodyMatchers {
					jsonPath("$.id.value", byRegex(anInteger()))
				}
			}
			response {
				status(OK())
				body("id" to ("value" to "132"),
						"surname" to "Kowalsky",
						"name" to "Jan",
						"created" to "2014-02-02 12:23:43"
				)
				headers {
					contentType(applicationJson())
				}
				bodyMatchers {
					jsonPath("$.created", byTimestamp())
				}
			}
		}

		assertDoesNotThrow {
			Contract.assertContract(contract)
		}. also {
			assertThat(contract.request.bodyMatchers.hasMatchers()).isTrue()
			assertThat(contract.response.bodyMatchers.hasMatchers()).isTrue()
		}
	}
}