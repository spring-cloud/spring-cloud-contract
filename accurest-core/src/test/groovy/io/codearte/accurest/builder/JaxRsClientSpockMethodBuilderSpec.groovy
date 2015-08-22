package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Issue
import spock.lang.Specification

class JaxRsClientSpockMethodBuilderSpec extends Specification {

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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('def responseBody = (response.body.asString())')
			spockTest.contains('responseBody == "test"')
	}

}
