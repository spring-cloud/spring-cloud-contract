/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.dsl.WireMockStubVerifier
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern
/**
 * @author Jakub Kubrynski, codearte.io
 */
class MockMvcMethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(assertJsonSize: true)

	@Shared
	// tag::contract_with_regex[]
	Contract dslWithOptionalsInString = Contract.make {
		priority 1
		request {
			method 'POST'
			url '/users/password'
			headers {
				header 'Content-Type': 'application/json'
			}
			body(
					email: $(consumer(optional(regex(email()))), producer('abc@abc.com')),
					callback_url: $(consumer(regex(hostname())), producer('http://partners.com'))
			)
		}
		response {
			status 404
			headers {
				header 'Content-Type': 'application/json'
			}
			body(
					code: value(consumer("123123"), producer(optional("123123"))),
					message: "User not found by email = [${value(producer(regex(email())), consumer('not.existing@user.com'))}]"
			)
		}
	}
	// end::contract_with_regex[]

	@Shared
	Contract dslWithOptionals = Contract.make {
		priority 1
		request {
			method 'POST'
			url '/users/password'
			headers {
				header 'Content-Type': 'application/json'
			}
			body(
					""" {
								"email" : "${
						value(consumer(optional(regex(email()))), producer('abc@abc.com'))
					}",
								"callback_url" : "${
						value(consumer(regex(hostname())), producer('http://partners.com'))
					}"
								}
							"""
			)
		}
		response {
			status 404
			headers {
				header 'Content-Type': 'application/json'
			}
			body(
					""" {
								"code" : "${value(consumer(123123), producer(optional(123123)))}",
								"message" : "User not found by email = [${
						value(producer(regex(email())), consumer('not.existing@user.com'))
					}]"
								}
							"""
			)
		}
	}

	def "should generate assertions for simple response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body """{
	"property1": "a",
	"property2": "b"
}"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("a")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").isEqualTo("b")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#187")
	def "should generate assertions for null and boolean values with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body """{
	"property1": "true",
	"property2": null,
	"property3": false
}"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("true")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").isNull()""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property3").isEqualTo(false)""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#79")
	def "should generate assertions for simple response body constructed from map with a list with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body(
						property1: 'a',
						property2: [
								[a: 'sth'],
								[b: 'sthElse']
						]
				)
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("a")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property2").contains("a").isEqualTo("sth")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property2").hasSize(2)""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property2").contains("b").isEqualTo("sthElse")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#82")
	def "should generate proper request when body constructed from map with a list #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
				body(
						items: ['HOP']
				)
			}
			response {
				status 200
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains(bodyString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | """.body('''{\"items\":[\"HOP\"]}''')"""
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '.body("{\\"items\\":[\\"HOP\\"]}")'
	}

	@Issue("#88")
	def "should generate proper request when body constructed from GString with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
				body(
						"property1=VAL1"
				)
			}
			response {
				status 200
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains(bodyString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | """.body('''property1=VAL1''')"""
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '.body("\\"property1=VAL1\\"")'
	}

	@Issue("185")
	def "should generate assertions for a response body containing map with integers as keys with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body(
						property: [
								14: 0.0,
								7 : 0.0
						]
				)
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property").field(7).isEqualTo(0.0)""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property").field(14).isEqualTo(0.0)""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate assertions for array in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body """[
{
	"property1": "a"
},
{
	"property2": "b"
}]"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("property2").isEqualTo("b")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("property1").isEqualTo("a")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate assertions for array inside response body element with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body """{
	"property1": [
	{ "property2": "test1"},
	{ "property3": "test2"}
	]
}"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property1").contains("property2").isEqualTo("test1")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property1").contains("property3").isEqualTo("test2")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate assertions for nested objects in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body '''\
{
	"property1": "a",
	"property2": {"property3": "b"}
}
'''
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").field("property3").isEqualTo("b")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("a")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate regex assertions for map objects in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body(
						property1: "a",
						property2: value(
								consumer('123'),
								producer(regex('[0-9]{3}'))
						)
				)
				headers {
					header('Content-Type': 'application/json')
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").matches("[0-9]{3}")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("a")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate regex assertions for string objects in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body("""{"property1":"a","property2":"${
					value(consumer('123'), producer(regex('[0-9]{3}')))
				}"}""")
				headers {
					header('Content-Type': 'application/json')
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").matches("[0-9]{3}")""")
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("a")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue(["#126", "#143"])
	def "should generate escaped regex assertions for string objects in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				url "test"
			}
			response {
				status 200
				body("""{"property":"  ${
					value(consumer('123'), producer(regex('\\d+')))
				}"}""")
				headers {
					header('Content-Type': 'application/json')
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property").matches("\\\\d+")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate a call with an url path and query parameters with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath('/users') {
					queryParameters {
						parameter 'limit': $(consumer(equalTo("20")), producer(equalTo("10")))
						parameter 'offset': $(consumer(containing("20")), producer(equalTo("20")))
						parameter 'filter': "email"
						parameter 'sort': equalTo("name")
						parameter 'search': $(consumer(notMatching(~/^\/[0-9]{2}$/)), producer("55"))
						parameter 'age': $(consumer(notMatching("^\\w*\$")), producer("99"))
						parameter 'name': $(consumer(matching("Denis.*")), producer("Denis.Stepanov"))
						parameter 'email': "bob@email.com"
						parameter 'hello': $(consumer(matching("Denis.*")), producer(absent()))
						parameter 'hello': absent()
					}
				}
			}
			response {
				status 200
				body """
					{
						"property1": "a",
						"property2": "b"
					}
					"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('get("/users?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
		test.contains('assertThatJson(parsedJson).field("property1").isEqualTo("a")')
		test.contains('assertThatJson(parsedJson).field("property2").isEqualTo("b")')
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#169')
	def "should generate a call with an url path and query parameters with url containing a pattern with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				url($(consumer(regex('/foo/[0-9]+')), producer('/foo/123456'))) {
					queryParameters {
						parameter 'limit': $(consumer(equalTo("20")), producer(equalTo("10")))
						parameter 'offset': $(consumer(containing("20")), producer(equalTo("20")))
						parameter 'filter': "email"
						parameter 'sort': equalTo("name")
						parameter 'search': $(consumer(notMatching(~/^\/[0-9]{2}$/)), producer("55"))
						parameter 'age': $(consumer(notMatching("^\\w*\$")), producer("99"))
						parameter 'name': $(consumer(matching("Denis.*")), producer("Denis.Stepanov"))
						parameter 'email': "bob@email.com"
						parameter 'hello': $(consumer(matching("Denis.*")), producer(absent()))
						parameter 'hello': absent()
					}
				}
			}
			response {
				status 200
				body """
					{
						"property1": "a",
						"property2": "b"
					}
					"""
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('get("/foo/123456?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
		test.contains('assertThatJson(parsedJson).field("property1").isEqualTo("a")')
		test.contains('assertThatJson(parsedJson).field("property2").isEqualTo("b")')
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate test for empty body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method('POST')
				url("/ws/payments")
				body("")
			}
			response {
				status 406
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(bodyString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | ".body('''''')"
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | ".body(\"\\\"\\\"\")"
	}

	def "should generate test for String in response body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "POST"
				url "test"
			}
			response {
				status 200
				body "test"
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(bodyDefinitionString)
		test.contains(bodyEvaluationString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyDefinitionString                                     | bodyEvaluationString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | 'def responseBody = (response.body.asString())'          | 'responseBody == "test"'
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | 'Object responseBody = (response.getBody().asString());' | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('113')
	def "should generate regex test for String in response header with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'POST'
				url $(consumer(regex('/partners/[0-9]+/users')), producer('/partners/1000/users'))
				headers { header 'Content-Type': 'application/json' }
				body(
						first_name: 'John',
						last_name: 'Smith',
						personal_id: '12345678901',
						phone_number: '500500500',
						invitation_token: '00fec7141bb94793bfe7ae1d0f39bda0',
						password: 'john'
				)
			}
			response {
				status 201
				headers {
					header 'Location': $(consumer('http://localhost/partners/1000/users/1001'), producer(regex('http://localhost/partners/[0-9]+/users/[0-9]+')))
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(headerEvaluationString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | headerEvaluationString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '''response.header('Location') ==~ java.util.regex.Pattern.compile('http://localhost/partners/[0-9]+/users/[0-9]+')'''
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
	}

	@Issue('115')
	def "should generate regex with helper method with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'POST'
				url $(consumer(regex('/partners/[0-9]+/users')), producer('/partners/1000/users'))
				headers { header 'Content-Type': 'application/json' }
				body(
						first_name: 'John',
						last_name: 'Smith',
						personal_id: '12345678901',
						phone_number: '500500500',
						invitation_token: '00fec7141bb94793bfe7ae1d0f39bda0',
						password: 'john'
				)
			}
			response {
				status 201
				headers {
					header 'Location': $(consumer('http://localhost/partners/1000/users/1001'), producer(regex("^${hostname()}/partners/[0-9]+/users/[0-9]+")))
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(headerEvaluationString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | headerEvaluationString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '''response.header('Location') ==~ java.util.regex.Pattern.compile('^((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?/partners/[0-9]+/users/[0-9]+')'''
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | 'assertThat(response.header("Location")).matches("^((http[s]?|ftp):/)/?([^:/s]+)(:[0-9]{1,5})?/partners/[0-9]+/users/[0-9]+");'
	}

	def "should work with more complex stuff and jsonpaths with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			priority 10
			request {
				method 'POST'
				url '/validation/client'
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						bank_account_number: '0014282912345698765432161182',
						email: 'foo@bar.com',
						phone_number: '100299300',
						personal_id: 'ABC123456'
				)
			}

			response {
				status 200
				body(errors: [
						[property: "bank_account_number", message: "incorrect_format"]
				])
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains("""assertThatJson(parsedJson).array("errors").contains("property").isEqualTo("bank_account_number")""")
		test.contains("""assertThatJson(parsedJson).array("errors").contains("message").isEqualTo("incorrect_format")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should work properly with GString url with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {

			request {
				method 'PUT'
				url "/partners/${value(consumer(regex('^[0-9]*$')), producer('11'))}/agents/11/customers/09665703Z"
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						first_name: 'Josef',
				)
			}
			response {
				status 422
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('''/partners/11/agents/11/customers/09665703Z''')
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should resolve properties in GString with regular expression with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			priority 1
			request {
				method 'POST'
				url '/users/password'
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						email: $(consumer(regex(email())), producer('not.existing@user.com')),
						callback_url: $(consumer(regex(hostname())), producer('http://partners.com'))
				)
			}
			response {
				status 404
				headers {
					header 'Content-Type': 'application/json'
				}
				body(
						code: 4,
						message: "User not found by email = [${value(producer(regex(email())), consumer('not.existing@user.com'))}]"
				)
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains("""assertThatJson(parsedJson).field("message").matches("User not found by email = \\\\\\\\[[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\\\\\.[a-zA-Z]{2,4}\\\\\\\\]")""")
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('42')
	def "should not omit the optional field in the test creation with MockMvcSpockMethodBodyBuilder"() {
		given:
		MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('''"email":"abc@abc.com"''')
		test.contains("""assertThatJson(parsedJson).field("code").matches("(123123)?")""")
		!test.contains('''REGEXP''')
		!test.contains('''OPTIONAL''')
		!test.contains('''OptionalProperty''')
		where:
		contractDsl << [dslWithOptionals, dslWithOptionalsInString]
	}

	@Issue('42')
	def "should not omit the optional field in the test creation with MockMvcJUnitMethodBodyBuilder"() {
		given:
		MethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl, properties)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('\\"email\\":\\"abc@abc.com\\"')
		test.contains('assertThatJson(parsedJson).field("code").matches("(123123)?");')
		!test.contains('''REGEXP''')
		!test.contains('''OPTIONAL''')
		!test.contains('''OptionalProperty''')
		where:
		contractDsl << [dslWithOptionals, dslWithOptionalsInString]
	}

	@Issue('72')
	def "should make the execute method work with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method """PUT"""
				url """/fraudcheck"""
				body("""
                        {
                        "clientPesel":"${
					value(consumer(regex('[0-9]{10}')), producer('1234567890'))
				}",
                        "loanAmount":123.123
                        }
                    """
				)
				headers {
					header("""Content-Type""", """application/vnd.fraud.v1+json""")
				}

			}
			response {
				status 200
				body("""{
    "fraudCheckStatus": "OK",
    "rejectionReason": ${
					value(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
				}
}""")
				headers {
					header('Content-Type': 'application/vnd.fraud.v1+json')
					header 'Location': value(
							consumer(null),
							producer(execute('assertThatLocationIsNull($it)'))
					)
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		String test = blockBuilder.toString()
		then:
		assertionStrings.each { String assertionString ->
			assert test.contains(assertionString)
		}
		where:
		methodBuilderName           | methodBuilder                                                               | assertionStrings
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | ['''assertThatRejectionReasonIsNull(parsedJson.read('$.rejectionReason'))''', '''assertThatLocationIsNull(response.header('Location'))''']
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
	}

	def "should support inner map and list definitions with #methodBuilderName"() {
		given:

		Pattern PHONE_NUMBER = Pattern.compile(/[+\w]*/)
		Pattern ANYSTRING = Pattern.compile(/.*/)
		Pattern NUMBERS = Pattern.compile(/[\d\.]*/)
		Pattern DATETIME = ANYSTRING

		Contract contractDsl = Contract.make {
			request {
				method "PUT"
				url "/v1/payments/e86df6f693de4b35ae648464c5b0dc09/client_data"
				headers {
					header('Content-Type': 'application/json')
				}
				body(
						client: [
								first_name   : $(consumer(regex(onlyAlphaUnicode())), producer('Denis')),
								last_name    : $(consumer(regex(onlyAlphaUnicode())), producer('FakeName')),
								email        : $(consumer(regex(email())), producer('fakemail@fakegmail.com')),
								fax          : $(consumer(PHONE_NUMBER), producer('+xx001213214')),
								phone        : $(consumer(PHONE_NUMBER), producer('2223311')),
								data_of_birth: $(consumer(DATETIME), producer('2002-10-22T00:00:00Z'))
						],
						client_id_card: [
								id           : $(consumer(ANYSTRING), producer('ABC12345')),
								date_of_issue: $(consumer(ANYSTRING), producer('2002-10-02T00:00:00Z')),
								address      : [
										street : $(consumer(ANYSTRING), producer('Light Street')),
										city   : $(consumer(ANYSTRING), producer('Fire')),
										region : $(consumer(ANYSTRING), producer('Skys')),
										country: $(consumer(ANYSTRING), producer('HG')),
										zip    : $(consumer(NUMBERS), producer('658965'))
								]
						],
						incomes_and_expenses: [
								monthly_income         : $(consumer(NUMBERS), producer('0.0')),
								monthly_loan_repayments: $(consumer(NUMBERS), producer('100')),
								monthly_living_expenses: $(consumer(NUMBERS), producer('22'))
						],
						additional_info: [
								allow_to_contact: $(consumer(optional(regex(anyBoolean()))), producer('true'))
						]
				)
			}
			response {
				status 200
				headers {
					header('Content-Type': 'application/json')
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains bodyString
		!test.contains("clientValue")
		!test.contains("cursor")
		where:
		methodBuilderName           | methodBuilder                                                               | bodyString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '"street":"Light Street"'
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '\\"street\\":\\"Light Street\\"'

	}

	def "shouldn't generate unicode escape characters with #methodBuilderName"() {
		given:
		Pattern ONLY_ALPHA_UNICODE = Pattern.compile(/[\p{L}]*/)

		Contract contractDsl = Contract.make {
			request {
				method "PUT"
				url "/v1/payments/e86df6f693de4b35ae648464c5b0dc09/енев"
				headers {
					header('Content-Type': 'application/json')
				}
				body(
						client: [
								first_name: $(consumer(ONLY_ALPHA_UNICODE), producer('Пенева')),
								last_name : $(consumer(ONLY_ALPHA_UNICODE), producer('Пенева'))
						]
				)
			}
			response {
				status 200
				headers {
					header('Content-Type': 'application/json')
				}
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		!test.contains("\\u041f")
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('177')
	def "should generate proper test code when having multiline body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "PUT"
				url "/multiline"
				body('''hello,
World.''')
			}
			response {
				status 200
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.given(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(bodyString)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | """'''hello,
World.'''"""
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '\\"hello,\\nWorld.\\"'
	}

	@Issue('180')
	def "should generate proper test code when having multipart parameters with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "PUT"
				url "/multipart"
				headers {
					header('content-type', 'multipart/form-data;boundary=AaB03x')
				}
				multipart(
						formParameter: value(consumer(regex('.+')), producer('"formParameterValue"')),
						someBooleanParameter: value(consumer(regex('(true|false)')), producer('true')),
						file: named(value(consumer(regex('.+')), producer('filename.csv')), value(consumer(regex('.+')), producer('file content')))
				)
			}
			response {
				status 200
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		for (String requestString : requestStrings) {
			test.contains(requestString)
		}
		where:
		methodBuilderName           | methodBuilder                                                               | requestStrings
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | ["""'content-type', 'multipart/form-data;boundary=AaB03x'""",
																													 """.param('formParameter', '"formParameterValue"'""",
																													 """.param('someBooleanParameter', 'true')""",
																													 """.multiPart('file', 'filename.csv', 'file content'.bytes)"""]
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | ['"content-type", "multipart/form-data;boundary=AaB03x"',
																													 '.param("formParameter", "\\"formParameterValue\\"")',
																													 '.param("someBooleanParameter", "true")',
																													 '.multiPart("file", "filename.csv", "file content".getBytes());']
	}

	@Issue('180')
	def "should generate proper test code when having multipart parameters with named as map with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "PUT"
				url "/multipart"
				multipart(
						formParameter: value(consumer(regex('".+"')), producer('"formParameterValue"')),
						someBooleanParameter: value(consumer(regex('(true|false)')), producer('true')),
						file: named(
								name: value(consumer(regex('.+')), producer('filename.csv')),
								content: value(consumer(regex('.+')), producer('file content')))
				)
			}
			response {
				status 200
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.given(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('.multiPart')
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#216')
	def "should parse JSON with arrays using Spock"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				urlPath('/auth/oauth/check_token') {
					queryParameters {
						parameter 'token':
								value(
										consumer(regex('^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}')),
										producer('6973b31d-7140-402a-bca6-1cdb954e03a7')
								)
					}
				}
			}
			response {
				status 200
				body(
						authorities: [
								value(consumer('ROLE_ADMIN'), producer(regex('^[a-zA-Z0-9_\\- ]+$')))
						]
				)
			}
		}
		MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('''assertThatJson(parsedJson).array("authorities").arrayField().matches("^[a-zA-Z0-9_\\\\- ]+\\$").value()''')
	}

	@Issue('#216')
	def "should parse JSON with arrays using JUnit"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method "GET"
				urlPath('/auth/oauth/check_token') {
					queryParameters {
						parameter 'token':
								value(
										consumer(regex('^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}')),
										producer('6973b31d-7140-402a-bca6-1cdb954e03a7')
								)
					}
				}
			}
			response {
				status 200
				body(
						authorities: [
								value(consumer('ROLE_ADMIN'), producer(regex('^[a-zA-Z0-9_\\- ]+$')))
						]
				)
			}
		}
		MethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl, properties)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains('''assertThatJson(parsedJson).array("authorities").arrayField().matches("^[a-zA-Z0-9_\\\\- ]+$").value()''')
	}

	def "should work with execution property"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'PUT'
				url '/fraudcheck'
			}
			response {
				status 200
				body(
						fraudCheckStatus: "OK",
						rejectionReason: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
				)
			}

		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		!test.contains('''assertThatJson(parsedJson).field("rejectionReason").isEqualTo("assertThatRejectionReasonIsNull("''')
		test.contains('''assertThatRejectionReasonIsNull(''')
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('262')
	def "should generate proper test code with map inside list"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/foos'
				}
				response {
					status 200
					body([[id: value(
							consumer('123'),
							producer(regex('[0-9]+'))
					)], [id: value(
							consumer('567'),
							producer(regex('[0-9]+'))
					)]])
					headers {
						header('Content-Type': 'application/json;charset=UTF-8')
					}
				}
			}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array().contains("id").matches("[0-9]+")')
	}

	@Issue('266')
	def "should generate proper test code with top level array using #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/api/tags'
				}
				response {
					status 200
					body(["Java", "Java8", "Spring", "SpringBoot", "Stream"])
					headers {
						header('Content-Type': 'application/json;charset=UTF-8')
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).hasSize(5)')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		where:
			methodBuilderName           | methodBuilder
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('266')
	def "should generate proper test code with top level array or arrays using #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/api/categories'
				}
				response {
					status 200
					body([["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]])
					headers {
						header('Content-Type': 'application/json;charset=UTF-8')
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).hasSize(2)')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Programming").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Java").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Spring").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Boot").value()')
		where:
			methodBuilderName           | methodBuilder
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

  @Issue('47')
	def "should generate async body when async flag set in response"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				url '/test'
			}
			response {
				status 200
				async()
			}
		}
		MethodBodyBuilder builder = methodBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		test.contains(bodyDefinitionString)
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		where:
		methodBuilderName           | methodBuilder                                                               | bodyDefinitionString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '.when().async()'
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '.when().async()'
	}

	def "should generate proper test code with array of primitives using #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/api/tags'
				}
				response {
					status 200
					body('''{
							  "partners":[
								  {
									"payment_methods":[ "BANK", "CASH" ]
								  }
							   ]
							}
				''')
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array("partners").array("payment_methods").arrayField().isEqualTo("BANK").value()')
			test.contains('assertThatJson(parsedJson).array("partners").array("payment_methods").arrayField().isEqualTo("CASH").value()')
		where:
			methodBuilderName           | methodBuilder
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#273')
	def "should not escape dollar in Spock regex tests"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
				}
				response {
					status 200
					body( code: 9, message: $(consumer('Wrong credentials'), producer(regex('^(?!\\s*$).+'))) )
				}
			}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).field("message").matches("^(?!\\\\s*\\$).+")')
	}

	Contract dslForDocs =
			// tag::dsl_example[]
		org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'PUT'
				url '/api/12'
				headers {
					header 'Content-Type': 'application/vnd.org.springframework.cloud.contract.verifier.twitter-places-analyzer.v1+json'
				}
				body '''\
				[{
					"created_at": "Sat Jul 26 09:38:57 +0000 2014",
					"id": 492967299297845248,
					"id_str": "492967299297845248",
					"text": "Gonna see you at Warsaw",
					"place":
					{
						"attributes":{},
						"bounding_box":
						{
							"coordinates":
								[[
									[-77.119759,38.791645],
									[-76.909393,38.791645],
									[-76.909393,38.995548],
									[-77.119759,38.995548]
								]],
							"type":"Polygon"
						},
						"country":"United States",
						"country_code":"US",
						"full_name":"Washington, DC",
						"id":"01fbe706f872cb32",
						"name":"Washington",
						"place_type":"city",
						"url": "http://api.twitter.com/1/geo/id/01fbe706f872cb32.json"
					}
				}]
			'''
			}
			response {
				status 200
			}
		}
		// end::dsl_example[]

	Contract dslWithOnlyOneSideForDocs =
			// tag::dsl_one_side_data_generation_example[]
		org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'PUT'
				url '/foo'
				body([
					requestElement: value(consumer(regex('[0-9]{5}')))
				])
			}
			response {
				status 200
				body([
					responseElement: value(producer(regex('[0-9]{7}')))
				])
			}
		}
		// end::dsl_one_side_data_generation_example[]

	@Issue('#32')
	def "should generate the regular expression for the other side of communication"() {
		given:
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(
					dslWithOnlyOneSideForDocs, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.given(blockBuilder)
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.replace('\n', '').matches('.*')
			test.replace('\n', '').stripIndent().stripMargin().matches('.*field\\("responseElement"\\).isEqualTo\\("[0-9]{7}.*')
	}

}
