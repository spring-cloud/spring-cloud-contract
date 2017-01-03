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
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

class JaxRsClientMethodBuilderSpec extends Specification implements WireMockStubVerifier {

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(assertJsonSize: true)

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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property3").isEqualTo(false)""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property2").isNull()""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("property1").isEqualTo("true")""")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue("#82")
	def "should generate proper request when body constructed from map with a list with #methodBuilderName"() {
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
			methodBuilderName                   | methodBuilder                                                                                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | """entity('{\"items\":[\"HOP\"]}', 'application/json')"""
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'entity("{\\"items\\":[\\"HOP\\"]}", "application/json")'
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | """entity('property1=VAL1', 'application/octet-stream')"""
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'entity("\\"property1=VAL1\\"", "application/octet-stream")'
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
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("property1").isEqualTo("a")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("property2").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should generate regex assertions for map objects in response body with #methodBodyName"() {
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
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
					body("""{"property1":"a","property2":"${value(consumer('123'), producer(regex('[0-9]{3}')))}"}""")
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	def "should ignore 'Accept' header and use 'request' method with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
					headers {
						header("Accept", "text/plain")
					}
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
			blockBuilder.toString().contains(requestString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | requestString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | "request('text/plain')"
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'request("text/plain")'
	}

	def "should ignore 'Content-Type' header and use 'entity' method with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
					headers {
						header("Content-Type", "text/plain")
						header("Timer", "123")
					}
					body ''
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
			for (String requestString : requestStrings) {
				blockBuilder.toString().contains(requestString)
			}
			!blockBuilder.toString().contains("""Content Type""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | requestStrings
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | ["""entity('', 'text/plain')""", """header('Timer', '123')"""]
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | ['entity("\\"\\"", "text/plain")', 'header("Timer", "123")']
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
			test.contains(modifyStringIfRequired.call("queryParam('limit', '10'"))
			test.contains(modifyStringIfRequired.call("queryParam('offset', '20'"))
			test.contains(modifyStringIfRequired.call("queryParam('filter', 'email'"))
			test.contains(modifyStringIfRequired.call("queryParam('sort', 'name'"))
			test.contains(modifyStringIfRequired.call("queryParam('search', '55'"))
			test.contains(modifyStringIfRequired.call("queryParam('age', '99'"))
			test.contains(modifyStringIfRequired.call("queryParam('name', 'Denis.Stepanov'"))
			test.contains(modifyStringIfRequired.call("queryParam('email', 'bob@email.com'"))
			test.contains(modifyStringIfRequired.call("""assertThatJson(parsedJson).field("property1").isEqualTo("a")"""))
			test.contains(modifyStringIfRequired.call("""assertThatJson(parsedJson).field("property2").isEqualTo("b")"""))
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | modifyStringIfRequired
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String paramString -> paramString }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | { String paramString -> paramString.replace("'", "\"") }
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
			test.contains(modifyStringIfRequired.call("queryParam('limit', '10'"))
			test.contains(modifyStringIfRequired.call("queryParam('offset', '20'"))
			test.contains(modifyStringIfRequired.call("queryParam('filter', 'email'"))
			test.contains(modifyStringIfRequired.call("queryParam('sort', 'name'"))
			test.contains(modifyStringIfRequired.call("queryParam('search', '55'"))
			test.contains(modifyStringIfRequired.call("queryParam('age', '99'"))
			test.contains(modifyStringIfRequired.call("queryParam('name', 'Denis.Stepanov'"))
			test.contains(modifyStringIfRequired.call("queryParam('email', 'bob@email.com'"))
			test.contains(modifyStringIfRequired.call("""assertThatJson(parsedJson).field("property1").isEqualTo("a")"""))
			test.contains(modifyStringIfRequired.call("""assertThatJson(parsedJson).field("property2").isEqualTo("b")"""))
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | modifyStringIfRequired
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String paramString -> paramString }
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | { String paramString -> paramString.replace("'", "\"") }
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | "entity('', 'application/octet-stream')"
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'entity("\\"\\"", "application/octet-stream"'
	}

	def "should generate test for String in response body with #methodBodyName"() {
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
			methodBuilderName                   | methodBuilder                                                                                                                                    | bodyDefinitionString                                    | bodyEvaluationString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | "String responseAsString = response.readEntity(String)" | 'responseBody == "test"'
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'String responseBody = responseAsString;'             | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('#171')
	def "should generate test with uppercase method name with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "get"
					url "/v1/some_cool_requests/e86df6f693de4b35ae648464c5b0dc08"
				}
				response {
					status 200
					headers {
						contentType(applicationJson())
					}
					body """
{"id":"789fgh","other_data":1268}
"""
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains(methodString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                   | methodBuilder                                                                                                                                    | methodString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | ".method('GET')"
			"JaxRsClientJUnitMethodBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                                   | 'method("GET")'
	}

	def "should generate a call with an url path and query parameters with JUnit - we'll put it into docs"() {
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
						"property1": "a"
					}
					"""
			}
		}
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedResponse =
// tag::jaxrs[]
'''
 // when:
  Response response = webTarget
    .path("/users")
    .queryParam("limit", "10")
    .queryParam("offset", "20")
    .queryParam("filter", "email")
    .queryParam("sort", "name")
    .queryParam("search", "55")
    .queryParam("age", "99")
    .queryParam("name", "Denis.Stepanov")
    .queryParam("email", "bob@email.com")
    .request()
    .method("GET");

  String responseAsString = response.readEntity(String.class);

 // then:
  assertThat(response.getStatus()).isEqualTo(200);
 // and:
  DocumentContext parsedJson = JsonPath.parse(responseAsString);
  assertThatJson(parsedJson).field("property1").isEqualTo("a");
'''
// end::jaxrs[]
		stripped(test) == stripped(expectedResponse)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompileJava(blockBuilder.toString())
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
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatRejectionReasonIsNull(parsedJson.read("$.get("rejectionReason").title"));')
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
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[1].name")''')
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
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[1].name")''')
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
					status 200
					body(value(stub("HELLO FROM STUB"), server(regex(".*"))))
				}
			}
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("assertThat(responseBody).matches(\".*\");")
		and:
			SyntaxChecker.tryToCompileJava(blockBuilder.toString())
	}

	@Issue('#150')
	def "should support body matching in response in Spock"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/get'
				}
				response {
					status 200
					status 200
					body(value(stub("HELLO FROM STUB"), server(regex(".*"))))
				}
			}
			MethodBodyBuilder builder = new JaxRsClientSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
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
					status 200
					body(value(stub("HELLO FROM STUB"), server(execute('foo($it)'))))
				}
			}
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("foo(responseBody);")
	}

	@Issue('#150')
	def "should support custom method execution in response in Spock"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/get'
				}
				response {
					status 200
					status 200
					body(value(stub("HELLO FROM STUB"), server(execute('foo($it)'))))
				}
			}
			MethodBodyBuilder builder = new JaxRsClientSpockMethodRequestProcessingBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("foo(responseBody)")
		and:
			SyntaxChecker.tryToCompileGroovy(blockBuilder.toString())
	}

	def "should allow c/p version of consumer producer"() {
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
						property2: $(
								c('123'),
								p(regex('[0-9]{3}'))
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
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                    | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#149')
	@Unroll
	def "should allow easier way of providing dynamic values for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
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
							time: $(anyTime())
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
							time: $(anyTime())
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
			test.contains('assertThatJson(parsedJson).field("date").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")')
			test.contains('assertThatJson(parsedJson).field("dateTime").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("time").matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			!test.contains('cursor')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                    | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	private String stripped(String string) {
		return string.stripMargin().stripIndent().replace('\t', '').replace('\n', '').replace(' ','')
	}

	@Issue('#173')
	@Unroll
	def "should resolve Optional object when used in query parameters for [#methodBuilderName]"() {
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
			methodBuilderName                                    | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}
}
