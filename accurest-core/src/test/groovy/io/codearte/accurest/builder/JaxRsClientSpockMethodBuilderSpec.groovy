package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.WireMockStubStrategy
import io.codearte.accurest.dsl.WireMockStubVerifier
import spock.lang.Issue
import spock.lang.Specification

class JaxRsClientSpockMethodBuilderSpec extends Specification implements WireMockStubVerifier {

	def "should generate assertions for simple response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
			blockBuilder.toString().contains("\$[?(@.property2 == 'b')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue("#79")
	def "should generate assertions for simple response body constructed from map with a list"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
			blockBuilder.toString().contains("\$.property2[*][?(@.a == 'sth')]")
			blockBuilder.toString().contains("\$.property2[*][?(@.b == 'sthElse')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue("#82")
	def "should generate proper request when body constructed from map with a list"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("entity('{\"items\":[\"HOP\"]}', 'application/json')")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue("#88")
	def "should generate proper request when body constructed from GString"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("entity('property1=VAL1', 'application/octet-stream')")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate assertions for array in response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[*][?(@.property1 == 'a')]")
			blockBuilder.toString().contains("\$[*][?(@.property2 == 'b')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate assertions for array inside response body element"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$.property1[*][?(@.property3 == 'test2')]")
			blockBuilder.toString().contains("\$.property1[*][?(@.property2 == 'test1')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate assertions for nested objects in response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$.property2[?(@.property3 == 'b')]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate regex assertions for map objects in response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate regex assertions for string objects in response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should ignore 'Accept' header and use 'request' method"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("request('text/plain')")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should ignore 'Content-Type' header and use 'entity' method"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("entity('', 'text/plain')")
			blockBuilder.toString().contains("header('Timer', '123')")
			!blockBuilder.toString().contains("header('Content-Type'")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate a call with an url path and query parameters"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains("queryParam('limit', '10'")
			spockTest.contains("queryParam('offset', '20'")
			spockTest.contains("queryParam('filter', 'email'")
			spockTest.contains("queryParam('sort', 'name'")
			spockTest.contains("queryParam('search', '55'")
			spockTest.contains("queryParam('age', '99'")
			spockTest.contains("queryParam('name', 'Denis.Stepanov'")
			spockTest.contains("queryParam('email', 'bob@email.com'")
			spockTest.contains('$[?(@.property2 == \'b\')]')
			spockTest.contains('$[?(@.property1 == \'a\')]')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue('#169')
	def "should generate a call with an url path and query parameters with url containing a pattern"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'GET'
					url($(stub(regex('/foo/[0-9]+')), test('/foo/123456'))){
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains("queryParam('limit', '10'")
			spockTest.contains("queryParam('offset', '20'")
			spockTest.contains("queryParam('filter', 'email'")
			spockTest.contains("queryParam('sort', 'name'")
			spockTest.contains("queryParam('search', '55'")
			spockTest.contains("queryParam('age', '99'")
			spockTest.contains("queryParam('name', 'Denis.Stepanov'")
			spockTest.contains("queryParam('email', 'bob@email.com'")
			spockTest.contains('$[?(@.property2 == \'b\')]')
			spockTest.contains('$[?(@.property1 == \'a\')]')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate test for empty body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains("entity('', 'application/octet-stream')")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should generate test for String in response body"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('String responseAsString = response.readEntity(String)')
			spockTest.contains('responseBody == "test"')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue('#171')
	def "should generate test with uppercase method name"() {
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
			JaxRsClientSpockMethodBodyBuilder builder = new JaxRsClientSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains(".method('GET')")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

}
