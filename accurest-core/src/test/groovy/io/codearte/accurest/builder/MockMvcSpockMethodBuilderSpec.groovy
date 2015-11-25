package io.codearte.accurest.builder
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.WireMockStubStrategy
import io.codearte.accurest.dsl.WireMockStubVerifier
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern
/**
 * @author Jakub Kubrynski
 */
class MockMvcSpockMethodBuilderSpec extends Specification implements WireMockStubVerifier {

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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('''{\"items\":[\"HOP\"]}''')")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('''property1=VAL1''')")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
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
		MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		then:
		blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
		blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
		stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue(["#126", "#143"])
	def "should generate escaped regex assertions for string objects in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body("""{"property":"  ${value(client('123'), server(regex('\\d+')))}"}""")
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
			blockBuilder.toString().contains("\$[?(@.property =~ /\\d+/)]")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('get("/users?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('get("/foo/123456?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains(".body('''''')")
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('def responseBody = (response.body.asString())')
			spockTest.contains('responseBody == "test"')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should work properly with GString url"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {

				request {
					method 'PUT'
					url "/partners/${value(client(regex('^[0-9]*$')), server('11'))}/agents/11/customers/09665703Z"
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''/partners/11/agents/11/customers/09665703Z''')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should resolve properties in GString with regular expression"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				priority 1
				request {
					method 'POST'
					url '/users/password'
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							email: $(client(regex(email())), server('not.existing@user.com')),
							callback_url: $(client(regex(hostname())), server('http://partners.com'))
					)
				}
				response {
					status 404
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							code: 4,
							message: "User not found by email = [${value(server(regex(email())), client('not.existing@user.com'))}]"
					)
				}
			}
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''$[?(@.message =~ /User not found by email = \\\\[[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4}\\\\]/)]''')
	}

	@Issue('42')
	@Unroll
	def "should not omit the optional field in the test creation"() {
		given:
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''"email":"abc@abc.com"''')
			spockTest.contains('''parsedJson.read(\'\'\'$[?(@.code =~ /(123123)?/)]''')
			!spockTest.contains('''REGEXP''')
			!spockTest.contains('''OPTIONAL''')
			!spockTest.contains('''OptionalProperty''')
		where:
		contractDsl << [
				GroovyDsl.make {
					priority 1
					request {
						method 'POST'
						url '/users/password'
						headers {
							header 'Content-Type': 'application/json'
						}
						body(
								email: $(stub(optional(regex(email()))), test('abc@abc.com')),
								callback_url: $(stub(regex(hostname())), test('http://partners.com'))
						)
					}
					response {
						status 404
						headers {
							header 'Content-Type': 'application/json'
						}
						body(
								code: value(stub("123123"), test(optional("123123"))),
								message: "User not found by email = [${value(test(regex(email())), stub('not.existing@user.com'))}]"
						)
					}
				},
				GroovyDsl.make {
					priority 1
					request {
						method 'POST'
						url '/users/password'
						headers {
							header 'Content-Type': 'application/json'
						}
						body(
								""" {
								"email" : "${value(stub(optional(regex(email()))), test('abc@abc.com'))}",
								"callback_url" : "${value(client(regex(hostname())), server('http://partners.com'))}"
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
								"code" : "${value(stub(123123), test(optional(123123)))}",
								"message" : "User not found by email = [${value(server(regex(email())), client('not.existing@user.com'))}]"
								}
							"""
						)
					}
				}
		]
	}

	@Issue('72')
	def "should make the execute method work"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method """PUT"""
					url """/fraudcheck"""
					body("""
                        {
                        "clientPesel":"${value(client(regex('[0-9]{10}')), server('1234567890'))}",
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
					body( """{
    "fraudCheckStatus": "OK",
    "rejectionReason": ${value(client(null), server(execute('assertThatRejectionReasonIsNull($it)')))}
}""")
					headers {
						header('Content-Type': 'application/vnd.fraud.v1+json')

					}

				}

			}
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains('''assertThatRejectionReasonIsNull(parsedJson.read('$.rejectionReason'))''')
	}

	def "should support inner map and list definitions"() {
		given:

			Pattern PHONE_NUMBER = Pattern.compile(/[+\w]*/)
			Pattern ANYSTRING = Pattern.compile(/.*/)
			Pattern NUMBERS = Pattern.compile(/[\d\.]*/)
			Pattern DATETIME = ANYSTRING

			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "PUT"
					url "/v1/payments/e86df6f693de4b35ae648464c5b0dc09/client_data"
					headers {
						header('Content-Type': 'application/json')
					}
					body(
							client: [
									first_name: $(stub(regex(onlyAlphaUnicode())), test('Denis')),
									last_name: $(stub(regex(onlyAlphaUnicode())), test('FakeName')),
									email: $(stub(regex(email())), test('fakemail@fakegmail.com')),
									fax: $(stub(PHONE_NUMBER), test('+xx001213214')),
									phone: $(stub(PHONE_NUMBER), test('2223311')),
									data_of_birth: $(stub(DATETIME), test('2002-10-22T00:00:00Z'))
							],
							client_id_card: [
									id: $(stub(ANYSTRING), test('ABC12345')),
									date_of_issue: $(stub(ANYSTRING), test('2002-10-02T00:00:00Z')),
									address: [
											street: $(stub(ANYSTRING), test('Light Street')),
											city: $(stub(ANYSTRING), test('Fire')),
											region: $(stub(ANYSTRING), test('Skys')),
											country: $(stub(ANYSTRING), test('HG')),
											zip: $(stub(NUMBERS), test('658965'))
									]
							],
							incomes_and_expenses: [
									monthly_income: $(stub(NUMBERS), test('0.0')),
									monthly_loan_repayments: $(stub(NUMBERS), test('100')),
									monthly_living_expenses: $(stub(NUMBERS), test('22'))
							],
							additional_info: [
									allow_to_contact: $(stub(optional(regex(anyBoolean()))), test('true'))
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains '"street":"Light Street"'
			!spockTest.contains("clientValue")
			!spockTest.contains("cursor")
	}


	def "shouldn't generate unicode escape characters"() {
		given:
			Pattern ONLY_ALPHA_UNICODE = Pattern.compile(/[\p{L}]*/)

			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "PUT"
					url "/v1/payments/e86df6f693de4b35ae648464c5b0dc09/енев"
					headers {
						header('Content-Type': 'application/json')
					}
					body(
							client: [
									first_name: $(stub(ONLY_ALPHA_UNICODE), test('Пенева')),
									last_name : $(stub(ONLY_ALPHA_UNICODE), test('Пенева'))
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			!spockTest.contains("\\u041f")
	}

	@Issue('177')
	def "should generate proper test code when having multiline body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
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
			MockMvcSpockMethodBodyBuilder builder = new MockMvcSpockMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.given(blockBuilder)
			def spockTest = blockBuilder.toString()
		then:
			spockTest.contains("""'''hello,
World.'''""")
	}

}
