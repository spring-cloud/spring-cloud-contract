package io.codearte.accurest.dsl

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import io.codearte.accurest.util.AssertionUtil
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

class WireMockGroovyDslSpec extends Specification implements WireMockStubVerifier {

	def 'should convert groovy dsl stub to wireMock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(~/\/[0-9]{2}/), server('/12'))
				}
				response {
					status 200
					body(
							id: value(
									client('123'),
									server({ regex('[0-9]+') })
							),
							surname: $(
									client('Kowalsky'),
									server('Lewandowski')
							),
							name: 'Jan',
							created: $(client('2014-02-02 12:23:43'), server({ currentDate(it) }))
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
			AssertionUtil.assertThatJsonsAreEqual('''
			{
			  "request" : {
				"urlPattern" : "/[0-9]{2}",
				"method" : "GET"
			  },
			  "response" : {
				"status" : 200,
				"body" : "{\\"id\\":\\"123\\",\\"surname\\":\\"Kowalsky\\",\\"name\\":\\"Jan\\",\\"created\\":\\"2014-02-02 12:23:43\\"}",
				"headers" : {
				  "Content-Type" : "text/plain"
				}
			  }
			}
			''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#79")
	def 'should convert groovy dsl stub to wireMock stub for the client side with a body containing a map'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url '/ingredients'
					headers {
						header 'Content-Type': 'application/vnd.pl.devoxx.aggregatr.v1+json'
					}
				}
				response {
					status 200
					body(
							ingredients: [
									[type: 'MALT', quantity: 100],
									[type: 'WATER', quantity: 200],
									[type: 'HOP', quantity: 300],
									[type: 'YIEST', quantity: 400]
							]
					)
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
{
  "request" : {
	"url" : "/ingredients",
	"method" : "GET",
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "application/vnd.pl.devoxx.aggregatr.v1+json"
	  }
	}
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"ingredients\\":[{\\"type\\":\\"MALT\\",\\"quantity\\":100},{\\"type\\":\\"WATER\\",\\"quantity\\":200},{\\"type\\":\\"HOP\\",\\"quantity\\":300},{\\"type\\":\\"YIEST\\",\\"quantity\\":400}]}"
  }
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#86")
	def 'should convert groovy dsl stub with GString and regexp'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('POST')
					url('/ws/payments')
					headers {
						header("Content-Type": 'application/x-www-form-urlencoded')
					}
					body("""paymentType=INCOMING&transferType=BANK&amount=${
						value(client(regex('[0-9]{3}\\.[0-9]{2}')), server(500.00))
					}&bookingDate=${
						value(client(regex('[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])')), server('2015-05-18'))
					}""")
				}
				response {
					status 204
					body(
							paymentId: value(client('4'), server(regex('[1-9][0-9]*'))),
							foundExistingPayment: false
					)
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
{
	"request": {
		"method": "POST",
		"headers": {
			"Content-Type": {
				"equalTo": "application/x-www-form-urlencoded"
			}
		},
		"url": "/ws/payments",
		"bodyPatterns": [
			{
				"matches": "paymentType=INCOMING&transferType=BANK&amount=[0-9]{3}\\\\.[0-9]{2}&bookingDate=[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])"
			}
		]
	},
	"response": {
		"status": 204,
		"body": "{\\"paymentId\\":\\"4\\",\\"foundExistingPayment\\":false}"
	}
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with Body as String to wireMock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(~/\/[0-9]{2}/), server('/12'))
				}
				response {
					status 200
					body("""\
							{
								"id": "${value(client('123'), server('321'))}",
								"surname": "${value(client('Kowalsky'), server('Lewandowski'))}",
								"name": "Jan",
								"created" : "${$(client('2014-02-02 12:23:43'), server('2999-09-09 01:23:45'))}"
							}
						"""
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET"
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"created\\":\\"2014-02-02 12:23:43\\",\\"id\\":\\"123\\",\\"name\\":\\"Jan\\",\\"surname\\":\\"Kowalsky\\"}",
	"headers" : {
	  "Content-Type" : "text/plain"
	}
  }
}
'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with simple Body as String to wireMock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(regex('/[0-9]{2}')), server('/12'))
					body """
						{
							"name": "Jan"
						}
						"""
				}
				response {
					status 200
					body("""\
							{
								"name": "Jan"
							}
						"""
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.name == 'Jan')]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "text/plain"
	}
  }
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should use equalToJson when body match is defined as map'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(~/\/[0-9]{2}/), server('/12'))
					body(
							id: value(
									client('123'),
									server({ regex('[0-9]+') })
							),
							surname: $(
									client('Kowalsky'),
									server('Lewandowski')
							),
							name: 'Jan',
							created: $(client('2014-02-02 12:23:43'), server({ currentDate(it) }))
					)
				}
				response {
					status 200
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.created == '2014-02-02 12:23:43')]"
	}, {
	  "matchesJsonPath" : "$[?(@.surname == 'Kowalsky')]"
	}, {
	  "matchesJsonPath" : "$[?(@.name == 'Jan')]"
	}, {
	  "matchesJsonPath" : "$[?(@.id == '123')]"
	} ]
  },
  "response" : {
	"status" : 200
  }
}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should use equalToJson when content type ends with json'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					headers {
						header "Content-Type", "customtype/json"
					}
					body """
							{
								"name": "Jan"
							}
							"""
				}
				response {
					status 200
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"url" : "/users",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.name == 'Jan')]"
	} ],
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "customtype/json"
	  }
	}
  },
  "response" : {
	"status" : 200
  }
}
			'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml when content type ends with xml'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					headers {
						header "Content-Type", "customtype/xml"
					}
					body """<name>${value(client('Jozo'), server('Denis'))}</name><jobId>${
						value(client("<test>"), server('1234567890'))
					}</jobId>"""
				}
				response {
					status 200
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"url": "/users",
						"headers": {
							"Content-Type": {
								"equalTo": "customtype/xml"
							}
						},
						"bodyPatterns": [
							{
								"equalToXml":"<name>Jozo</name><jobId>&lt;test&gt;</jobId>"
							}
						]
					},
					"response": {
						"status": 200
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml when content type is parsable xml'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					body """<user><name>${value(client('Jozo'), server('Denis'))}</name><jobId>${
						value(client("<test>"), server('1234567890'))
					}</jobId></user>"""
				}
				response {
					status 200
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
					{
						"request": {
							"method": "GET",
							"url": "/users",
							"bodyPatterns": [
								{
									"equalToXml":"<user><name>Jozo</name><jobId>&lt;test&gt;</jobId></user>"
								}
							]
						},
						"response": {
							"status": 200
						}
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should support xml as a response body'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
				}
				response {
					status 200
					body """<user><name>${value(client('Jozo'), server('Denis'))}</name><jobId>${
						value(client("<test>"), server('1234567890'))
					}</jobId></user>"""
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
						{
							"request": {
								"method": "GET",
								"url": "/users"
							},
							"response": {
								"status": 200,
								"body":"<user><name>Jozo</name><jobId>&lt;test&gt;</jobId></user>"
							}
						}
						'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToJson'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					body equalToJson('''{"name":"Jan"}''')
				}
				response {
					status 200
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"url": "/users",
						"bodyPatterns": [
							{
								"equalToJson":"{\\"name\\":\\"Jan\\"}"
							}
						]
					},
					"response": {
						"status": 200
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					body equalToXml("""<name>${value(client('Jozo'), server('Denis'))}</name><jobId>${
						value(client("<test>"), server('1234567890'))
					}</jobId>""")
				}
				response {
					status 200
				}
			}
		when:
			String json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
					{
						"request": {
							"method": "GET",
							"url": "/users",
							"bodyPatterns": [
								{
									"equalToXml":"<name>Jozo</name><jobId>&lt;test&gt;</jobId>"
								}
							]
						},
						"response": {
							"status": 200
						}
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should convert groovy dsl stub with regexp Body as String to wireMock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(regex('/[0-9]{2}')), server('/12'))
					body """
						{
							"personalId": "${value(client(regex('^[0-9]{11}$')), server('57593728525'))}"
						}
						"""
				}
				response {
					status 200
					body("""\
							{
								"name": "Jan"
							}
					 """
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.personalId =~ /^[0-9]{11}$/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "text/plain"
	}
  }
}
'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with a regexp and an integer in request body'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'PUT'
					url '/fraudcheck'
					body("""
						{
						"clientPesel":"${value(client(regex('[0-9]{10}')), server('1234567890'))}",
						"loanAmount":123.123
						}
					"""
					)
					headers {
						header('Content-Type', 'application/vnd.fraud.v1+json')
					}

				}
				response {
					status 200
					body(
							fraudCheckStatus: "OK",
							rejectionReason: $(client(null), server(execute('assertThatRejectionReasonIsNull($it)')))
					)
					headers {
						header('Content-Type': 'application/vnd.fraud.v1+json')
					}
				}

			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"url" : "/fraudcheck",
	"method" : "PUT",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.loanAmount == 123.123)]"
	}, {
	  "matchesJsonPath" : "$[?(@.clientPesel =~ /[0-9]{10}/)]"
	} ],
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "application/vnd.fraud.v1+json"
	  }
	}
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"fraudCheckStatus\\":\\"OK\\",\\"rejectionReason\\":null}",
	"headers" : {
	  "Content-Type" : "application/vnd.fraud.v1+json"
	}
  }
}
'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def "should generate request with urlPath and queryParameters for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					urlPath($(client("users"), server("items"))) {
						queryParameters {
							parameter 'limit': $(client(equalTo("20")), server("10"))
							parameter 'offset': $(client(containing("10")), server("10"))
							parameter 'filter': "email"
							parameter 'sort': $(client(~/^[0-9]{10}$/), server("1234567890"))
							parameter 'search': $(client(notMatching(~/^\/[0-9]{2}$/)), server("10"))
							parameter 'age': $(client(notMatching("^\\w*\$")), server(10))
							parameter 'name': $(client(matching("Denis.*")), server("Denis"))
							parameter 'credit': absent()
						}
					}
				}
				response {
					status 200
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
				"request": {
					"method": "GET",
					"urlPath": "users",
					"queryParameters": {
					  "offset": {
						"contains": "10"
					  },
					  "limit": {
						"equalTo": "20"
					  },
					  "filter": {
						"equalTo": "email"
					  },
					  "sort": {
						"matches": "^[0-9]{10}$"
					  },
					  "search": {
						"doesNotMatch": "^/[0-9]{2}$"
					  },
					  "age": {
						"doesNotMatch": "^\\\\w*$"
					  },
					  "name": {
						"matches": "Denis.*"
					  },
					  "credit": {
					  	 "absent": true
					  }
					}
				},
				"response": {
					"status": 200,
				}
			}
			'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def "should generate request with urlPath for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					urlPath $(client("boxes"), server("items"))
				}
				response {
					status 200
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"urlPath": "boxes"
					},
					"response": {
						"status": 200,
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def "should generate simple request with urlPath for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					urlPath "boxes"
				}
				response {
					status 200
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"urlPath": "boxes"
					},
					"response": {
						"status": 200,
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def "should not allow regexp in url for server value"() {
		when:
			GroovyDsl.make {
				request {
					method 'GET'
					url(regex(/users\/[0-9]*/)) {
						queryParameters {
							parameter 'age': notMatching("^\\w*\$")
							parameter 'name': matching("Denis.*")
						}
					}
				}
				response {
					status 200
				}
			}
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Url can't be a pattern for the server side"
	}

	def "should not allow regexp in query parameter for server value"() {
		when:
			GroovyDsl.make {
				request {
					method 'GET'
					url("abc") {
						queryParameters {
							parameter 'age': $(client(notMatching("^\\w*\$")), server(regex(".*")))
						}
					}
				}
				response {
					status 200
				}
			}
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Query parameter 'age' can't be a pattern for the server side"
	}

	def "should not allow query parameter unresolvable for a server value"() {
		when:
			GroovyDsl.make {
				request {
					method 'GET'
					urlPath("users") {
						queryParameters {
							parameter 'age': notMatching("^\\w*\$")
							parameter 'name': matching("Denis.*")
						}
					}
				}
				response {
					status 200
				}
			}
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Query parameter 'age' can't be of a matching type: NOT_MATCHING for the server side"
	}

	def "should not allow query parameter with a different absent variation for server/client"() {
		when:
			GroovyDsl.make dsl
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Absent cannot only be used only on one side"
		where:
			dsl << [
					{
						request {
							method 'GET'
							urlPath("users") {
								queryParameters {
									parameter 'name': $(client(absent()), server(""))
								}
							}
						}
						response {
							status 200
						}
					},
					{
						request {
							method 'GET'
							urlPath("users") {
								queryParameters {
									parameter 'name': $(client(""), server(absent()))
								}
							}
						}
						response {
							status 200
						}
					},
					{
						request {
							method 'GET'
							urlPath("users") {
								queryParameters {
									parameter 'name': $(client(absent()), server(matching("abc")))
								}
							}
						}
						response {
							status 200
						}
					}
			]
	}

	def "should generate request with url and queryParameters for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url($(client(regex(/users\/[0-9]*/)), server("users/123"))) {
						queryParameters {
							parameter 'age': $(client(notMatching("^\\w*\$")), server(10))
							parameter 'name': $(client(matching("Denis.*")), server("Denis"))
						}
					}
				}
				response {
					status 200
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"urlPattern": "users/[0-9]*",
						"queryParameters": {
						  "age": {
							"doesNotMatch": "^\\\\w*$"
						  },
						  "name": {
							"matches": "Denis.*"
						  }
						}
					},
					"response": {
						"status": 200,
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should convert groovy dsl stub with rich tree Body as String to wireMock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					url $(client(~/\/[0-9]{2}/), server('/12'))
					body """\
						{
						  "personalId": "${value(client(regex('[0-9]{11}')), server('57593728525'))}",
						  "firstName": "${value(client(regex('.*')), server('Bruce'))}",
						  "lastName": "${value(client(regex('.*')), server('Lee'))}",
						  "birthDate": "${value(client(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}')), server('1985-12-12'))}",
						  "errors": [
									{
									  "propertyName": "${value(client(regex('[0-9]{2}')), server('04'))}",
									  "providerValue": "Test"
									},
									{
									  "propertyName": "${value(client(regex('[0-9]{2}')), server('08'))}",
									  "providerValue": "Test"
									}
								  ]
						}
						"""
				}
				response {
					status 200
					body("""\
								{
									"name": "Jan"
								}
							"""
					)
					headers {
						header 'Content-Type': 'text/plain'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$.errors[*][?(@.propertyName =~ /[0-9]{2}/)]"
	}, {
	  "matchesJsonPath" : "$.errors[*][?(@.providerValue == 'Test')]"
	}, {
	  "matchesJsonPath" : "$.errors[*][?(@.providerValue == 'Test')]"
	}, {
	  "matchesJsonPath" : "$[?(@.lastName =~ /.*/)]"
	}, {
	  "matchesJsonPath" : "$.errors[*][?(@.propertyName =~ /[0-9]{2}/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.birthDate =~ /[0-9]{4}-[0-9]{2}-[0-9]{2}/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.personalId =~ /[0-9]{11}/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.firstName =~ /.*/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "text/plain"
	}
  }
}
			'''), wireMockStub)
	}

	def 'should use regexp matches when request body match is defined using a map with a pattern'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/reissue-payment-order'
					body(
							loanNumber: "999997001",
							amount: value(client(regex('[0-9.]+')), server('100.00')),
							currency: "DKK",
							applicationName: value(client(regex('.*')), server("Auto-Repayments")),
							username: value(client(regex('.*')), server("scheduler")),
							cardId: 1
					)
				}
				response {
					status 200
					body '''
						{
						"status": "OK"
						}
					'''
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"url" : "/reissue-payment-order",
	"method" : "POST",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.loanNumber == '999997001')]"
	}, {
	  "matchesJsonPath" : "$[?(@.username =~ /.*/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.amount =~ /[0-9.]+/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.cardId == 1)]"
	}, {
	  "matchesJsonPath" : "$[?(@.currency == 'DKK')]"
	}, {
	  "matchesJsonPath" : "$[?(@.applicationName =~ /.*/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"status\\":\\"OK\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	}
  }
}
				'''), json)
	}

	def "should generate stub for empty body"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('POST')
					url("test")
					body("")
				}
				response {
					status 406
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
		"request": {
		  "method": "POST",
		  "url": "test",
		  "bodyPatterns": [
			{
				"equalTo": ""
			}
		  ]
		},
		"response": {
		  "status": 406
		}
			}
'''), json)
	}

	def "should generate stub with priority"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				priority 9
				request {
					method('POST')
					url("test")
				}
				response {
					status 406
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"priority": 9,
					"request": {
						"method": "POST",
						"url": "test"
					},
					"response": {
						"status": 406
					}
				}
			'''), json)
	}

	@Issue("#127")
	def 'should use "test" as an alias for "server"'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('POST')
					body(
							property: value(stub("value"), test("value"))
					)
				}
				response {
					status 200
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"method" : "POST",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.property == 'value')]"
	} ]
  },
  "response" : {
	"status" : 200
  }
}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#121")
	def 'should generate stub with empty list as a value of a field'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('POST')
					body(
							values: []
					)
				}
				response {
					status 200
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "POST",
						"bodyPatterns": [
							{
								"equalToJson": "{\\"values\\":[]}"
							}
						]
					},
					"response": {
						"status": 200
					}
				}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should generate stub properly resolving GString with regular expression'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
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
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
		{
		  "request" : {
			"url" : "/users/password",
			"method" : "POST",
			"bodyPatterns" : [ {
			  "matchesJsonPath" : "$[?(@.callback_url =~ /((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?/)]"
			}, {
			  "matchesJsonPath" : "$[?(@.email =~ /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4}/)]"
			} ],
			"headers" : {
			  "Content-Type" : {
				"equalTo" : "application/json"
			  }
			}
		  },
		  "response" : {
			"status" : 404,
			"body" : "{\\"code\\":4,\\"message\\":\\"User not found by email = [not.existing@user.com]\\"}",
			"headers" : {
			  "Content-Type" : "application/json"
			}
		  },
		  "priority" : 1
		}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should generate stub properly resolving GString with regular expression in url'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {

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
		when:
			String wireMockStub = new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
		{
		  "request" : {
			"urlPattern" : "/partners/^[0-9]*$/agents/11/customers/09665703Z",
			"method" : "PUT",
			"bodyPatterns" : [ {
			  "matchesJsonPath" : "$[?(@.first_name == 'Josef')]"
			} ],
			"headers" : {
			  "Content-Type" : {
				"equalTo" : "application/json"
			  }
			}
		  },
		  "response" : {
			"status" : 422
		  }
		}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('42')
	@Unroll
	def 'should generate stub without optional parameters'() {
		when:
			String wireMockStub = new WireMockStubStrategy(contractDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
			  "request" : {
				"url" : "/users/password",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "matchesJsonPath" : "$[?(@.callback_url =~ /((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?/)]"
				}, {
				  "matchesJsonPath" : "$[?(@.email =~ /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4})?/)]"
				} ],
				"headers" : {
				  "Content-Type" : {
					"equalTo" : "application/json"
				  }
				}
			  },
			  "response" : {
				"status" : 404,
				"body" : "{\\"code\\":\\"123123\\",\\"message\\":\\"User not found by email = [not.existing@user.com]\\"}",
				"headers" : {
				  "Content-Type" : "application/json"
				}
			  },
			  "priority" : 1
			}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
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
								code: $(stub("123123"), test(optional("123123"))),
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

	String toJsonString(value) {
		new JsonBuilder(value).toPrettyString()
	}

	Object parseJson(json) {
		new JsonSlurper().parseText(json)
	}

	String toWireMockClientJsonStub(groovyDsl) {
		new WireMockStubStrategy(groovyDsl).toWireMockClientStub()
	}

	@Issue('180')
	@Unroll
	def 'should generate stub with multipart parameters'() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "PUT"
					url "/multipart"
					multipart(
							formParameter: value(client(regex('".+"')), server('"formParameterValue"')),
							someBooleanParameter: value(client(regex('(true|false)')), server('true')),
							file: named(
									name: value(client(regex('.+')), server('filename.csv')),
									content: value(client(regex('.+')), server('file content')))
					)
				}
				response {
					status 200
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy(contractDsl).toWireMockClientStub()
		then:
			println wireMockStub
			AssertionUtil.assertThatJsonsAreEqual(('''
		{
		  "request" : {
			"url" : "/multipart",
			"method" : "PUT",
			"bodyPatterns" : [ {
				"matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"formParameter\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n\\".+\\"\\r\\n--\\\\1.*"
    		}, {
      			"matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"someBooleanParameter\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n(true|false)\\r\\n--\\\\1.*"
    		}, {			
			  "matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"file\\"; filename=\\".+\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n.+\\r\\n--\\\\1.*"
			} ]
		  },
		  "response" : {
			"status" : 200
		  }
		}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}
}
