package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Issue
import spock.lang.Specification

/**
 * @author Jakub Kubrynski
 */
class MockMvcSpockMethodBuilderSpec extends Specification {

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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('{\"items\":[\"HOP\"]}')")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('property1=VAL1')")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('get("/users?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains(".body('')")
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

	@Issue('113')
	def "should generate regex test for String in response header"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'POST'
					url $(client(regex('/partners/[0-9]+/users')), server('/partners/1000/users'))
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
						header 'Location': $(client('http://localhost/partners/1000/users/1001'), server(regex('http://localhost/partners/[0-9]+/users/[0-9]+')))
					}
				}
			}
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''response.header('Location') ==~ java.util.regex.Pattern.compile('http://localhost/partners/[0-9]+/users/[0-9]+')''')
	}

	@Issue('115')
	def "should generate regex with helper method"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'POST'
					url $(client(regex('/partners/[0-9]+/users')), server('/partners/1000/users'))
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
						header 'Location': $(client('http://localhost/partners/1000/users/1001'), server(regex("^${hostname()}/partners/[0-9]+/users/[0-9]+")))
					}
				}
			}
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''response.header('Location') ==~ java.util.regex.Pattern.compile('^((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?/partners/[0-9]+/users/[0-9]+')''')
	}

	def "should work with more complex stuff and jsonpaths"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''$.errors[*][?(@.property == 'bank_account_number')]''')
			spockTest.contains('''$.errors[*][?(@.message == 'incorrect_format')]''')
	}
}
