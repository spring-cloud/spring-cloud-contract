/*
 *  Copyright 2013-2017 the original author or authors.
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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Rule
import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.dsl.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import java.util.regex.Pattern
/**
 * @author Jakub Kubrynski, codearte.io
 */
class MockMvcMethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule OutputCapture capture = new OutputCapture()

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Shared
	// tag::contract_with_regex[]
	Contract dslWithOptionalsInString = Contract.make {
		priority 1
		request {
			method POST()
			url '/users/password'
			headers {
				contentType(applicationJson())
			}
			body(
					email: $(consumer(optional(regex(email()))), producer('abc@abc.com')),
					callback_url: $(consumer(regex(hostname())), producer('http://partners.com'))
			)
		}
		response {
			status 404
			headers {
				contentType(applicationJson())
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
			method POST()
			url '/users/password'
			headers {
				contentType(applicationJson())
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
				contentType(applicationJson())
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
				method GET()
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
				method GET()
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		blockBuilder.toString().contains("""assertThatJson(parsedJson).array("property2").contains("b").isEqualTo("sthElse")""")
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#79")
	@RestoreSystemProperties
		def "should generate assertions for simple response body constructed from map with a list with #methodBuilderName with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		test.contains('''.queryParam("limit","10")''')
		test.contains('''.queryParam("offset","20")''')
		test.contains('''.queryParam("filter","email")''')
		test.contains('''.queryParam("sort","name")''')
		test.contains('''.queryParam("search","55")''')
		test.contains('''.queryParam("age","99")''')
		test.contains('''.queryParam("name","Denis.Stepanov")''')
		test.contains('''.queryParam("email","bob@email.com")''')
		test.contains('''.get("/users")''')
		test.contains('assertThatJson(parsedJson).field("property1").isEqualTo("a")')
		test.contains('assertThatJson(parsedJson).field("property2").isEqualTo("b")')
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		test.contains('''.queryParam("limit","10")''')
		test.contains('''.queryParam("offset","20")''')
		test.contains('''.queryParam("filter","email")''')
		test.contains('''.queryParam("sort","name")''')
		test.contains('''.queryParam("search","55")''')
		test.contains('''.queryParam("age","99")''')
		test.contains('''.queryParam("name","Denis.Stepanov")''')
		test.contains('''.queryParam("email","bob@email.com")''')
		test.contains('''.get("/foo/123456")''')
		test.contains('assertThatJson(parsedJson).field("property1").isEqualTo("a")')
		test.contains('assertThatJson(parsedJson).field("property2").isEqualTo("b")')
		and:
		stubMappingIsValidWireMockStub(contractDsl)
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

		def "should generate test for empty body with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method(POST())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
		methodBuilderName           | methodBuilder                                                               | bodyDefinitionString                                     | bodyEvaluationString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | 'def responseBody = (response.body.asString())'          | 'responseBody == "test"'
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('113')
		def "should generate regex test for String in response header with #methodBuilderName"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'POST'
				url $(consumer(regex('/partners/[0-9]+/users')), producer('/partners/1000/users'))
				headers { contentType(applicationJson()) }
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
				headers { contentType(applicationJson()) }
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
		methodBuilderName           | methodBuilder                                                                           | headerEvaluationString
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '''response.header('Location') ==~ java.util.regex.Pattern.compile('^((http[s]?|ftp):/)/?([^:/s]+)(:[0-9]{1,5})?/partners/[0-9]+/users/[0-9]+')'''
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
		methodBuilderName           | methodBuilder
		"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
		"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

		def "should work properly with GString url with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {

				request {
					method PUT()
					url "/partners/${value(consumer(regex('^[0-9]*$')), producer('11'))}/agents/11/customers/09665703Z"
					headers {
						contentType(applicationJson())
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					method POST()
					url '/users/password'
					headers {
						contentType(applicationJson())
					}
					body(
							email: $(consumer(regex(email())), producer('not.existing@user.com')),
							callback_url: $(consumer(regex(hostname())), producer('http://partners.com'))
					)
				}
				response {
					status 404
					headers {
						contentType(applicationJson())
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
		and:
			// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompileJava(blockBuilder.toString())
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
					contentType(applicationJson())
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					contentType(applicationJson())
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
					contentType(applicationJson())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
					contentType('multipart/form-data;boundary=AaB03x')
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompileJava(blockBuilder.toString())
	}

		def "should work with execution property with #methodBuilderName"() {
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
						contentType(applicationJsonUtf8())
					}
				}
			}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array().contains("id").matches("[0-9]+")')
		and:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName           | methodBuilder
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('266')
	@RestoreSystemProperties
		def "should generate proper test code with top level array using #methodBuilderName with array size check"() {
		given:
			System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).hasSize(5)')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Programming").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Java").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Spring").value()')
			test.contains('assertThatJson(parsedJson).array().arrayField().isEqualTo("Boot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
		and:
		SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array("partners").array("payment_methods").arrayField().isEqualTo("BANK").value()')
			test.contains('assertThatJson(parsedJson).array("partners").array("payment_methods").arrayField().isEqualTo("CASH").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).field("message").matches("^(?!\\\\s*\\$).+")')
		and:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString(), false)
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
				url value(consumer(regex('/foo/[0-9]{5}')))
				body([
					requestElement: $(consumer(regex('[0-9]{5}')))
				])
				headers {
					header('header', $(consumer(regex('application\\/vnd\\.fraud\\.v1\\+json;.*'))))
				}
			}
			response {
				status 200
				body([
					responseElement: $(producer(regex('[0-9]{7}')))
				])
				headers {
					contentType("application/vnd.fraud.v1+json")
				}
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
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
			def strippedTest = test.replace('\n', '').stripIndent().stripMargin()
		then:
			strippedTest.matches(""".*header\\("header", "application\\/vnd\\.fraud\\.v1\\+json;.*"\\).*""")
			strippedTest.matches(""".*body\\('''\\{"requestElement":"[0-9]{5}"\\}'''\\).*""")
			strippedTest.matches(""".*put\\("/foo/[0-9]{5}"\\).*""")
			strippedTest.contains("""response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application/vnd\\\\.fraud\\\\.v1\\\\+json.*')""")
			"application/vnd.fraud.v1+json;charset=UTF-8".matches('application/vnd\\.fraud\\.v1\\+json.*')
			strippedTest.contains("""assertThatJson(parsedJson).field("responseElement").matches("[0-9]{7}")""")
		and:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
	}

	@Issue('#85')
	def "should execute custom method for complex structures on the response side"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
			}
			response {
				status 200
				body([
					fraudCheckStatus: "OK",
					rejectionReason : [
							title: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
					]
				])
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatRejectionReasonIsNull(parsedJson.read(\'$.rejectionReason.title\'))')
		when:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains("Cannot find matching method Script1#assertThatRejectionReasonIsNull")
	}

	@Issue('#85')
	def "should execute custom method for more complex structures on the response side when using Spock"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
			}
			response {
				status 200
				body([
					[
							name: $(consumer("userName 1"), producer(execute('assertThatUserNameIsNotNull($it)')))
					],
					[
							name: $(consumer("userName 2"), producer(execute('assertThatUserNameIsNotNull($it)')))
					]
				])
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read('$[0].name')''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read('$[1].name')''')
	}

	@Issue('#85')
	def "should execute custom method for more complex structures on the response side when using JUnit"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
			}
			response {
				status 200
				body([
					[
							name: $(consumer("userName 1"), producer(execute('assertThatUserNameIsNotNull($it)')))
					],
					[
							name: $(consumer("userName 2"), producer(execute('assertThatUserNameIsNotNull($it)')))
					]
				])
			}
		}
			MethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[1].name")''')
	}

	@Issue('#111')
	def "should execute custom method for request headers"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
				headers {
					header('authorization', value(consumer('Bearer token'), producer(execute('getOAuthTokenHeader()'))))
				}
			}
			response {
				status 200
				body([
						fraudCheckStatus: "OK",
						rejectionReason : [
								title: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
						]
				])
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.given(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('.header("authorization", getOAuthTokenHeader())')
		when:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains("Cannot find matching method Script1#getOAuthTokenHeader")
	}

	@Issue('#150')
	def "should support body matching in response"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				url '/get'
			}
			response {
				status 200
				body(value(stub("HELLO FROM STUB"), server(regex(".*"))))
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("responseBody ==~ java.util.regex.Pattern.compile('.*')")
		and:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
	}

	@Issue('#150')
	def "should support custom method execution in response"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				url '/get'
			}
			response {
				status 200
				body(value(stub("HELLO FROM STUB"), server(execute('foo($it)'))))
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("foo(responseBody)")
		when:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains("Cannot find matching method Script1#foo")
	}

	@Issue('#149')
	def "should allow c/p version of consumer producer"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
				headers {
					header('authorization', $(c('Bearer token'), p(execute('getOAuthTokenHeader()'))))
				}
			}
			response {
				status 200
				body([
						fraudCheckStatus: "OK",
						rejectionReason : [
								title: $(c(null), p(execute('assertThatRejectionReasonIsNull($it)')))
						]
				])
			}
		}
			MethodBodyBuilder builder = new MockMvcSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.given(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('.header("authorization", getOAuthTokenHeader())')
		when:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains("Cannot find matching method Script1#getOAuthTokenHeader()")
	}

	@Issue('#149')
		def "should allow easier way of providing dynamic values for [#methodBuilderName]"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/get'
				body([
						duck: $(regex("[0-9]")),
						alpha: $(anyAlphaUnicode()),
				        number: $(anyNumber()),
						aBoolean: $(aBoolean()),
						ip: $(anyIpAddress()),
						hostname: $(anyHostname()),
						email: $(anyEmail()),
						url: $(anyUrl()),
						uuid: $(anyUuid()),
						date: $(anyDate()),
						dateTime: $(anyDateTime()),
						time: $(anyTime()),
						iso8601WithOffset: $(anyIso8601WithOffset()),
						nonBlankString: $(anyNonBlankString()),
						nonEmptyString: $(anyNonEmptyString()),
						anyOf: $(anyOf('foo', 'bar'))
				])
				headers {
					contentType(applicationJson())
				}
			}
			response {
				status 200
				body([
						alpha: $(anyAlphaUnicode()),
						number: $(anyNumber()),
						aBoolean: $(aBoolean()),
						ip: $(anyIpAddress()),
						hostname: $(anyHostname()),
						email: $(anyEmail()),
						url: $(anyUrl()),
						uuid: $(anyUuid()),
						date: $(anyDate()),
						dateTime: $(anyDateTime()),
						time: $(anyTime()),
						iso8601WithOffset: $(anyIso8601WithOffset()),
						nonBlankString: $(anyNonBlankString()),
						nonEmptyString: $(anyNonEmptyString()),
						anyOf: $(anyOf('foo', 'bar'))
				])
				headers {
					contentType(applicationJson())
				}
			}
		}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).field("aBoolean").matches("(true|false)")')
			test.contains('assertThatJson(parsedJson).field("alpha").matches("[\\\\p{L}]*")')
			test.contains('assertThatJson(parsedJson).field("hostname").matches("((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?")')
			test.contains('assertThatJson(parsedJson).field("url").matches("((www\\\\.|(http|https|ftp|news|file)+\\\\:\\\\/\\\\/)[_.a-z0-9-]+\\\\.[a-z0-9\\\\/_:@=.+?,##%&~-]*[^.|\\\\\'|\\\\# |!|\\\\(|?|,| |>|<|;|\\\\)])")')
			test.contains('assertThatJson(parsedJson).field("number").matches("-?\\\\d*(\\\\.\\\\d+)?")')
			test.contains('assertThatJson(parsedJson).field("email").matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4}")')
			test.contains('assertThatJson(parsedJson).field("ip").matches("([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])")')
			test.contains('assertThatJson(parsedJson).field("uuid").matches("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}")')
			test.contains('assertThatJson(parsedJson).field("date").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])')
			test.contains('assertThatJson(parsedJson).field("dateTime").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])')
			test.contains('assertThatJson(parsedJson).field("time").matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("iso8601WithOffset").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.\\\\d{3})?(Z|[+-][01]\\\\d:[0-5]\\\\d)")')
			test.contains('assertThatJson(parsedJson).field("nonBlankString").matches(".*(\\\\S+|\\\\R).*|!^\\\\R*' + endOfLineRegExSymbol + '")')
			test.contains('assertThatJson(parsedJson).field("nonEmptyString").matches(".+")')
			test.contains('assertThatJson(parsedJson).field("anyOf").matches("^foo' + endOfLineRegExSymbol + '|^bar' + endOfLineRegExSymbol + '")')
			!test.contains('cursor')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName			| methodBuilder																				| endOfLineRegExSymbol
			"MockMvcSpockMethodBuilder"	| { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } 	| '\\$'
			"MockMvcJUnitMethodBuilder"	| { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  	| '$'

	}

	@Issue('#162')
		def "should escape regex properly for content type"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method GET()
				url 'get'
				headers {
					contentType("application/vnd.fraud.v1+json")
				}
			}
			response {
				status 200
				headers {
					contentType("application/vnd.fraud.v1+json")
				}
			}
		}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('application/vnd\\\\.fraud\\\\.v1\\\\+json.*')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName           | methodBuilder                                                                          
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                 
	}

	@Issue('#173')
		def "should resolve Optional object when used in query parameters"() {
		given:
		Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath('/blacklist') {
						queryParameters {
							parameter 'isActive': value(consumer(optional(regex('(true|false)'))))
							parameter 'limit': value(consumer(optional(regex('([0-9]{1,10})'))))
							parameter 'offset': value(consumer(optional(regex('([0-9]{1,10})'))))
						}
					}
					headers {
						header 'Content-Type': 'application/json'
					}
				}
				response {
					status(200)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('org.springframework.cloud.contract.spec.internal.OptionalProperty')
			test.contains('(([0-9]{1,10}))?')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName           | methodBuilder                                                                          
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                 
	}

	@Issue('#172')
		def "should resolve plain text properly via headers"() {
		given:
		Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url("/foo")
				}
				response {
					status(200)
					body '{"a":1}\n{"a":2}'
					headers {
						contentType(textPlain())
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('assertThatJson(parsedJson).field("a").isEqualTo(1)')
			test.contains(expectedAssertion)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName           | methodBuilder                                                                           | expectedAssertion
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '''responseBody == "{\\"a\\":1}\\n{\\"a\\":2}"'''
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
	}

	@Issue('#169')
	def "should escape quotes properly using [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url '/foo'
					body(
							xyz: 'abc'
					)
					headers { header('Content-Type', 'application/json;charset=UTF-8') }
				}
				response {
					status 200
					body(
							bar: $(producer(regex('some value \u0022with quote\u0022|bar')))
					)
					headers { header('Content-Type': 'application/json;charset=UTF-8') }
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
//			test.contains('assertThatJson(parsedJson).field("bar").matches("some value \\"with quote\\"|bar")')
//		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName           | methodBuilder                                                                           | expectedAssertion
			"MockMvcSpockMethodBuilder" | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '''responseBody == "{\\"a\\":1}\\n{\\"a\\":2}"'''
			"MockMvcJUnitMethodBuilder" | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                  | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
	}

	@Issue('#169')
	def "should make the execute method work in a url for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url $(c("foo"), p(execute("executedMethod()")))
				}
				response {
					status 200
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			def e = thrown(Throwable)
			missingMethodAssertion(e, capture)
		and:
			test.contains("executedMethod()")
			!test.contains("\"executedMethod()\"")
			!test.contains("'executedMethod()'")
		where:
			methodBuilderName                                    | methodBuilder                                                                               | missingMethodAssertion
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }     | { Throwable t, OutputCapture capture -> t.message.contains("Cannot find matching method Script1#executedMethod") }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { Throwable t, OutputCapture capture -> t.message.contains("Truncated class file") && capture.toString().contains("post(executedMethod())") }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { Throwable t, OutputCapture capture -> t.message.contains("Cannot find matching method Script1#executedMethod") }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { Throwable t, OutputCapture capture -> t.message.contains("Truncated class file") && capture.toString().contains("path(executedMethod())") }
	}

	@Issue('#203')
	def "should create an assertion for an empty list for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/api/v1/xxxx'
				}
				response {
					status 200
					body([
							status: '200',
							list: [],
							foo: ["bar", "baz"]
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			test.contains('assertThatJson(parsedJson).array("list").isEmpty()')
			!test.contains('assertThatJson(parsedJson).array("foo").isEmpty()')
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#226")
	def "should work properly when body is an integer [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/api/v1/xxxx'
					body(12000)
				}
				response {
					status 200
					body(12000)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			requestAssertion(test)
			responseAssertion(test)
		where:
			methodBuilderName                                    | methodBuilder                                                                               | requestAssertion                                                                      | responseAssertion
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }     | { String body -> body.contains("body('''12000''')") }                                 | { String body -> body.contains('responseBody == "12000"') }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { String body -> body.contains('body("12000")') }                                     | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String body -> body.contains(""".method('GET', entity('12000', 'text/plain'))""") } | { String body -> body.contains('responseBody == "12000"') }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { String body -> body.contains(""".method("GET", entity("12000", "text/plain"))""") } | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000")') }
	}

	@Issue("#230")
	def "should manage to reference request in response [#methodBuilderName]"() {
		given:
			//tag::template_contract[]
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url('/api/v1/xxxx') {
						queryParameters {
							parameter("foo", "bar")
							parameter("foo", "bar2")
						}
					}
					headers {
						header(authorization(), "secret")
						header(authorization(), "secret2")
					}
					body(foo: "bar", baz: 5)
				}
				response {
					status 200
					headers {
						header(authorization(), "foo ${fromRequest().header(authorization())} bar")
					}
					body(
							url: fromRequest().url(),
							param: fromRequest().query("foo"),
							paramIndex: fromRequest().query("foo", 1),
							authorization: fromRequest().header("Authorization"),
							authorization2: fromRequest().header("Authorization", 1),
							fullBody: fromRequest().body(),
							responseFoo: fromRequest().body('$.foo'),
							responseBaz: fromRequest().body('$.baz'),
							responseBaz2: "Bla bla ${fromRequest().body('$.foo')} bla bla"
					)
				}
			}
			//end::template_contract[]
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		then:
			!test.contains('''DslProperty''')
			test.contains('''assertThatJson(parsedJson).field("url").isEqualTo("/api/v1/xxxx")''')
			test.contains('''assertThatJson(parsedJson).field("fullBody").isEqualTo("{\\"foo\\":\\"bar\\",\\"baz\\":5}")''')
			test.contains('''assertThatJson(parsedJson).field("paramIndex").isEqualTo("bar2")''')
			test.contains('''assertThatJson(parsedJson).field("responseFoo").isEqualTo("bar")''')
			test.contains('''assertThatJson(parsedJson).field("authorization").isEqualTo("secret")''')
			test.contains('''assertThatJson(parsedJson).field("authorization2").isEqualTo("secret2")''')
			test.contains('''assertThatJson(parsedJson).field("responseBaz").isEqualTo(5)''')
			test.contains('''assertThatJson(parsedJson).field("responseBaz2").isEqualTo("Bla bla bar bla bla")''')
			test.contains('''assertThatJson(parsedJson).field("param").isEqualTo("bar")''')
			responseAssertion(test)
		where:
			methodBuilderName                                    | methodBuilder                                                                               | responseAssertion
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }     | { String body -> body.contains("response.header('Authorization')  == 'foo secret bar'") }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { String body -> body.contains('assertThat(response.header("Authorization")).isEqualTo("foo secret bar");') }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String body -> body.contains("response.getHeaderString('Authorization')  == 'foo secret bar'") }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { String body -> body.contains('assertThat(response.getHeaderString("Authorization")).isEqualTo("foo secret bar");') }
	}
}
