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

package org.springframework.cloud.contract.verifier.builder

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

class JaxRsClientMethodBuilderSpec extends Specification implements WireMockStubVerifier {

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Shared
	SingleTestGenerator.GeneratedClassData generatedClassData =
			new SingleTestGenerator.GeneratedClassData("foo", "com.example", new File(".").toPath())

	@Shared
	GeneratedClassDataForMethod generatedClassDataForMethod = new GeneratedClassDataForMethod(
			new SingleTestGenerator.GeneratedClassData("foo", "bar", new File(".").toPath()), "method")

	def setup() {
		properties = new ContractVerifierConfigProperties(
				assertJsonSize: true,
				testMode: TestMode.JAXRSCLIENT
		)
	}

	private String singleTestGenerator(Contract contractDsl) {
		return new JavaTestGenerator() {
			@Override
			ClassBodyBuilder classBodyBuilder(BlockBuilder builder, GeneratedClassMetaData metaData, SingleMethodBuilder methodBuilder) {
				return super.classBodyBuilder(builder, metaData, methodBuilder).field(new Field() {
					@Override
					boolean accept() {
						return metaData.configProperties.testMode == TestMode.JAXRSCLIENT
					}

					@Override
					Field call() {
						builder.addLine("WebTarget webTarget")
						return this
					}
				})
			}
		}.buildClass(properties, [contractMetadata(contractDsl)], "foo", generatedClassData)
	}

	private GeneratedClassMetaData generatedClassMetaData(Contract contractDsl) {
		new GeneratedClassMetaData(properties, [contractMetadata(contractDsl)], "foo", generatedClassData)
	}

	ContractMetadata contractMetadata(Contract contractDsl) {
		return new ContractMetadata(new File(".").toPath(), false, 0, null, contractDsl)
	}

	@Shared
	// tag::contract_with_cookies[]
	Contract contractDslWithCookiesValue = Contract.make {
		request {
			method "GET"
			url "/foo"
			headers {
				header 'Accept': 'application/json'
			}
			cookies {
				cookie 'cookie-key': 'cookie-value'
			}
		}
		response {
			status 200
			headers {
				header 'Content-Type': 'application/json'
			}
			cookies {
				cookie 'cookie-key': 'new-cookie-value'
			}
			body([status: 'OK'])
		}
	}
	// end::contract_with_cookies[]

	@Shared
	Contract contractDslWithCookiesPattern = Contract.make {
		request {
			method "GET"
			url "/foo"
			headers {
				header 'Accept': 'application/json'
			}
			cookies {
				cookie 'cookie-key': regex('[A-Za-z]+')
			}
		}
		response {
			status 200
			headers {
				header 'Content-Type': 'application/json'
			}
			cookies {
				cookie 'cookie-key': regex('[A-Za-z]+')
			}
			body([status: 'OK'])
		}
	}

	@Shared
	Contract contractDslWithAbsentCookies = Contract.make {
		request {
			method "GET"
			url "/foo"
			cookies {
				cookie 'cookie-key': absent()
			}
		}
		response {
			status 200
			body([status: 'OK'])
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
					status OK()
					body """{
	"property1": "a",
	"property2": "b"
}"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
					status OK()
					body """{
	"property1": "true",
	"property2": null,
	"property3": false
}"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property3']").isEqualTo(false)""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isNull()""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("true")""")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
					status OK()
					body(
							property1: 'a',
							property2: [
									[a: 'sth'],
									[b: 'sthElse']
							]
					)
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
					status OK()
					body(
							property1: 'a',
							property2: [
									[a: 'sth'],
									[b: 'sthElse']
							]
					)
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").hasSize(2)""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub())
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | """entity("{\\"items\\":[\\"HOP\\"]}", "application/json")"""
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'entity("{\\"items\\":[\\"HOP\\"]}", "application/json")'
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
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | """entity("property1=VAL1", "application/octet-stream")"""
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'entity("property1=VAL1", "application/octet-stream")'
	}

	def "should generate assertions for array in response body with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
					body """[
{
	"property1": "a"
},
{
	"property2": "b"
}]"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).array().contains("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).array().contains("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	def "should generate assertions for array inside response body element with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
					body """{
	"property1": [
	{ "property2": "test1"},
	{ "property3": "test2"}
	]
}"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).array("['property1']").contains("['property2']").isEqualTo("test1")""")
			test.contains("""assertThatJson(parsedJson).array("['property1']").contains("['property3']").isEqualTo("test2")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	def "should generate assertions for nested objects in response body with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
					body '''\
{
	"property1": "a",
	"property2": {"property3": "b"}
}
'''
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property2']").field("['property3']").isEqualTo("b")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	def "should generate regex assertions for map objects in response body with #methodBodyName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property2']").matches("[0-9]{3}")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	def "should generate regex assertions for string objects in response body with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
					body("""{"property1":"a","property2":"${value(consumer('123'), producer(regex('[0-9]{3}')))}"}""")
					headers {
						header('Content-Type': 'application/json')
					}
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property2']").matches("[0-9]{3}")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(requestString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'request("text/plain")'
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'request("text/plain")'
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
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			for (String requestString : requestStrings) {
				test.contains(requestString)
			}
			!test.contains("""Content Type""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestStrings
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | ["""entity('', 'text/plain')""", """header('Timer', '123')"""]
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | ['entity("\\"\\"", "text/plain")', 'header("Timer", "123")']
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
					status OK()
					body """
					{
						"property1": "a",
						"property2": "b"
					}
					"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(modifyStringIfRequired.call('''queryParam("limit", "10"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("offset", "20"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("filter", "email"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("sort", "name"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("search", "55"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("age", "99"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("name", "Denis.Stepanov"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("email", "bob@email.com"'''))
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | modifyStringIfRequired
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String paramString -> paramString }
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String paramString -> paramString.replace("'", "\"") }
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
					status OK()
					body """
					{
						"property1": "a",
						"property2": "b"
					}
					"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(modifyStringIfRequired.call('''queryParam("limit", "10"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("offset", "20"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("filter", "email"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("sort", "name"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("search", "55"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("age", "99"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("name", "Denis.Stepanov"'''))
			test.contains(modifyStringIfRequired.call('''queryParam("email", "bob@email.com"'''))
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | modifyStringIfRequired
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String paramString -> paramString }
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String paramString -> paramString.replace("'", "\"") }
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

			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | """entity("", "application/octet-stream")"""
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | """entity("", "application/octet-stream")"""
	}

	def "should not parse the response body if there is no response body specified in the contract"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "HEAD"
					url "head"
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains(bodyParsingString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyParsingString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | "String responseAsString = response.readEntity(String)"
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | "String responseAsString = response.readEntity(String.class);"
	}

	def "should generate test for String in response body with #methodBuilderName"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "POST"
					url "test"
				}
				response {
					status OK()
					body "test"
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyDefinitionString)
			test.contains(bodyEvaluationString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyDefinitionString                                    | bodyEvaluationString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | "String responseAsString = response.readEntity(String)" | 'responseBody == "test"'
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'String responseBody = responseAsString;'               | 'assertThat(responseBody).isEqualTo("test");'
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
					status OK()
					headers {
						contentType(applicationJson())
					}
					body """
{"id":"789fgh","other_data":1268}
"""
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(methodString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | methodString
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | '.build("GET")'
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | 'build("GET")'
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
					status OK()
					body """
					{
						"property1": "a"
					}
					"""
				}
			}
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedResponse =
// tag::jaxrs[]
					"""\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static javax.ws.rs.client.Entity.*;

@SuppressWarnings("rawtypes")
public class FooTest {
\tWebTarget webTarget;

\t@Test
\tpublic void validate_() throws Exception {

\t\t// when:
\t\t\tResponse response = webTarget
\t\t\t\t\t\t\t.path("/users")
\t\t\t\t\t\t\t.queryParam("limit", "10")
\t\t\t\t\t\t\t.queryParam("offset", "20")
\t\t\t\t\t\t\t.queryParam("filter", "email")
\t\t\t\t\t\t\t.queryParam("sort", "name")
\t\t\t\t\t\t\t.queryParam("search", "55")
\t\t\t\t\t\t\t.queryParam("age", "99")
\t\t\t\t\t\t\t.queryParam("name", "Denis.Stepanov")
\t\t\t\t\t\t\t.queryParam("email", "bob@email.com")
\t\t\t\t\t\t\t.request()
\t\t\t\t\t\t\t.build("GET")
\t\t\t\t\t\t\t.invoke();
\t\t\tString responseAsString = response.readEntity(String.class);

\t\t// then:
\t\t\tassertThat(response.getStatus()).isEqualTo(200);

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(responseAsString);
\t\t\tassertThatJson(parsedJson).field("['property1']").isEqualTo("a");
\t}

}

"""
// end::jaxrs[]
			test.trim() == expectedResponse.trim()
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompileJava("jaxrs", test)
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
					status OK()
					body([
							fraudCheckStatus: "OK",
							rejectionReason : [
									title: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
							]
					])
				}
			}
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason.title"));')
	}

	String sampleJson = '''

  [
    {
      "name"  : "iPhone",
      "number": "0123-4567-8888"
    },
    {
      "name"  : "home",
      "number": "0123-4567-8910"
    }
  ]
'''

	@Issue('#85')
	def "should execute custom method for more complex structures on the response side when using Spock"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
				}
				response {
					status OK()
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
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$.[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$.[1].name")''')
		and:
			DocumentContext context = JsonPath.parse(sampleJson)
			context.read('$.[0].name') == 'iPhone'
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
					status OK()
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
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$.[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$.[1].name")''')
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
					status OK()
					status OK()
					body(value(stub("HELLO FROM STUB"), server(regex(".*"))))
				}
			}
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("assertThat(responseBody).matches(\".*\");")
		and:
			SyntaxChecker.tryToCompileJava("jaxrs", test)
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
					status OK()
					status OK()
					body(value(stub("HELLO FROM STUB"), server(regex(".*"))))
				}
			}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('responseBody ==~ java.util.regex.Pattern.compile(".*")')
		and:
			SyntaxChecker.tryToCompileGroovy("jaxrs", test)
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
					status OK()
					status OK()
					body(value(stub("HELLO FROM STUB"), server(execute('foo($it)'))))
				}
			}
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
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
					status OK()
					status OK()
					body(value(stub("HELLO FROM STUB"), server(execute('foo($it)'))))
				}
			}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("foo(responseBody)")
		and:
			// no static compilation due to bug in Groovy https://issues.apache.org/jira/browse/GROOVY-8055
			SyntaxChecker.tryToCompileGroovy("jaxrs", test, false)
	}

	def "should allow c/p version of consumer producer"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status OK()
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property2']").matches("[0-9]{3}")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
							alpha            : $(anyAlphaUnicode()),
							number           : $(anyNumber()),
							positiveInt      : $(positiveInt()),
							aDouble          : $(anyDouble()),
							aBoolean         : $(aBoolean()),
							ip               : $(anyIpAddress()),
							hostname         : $(anyHostname()),
							email            : $(anyEmail()),
							url              : $(anyUrl()),
							httpsUrl         : $(anyHttpsUrl()),
							uuid             : $(anyUuid()),
							date             : $(anyDate()),
							dateTime         : $(anyDateTime()),
							time             : $(anyTime()),
							iso8601WithOffset: $(anyIso8601WithOffset()),
							nonBlankString   : $(anyNonBlankString()),
							nonEmptyString   : $(anyNonEmptyString()),
							anyOf            : $(anyOf('foo', 'bar'))
					])
					headers {
						contentType(applicationJson())
					}
				}
				response {
					status OK()
					body([
							alpha            : $(anyAlphaUnicode()),
							number           : $(anyNumber()),
							positiveInt      : $(positiveInt()),
							aDouble          : $(anyDouble()),
							aBoolean         : $(aBoolean()),
							ip               : $(anyIpAddress()),
							hostname         : $(anyHostname()),
							email            : $(anyEmail()),
							url              : $(anyUrl()),
							httpsUrl         : $(anyHttpsUrl()),
							uuid             : $(anyUuid()),
							date             : $(anyDate()),
							dateTime         : $(anyDateTime()),
							time             : $(anyTime()),
							iso8601WithOffset: $(anyIso8601WithOffset()),
							nonBlankString   : $(anyNonBlankString()),
							nonEmptyString   : $(anyNonEmptyString()),
							anyOf            : $(anyOf('foo', 'bar'))
					])
					headers {
						contentType(applicationJson())
					}
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).field("[\'aBoolean\']").matches("(true|false)")')
			test.contains('assertThatJson(parsedJson).field("[\'alpha\']").matches("[\\\\p{L}]*")')
			test.contains('assertThatJson(parsedJson).field("[\'hostname\']").matches("((http[s]?|ftp):/)/?([^:/\\\\s]+)(:[0-9]{1,5})?")')
			test.contains('assertThatJson(parsedJson).field("[\'url\']").matches("^(?:(?:[A-Za-z][+-.\\\\w^_]*:/{2})?(?:\\\\S+(?::\\\\S*)?@)?(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,}))|(?:localhost))(?::\\\\d{2,5})?(?:/\\\\S*)?)')
			test.contains('assertThatJson(parsedJson).field("[\'httpsUrl\']").matches("^(?:https:/{2}(?:\\\\S+(?::\\\\S*)?@)?(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,}))|(?:localhost))(?::\\\\d{2,5})?(?:/\\\\S*)?)')
			test.contains('assertThatJson(parsedJson).field("[\'number\']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'positiveInt\']").matches("([1-9]\\\\d*)")')
			test.contains('assertThatJson(parsedJson).field("[\'aDouble\']").matches("-?(\\\\d*\\\\.\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'email\']").matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6}")')
			test.contains('assertThatJson(parsedJson).field("[\'ip\']").matches("([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])")')
			test.contains('assertThatJson(parsedJson).field("[\'uuid\']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")')
			test.contains('assertThatJson(parsedJson).field("[\'date\']").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")')
			test.contains('assertThatJson(parsedJson).field("[\'dateTime\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("[\'time\']").matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("[\'iso8601WithOffset\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.\\\\d{1,6})?(Z|[+-][01]\\\\d:[0-5]\\\\d)")')
			test.contains('assertThatJson(parsedJson).field("[\'nonBlankString\']").matches("^\\\\s*\\\\S[\\\\S\\\\s]*")')
			test.contains('assertThatJson(parsedJson).field("[\'nonEmptyString\']").matches("[\\\\S\\\\s]+")')
			test.contains('assertThatJson(parsedJson).field("[\'anyOf\']").matches("^foo' + endOfLineRegexSymbol + '|^bar' + endOfLineRegexSymbol + '")')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | endOfLineRegexSymbol
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | '\\$'
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | '$'
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('org.springframework.cloud.contract.spec.internal.OptionalProperty')
			test.contains('(([0-9]{1,10}))?')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	@Issue('#1388')
	@Unroll
	def "should keep the custom content type that includes the +json suffix [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method("POST")
					url("/ping")
					headers {
						header('Content-Type': 'application/my-content-type+json')
					}
					body($(test(value: "test"), stub(anyNonEmptyString())))
				}
				response {
					status 200
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('"application/my-content-type+json")')
			!test.contains('"application/json")')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	@Issue('#261')
	@Unroll
	def "should not produce any additional quotes for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "POST"
					url "/v2/applications/a-TEST-upload/documents"
					headers {
						header 'Authorization': "foo"
						header 'Content-Type': "multipart/form-data;boundary=Boundary_1_1831312172_1491482784697"
					}
					body $('''
--Boundary_1_1831312172_1491482784697
Content-Disposition: form-data; name="file"

DATA
--Boundary_1_1831312172_1491482784697--
''')
				}
				response {
					status 400
					body "File name is required"
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('entity("\\"\\n')
			test.contains('''entity("\\n--Boundary_1_1831312172_1491482784697\\nContent-Disposition: form-data; name=\\"file\\"\\n\\nDATA\\n--Boundary_1_1831312172_1491482784697--\\n", "multipart/form-data;boundary=Boundary_1_1831312172_1491482784697"))''')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	@Issue('#261')
	@Unroll
	def "should not produce any additional quotes for json body [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method "POST"
					url "/foo"
					headers {
						header 'Content-Type': "application/json"
					}
					body $('''{ "foo": "bar"}''')
				}
				response {
					status 400
					body "File name is required"
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('entity("\\"\\n')
			test.contains('''.build("POST", entity("{\\"foo\\":\\"bar\\"}", "application/json"))''')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
	}

	def "should generate test for cookies with string value in JAX-RS JUnit test"() {
		given:
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDslWithCookiesValue)
		then:
			test.contains('''.cookie("cookie-key", "cookie-value")''')
			test.contains('''assertThat(response.getCookies().get("cookie-key")).isNotNull();''')
			test.contains('''assertThat(response.getCookies().get("cookie-key").getValue()).isEqualTo("new-cookie-value");''')
		and:
			SyntaxChecker.tryToCompile("jaxrs", test)
	}

	def "should generate test for cookies with pattern in JAX-RS JUnit test"() {
		given:
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDslWithCookiesPattern)
		then:
			!test.contains('''.cookie("cookie-key", "[A-Za-z]+")''')
			test.contains('''.cookie("cookie-key", "''')
			test.contains('''assertThat(response.getCookies().get("cookie-key")).isNotNull();''')
			test.contains('''assertThat(response.getCookies().get("cookie-key").getValue()).matches("[A-Za-z]+");''')
		and:
			SyntaxChecker.tryToCompile("jaxrs", test)
	}

	def "should not generate cookie assertions with absent value in JAX-RS JUnit test"() {
		given:
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDslWithAbsentCookies)
		then:
			!test.contains("cookie")
		and:
			SyntaxChecker.tryToCompile("jaxrs", test)
	}

	def "should generate test for cookies with string value in JAX-RS Spock test"() {
		given:
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDslWithCookiesValue)
		then:
			test.contains('''.cookie('cookie-key', 'cookie-value')''')
			test.contains('''response.getCookies().get("cookie-key") != null''')
			test.contains('response.getCookies().get("cookie-key").getValue() == "new-cookie-value"')
		and:
			SyntaxChecker.tryToCompile("jaxrs-spock", test)
	}

	def "should generate test for cookies with pattern in JAX-RS Spock test"() {
		given:
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDslWithCookiesPattern)
		then:
			!test.contains('''.cookie('cookie-key', '[A-Za-z]+')''')
			test.contains('''.cookie('cookie-key', ''')
			test.contains('''response.getCookies().get("cookie-key") != null''')
			test.contains('''response.getCookies().get("cookie-key").getValue() ==~ java.util.regex.Pattern.compile("[A-Za-z]+")''')
		and:
			SyntaxChecker.tryToCompile("jaxrs-spock", test)
	}

	def "should not generate cookie assertions with absent value in JAX-RS Spock test"() {
		given:
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDslWithAbsentCookies)
		then:
			!test.contains("cookie")
		and:
			SyntaxChecker.tryToCompile("jaxrs-spock", test)
	}
}
