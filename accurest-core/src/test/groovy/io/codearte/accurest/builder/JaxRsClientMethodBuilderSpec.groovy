package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.WireMockStubStrategy
import io.codearte.accurest.dsl.WireMockStubVerifier
import io.codearte.accurest.file.Contract
import spock.lang.Issue
import spock.lang.Specification

class JaxRsClientMethodBuilderSpec extends Specification implements WireMockStubVerifier {

	def "should generate assertions for simple response body with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	@Issue("#187")
	def "should generate assertions for null and boolean values with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new Contract(null, false, 0, null), contractDsl).toWireMockClientStub())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	@Issue("#79")
	def "should generate assertions for simple response body constructed from map with a list with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			stubMappingIsValidWireMockStub(new WireMockStubStrategy("Test", new Contract(null, false, 0, null), contractDsl).toWireMockClientStub())
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	@Issue("#82")
	def "should generate proper request when body constructed from map with a list with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | """entity('{\"items\":[\"HOP\"]}', 'application/json')"""
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'entity("{\\"items\\":[\\"HOP\\"]}", "application/json")'
	}

	@Issue("#88")
	def "should generate proper request when body constructed from GString with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | """entity('property1=VAL1', 'application/octet-stream')"""
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'entity("\\"property1=VAL1\\"", "application/octet-stream")'
	}

	def "should generate assertions for array in response body with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
		where:
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	def "should generate assertions for array inside response body element with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	def "should generate assertions for nested objects in response body with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	def "should generate regex assertions for map objects in response body with #methodBodyName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body(
							property1: "a",
							property2: value(
									client('123'),
									server(regex('[0-9]{3}'))
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	def "should generate regex assertions for string objects in response body with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body("""{"property1":"a","property2":"${value(client('123'), server(regex('[0-9]{3}')))}"}""")
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
			methodBuilderName                   | methodBuilder
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }
	}

	def "should ignore 'Accept' header and use 'request' method with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
		where:
			methodBuilderName                   | methodBuilder                                                                    | requestString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | "request('text/plain')"
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'request("text/plain")'
	}

	def "should ignore 'Content-Type' header and use 'entity' method with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
		where:
			methodBuilderName                   | methodBuilder                                                                    | requestStrings
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | ["""entity('', 'text/plain')""", """header('Timer', '123')"""]
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | ['entity("\\"\\"", "text/plain")', 'header("Timer", "123")']
	}

	def "should generate a call with an url path and query parameters with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'GET'
					urlPath('/users') {
						queryParameters {
							parameter 'limit': $(client(equalTo("20")), server(equalTo("10")))
							parameter 'offset': $(client(containing("20")), server(equalTo("20")))
							parameter 'filter': "email"
							parameter 'sort': equalTo("name")
							parameter 'search': $(client(notMatching(~/^\/[0-9]{2}$/)), server("55"))
							parameter 'age': $(client(notMatching("^\\w*\$")), server("99"))
							parameter 'name': $(client(matching("Denis.*")), server("Denis.Stepanov"))
							parameter 'email': "bob@email.com"
							parameter 'hello': $(client(matching("Denis.*")), server(absent()))
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
		where:
			methodBuilderName                   | methodBuilder                                                                    | modifyStringIfRequired
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | { String paramString -> paramString }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | { String paramString -> paramString.replace("'", "\"") }
	}

	@Issue('#169')
	def "should generate a call with an url path and query parameters with url containing a pattern with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'GET'
					url($(stub(regex('/foo/[0-9]+')), test('/foo/123456'))) {
						queryParameters {
							parameter 'limit': $(client(equalTo("20")), server(equalTo("10")))
							parameter 'offset': $(client(containing("20")), server(equalTo("20")))
							parameter 'filter': "email"
							parameter 'sort': equalTo("name")
							parameter 'search': $(client(notMatching(~/^\/[0-9]{2}$/)), server("55"))
							parameter 'age': $(client(notMatching("^\\w*\$")), server("99"))
							parameter 'name': $(client(matching("Denis.*")), server("Denis.Stepanov"))
							parameter 'email': "bob@email.com"
							parameter 'hello': $(client(matching("Denis.*")), server(absent()))
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
		where:
			methodBuilderName                   | methodBuilder                                                                    | modifyStringIfRequired
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | { String paramString -> paramString }
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | { String paramString -> paramString.replace("'", "\"") }
	}

	def "should generate test for empty body with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder                                                                    | bodyString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | "entity('', 'application/octet-stream')"
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'entity("\\"\\"", "application/octet-stream"'
	}

	def "should generate test for String in response body with #methodBodyName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			methodBuilderName                   | methodBuilder                                                                    | bodyDefinitionString                                    | bodyEvaluationString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | "String responseAsString = response.readEntity(String)" | 'responseBody == "test"'
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'Object responseBody = (responseAsString);'             | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('#171')
	def "should generate test with uppercase method name with #methodBuilderName"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "get"
					url "/v1/some_cool_requests/e86df6f693de4b35ae648464c5b0dc08"
				}
				response {
					status 200
					headers {
						header('Content-Type': 'application/json;charset=UTF-8')
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
		where:
			methodBuilderName                   | methodBuilder                                                                    | methodString
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl) } | ".method('GET')"
			"JaxRsClientJUnitMethodBodyBuilder" | { GroovyDsl dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl) }                  | 'method("GET")'
	}

	def "should generate a call with an url path and query parameters with JUnit - we'll put it into docs"() {
		given:
		GroovyDsl contractDsl = GroovyDsl.make {
			request {
				method 'GET'
				urlPath('/users') {
					queryParameters {
						parameter 'limit': $(client(equalTo("20")), server(equalTo("10")))
						parameter 'offset': $(client(containing("20")), server(equalTo("20")))
						parameter 'filter': "email"
						parameter 'sort': equalTo("name")
						parameter 'search': $(client(notMatching(~/^\/[0-9]{2}$/)), server("55"))
						parameter 'age': $(client(notMatching("^\\w*\$")), server("99"))
						parameter 'name': $(client(matching("Denis.*")), server("Denis.Stepanov"))
						parameter 'email': "bob@email.com"
						parameter 'hello': $(client(matching("Denis.*")), server(absent()))
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
			MethodBodyBuilder builder = new JaxRsClientJUnitMethodBodyBuilder(contractDsl)
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
	}

	private String stripped(String string) {
		return string.stripMargin().stripIndent().replace('\t', '').replace('\n', '').replace(' ','')
	}
}
