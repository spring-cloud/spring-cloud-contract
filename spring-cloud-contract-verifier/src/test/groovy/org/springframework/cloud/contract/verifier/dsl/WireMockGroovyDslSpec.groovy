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

package org.springframework.cloud.contract.verifier.dsl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Issue
import spock.lang.Specification

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsEscapeHelper
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubMapping
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubStrategy
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.AssertionUtil
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.SocketUtils

class WireMockGroovyDslSpec extends Specification implements WireMockStubVerifier {

	def 'should convert groovy dsl stub to wireMock stub for the client side'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(~/\/[0-9]{2}/), producer('/12'))
				}
				response {
					status OK()
					body(
							id: value(
									consumer('123'),
									producer(regex('[0-9]+'))
							),
							surname: $(
									consumer('Kowalsky'),
									producer(regex('[a-zA-Z]+'))
							),
							name: 'Jan',
							created: $(consumer('2014-02-02 12:23:43'), producer(execute('currentDate($it)')))
					)
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			AssertionUtil.assertThatJsonsAreEqual('''
			{
			  "request" : {
				"urlPattern" : "/[0-9]{2}",
				"method" : "GET"
			  },
			  "response" : {
				"status" : 200,
				"body" : "{\\"surname\\":\\"Kowalsky\\",\\"created\\":\\"2014-02-02 12:23:43\\",\\"name\\":\\"Jan\\",\\"id\\":\\"123\\"}",
				"headers" : {
				  "Content-Type" : "application/json"
				},
				"transformers" : [ "response-template", "foo-transformer" ]
			  }
			}
			''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#79")
	def 'should convert groovy dsl stub to wireMock stub for the client side with a body containing a map'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url '/ingredients'
					headers {
						header 'Content-Type': 'application/vnd.pl.devoxx.aggregatr.v1+json'
					}
				}
				response {
					status OK()
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
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
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
	"body" : "{\\"ingredients\\":[{\\"quantity\\":100,\\"type\\":\\"MALT\\"},{\\"quantity\\":200,\\"type\\":\\"WATER\\"},{\\"quantity\\":300,\\"type\\":\\"HOP\\"},{\\"quantity\\":400,\\"type\\":\\"YIEST\\"}]}",
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#86")
	def 'should convert groovy dsl stub with GString and regexp'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('POST')
					url('/ws/payments')
					headers {
						header("Content-Type": 'application/x-www-form-urlencoded')
					}
					body("""paymentType=INCOMING&transferType=BANK&amount=${
						value(consumer(regex('[0-9]{3}\\.[0-9]{2}')), producer(500.00))
					}&bookingDate=${
						value(consumer(regex('[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])')), producer('2015-05-18'))
					}""")
				}
				response {
					status 204
					body(
							paymentId: value(consumer('4'), producer(regex('[1-9][0-9]*'))),
							foundExistingPayment: false
					)
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
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
		"body": "{\\"foundExistingPayment\\":false,\\"paymentId\\":\\"4\\"}",
		"transformers" : [ "response-template", "foo-transformer" ]
	}
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with Body as String to wireMock stub for the client side'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(~/\/[0-9]{2}/), producer('/12'))
				}
				response {
					status OK()
					body("""\
							{
								"id": "${value(consumer('123'), producer('321'))}",
								"surname": "${value(consumer('Kowalsky'), producer(regex('[a-zA-Z]+')))}",
								"name": "Jan",
								"created" : "${$(consumer('2014-02-02 12:23:43'), producer('2999-09-09 01:23:45'))}"
							}
						"""
					)
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			def actual = new JsonSlurper().parseText(wireMockStub)
			def expected = new JsonSlurper().parseText('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET"
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"id\\":\\"123\\",\\"surname\\":\\"Kowalsky\\",\\"name\\":\\"Jan\\",\\"created\\":\\"2014-02-02 12:23:43\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
''')
			actual.request == expected.request
			actual.response.status == expected.response.status
			actual.response.headers == expected.response.headers
			def actualBody = new JsonSlurper().parseText(actual.response.body)
			def expectedBody = new JsonSlurper().parseText(expected.response.body)
			actualBody == expectedBody
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with simple Body as String to wireMock stub for the client side'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(regex('/[0-9]{2}')), producer('/12'))
					body """
						{
							"name": "Jan"
						}
						"""
				}
				response {
					status OK()
					body("""\
							{
								"name": "Jan"
							}
						"""
					)
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.['name'] == 'Jan')]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should use equalToJson when body match is defined as map'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(~/\/[0-9]{2}/), producer('/12'))
					body(
							id: value(
									consumer(regex('[0-9]+')),
									producer('123'),
							),
							surname: $(
									consumer(regex('[a-zA-Z]+')),
									producer('Kowalsky'),
							),
							name: 'Jan',
							created: $(consumer('2014-02-02 12:23:43'), producer(execute('currentDate($it)')))
					)
				}
				response {
					status OK()
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.['created'] == '2014-02-02 12:23:43')]"
	}, {
	  "matchesJsonPath" : "$[?(@.['surname'] =~ /[a-zA-Z]+/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['name'] == 'Jan')]"
	}, {
	  "matchesJsonPath" : "$[?(@.['id'] =~ /[0-9]+/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should use equalToJson when content type ends with json'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
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
					status OK()
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
	  "matchesJsonPath" : "$[?(@.['name'] == 'Jan')]"
	} ],
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "customtype/json"
	  }
	}
  },
  "response" : {
	"status" : 200,
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
			'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml when content type ends with xml'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url "/users"
					headers {
						header "Content-Type", "customtype/xml"
					}
					body """<foo><name>${value(consumer('Jozo'), producer('Denis'))}</name><jobId>${
						value(consumer("&lt;test&gt;"), producer('1234567890'))
					}</jobId></foo>"""
				}
				response {
					status OK()
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
								"equalToXml":"<foo><name>Jozo</name><jobId>&lt;test&gt;</jobId></foo>"
							}
						]
					},
					"response": {
						"status": 200,
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml when content type is parsable xml'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url "/users"
					body """<user><name>${value(consumer('Jozo'), producer('Denis'))}</name><jobId>${
						value(consumer("&lt;test&gt;"), producer('1234567890'))
					}</jobId></user>"""
				}
				response {
					status OK()
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
							"status": 200,
							"transformers" : [ "response-template", "foo-transformer" ]
						}
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should support xml as a response body'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url "/users"
				}
				response {
					status OK()
					body """<user><name>${value(consumer('Jozo'), producer('Denis'))}</name><jobId>${
						value(consumer("<test>"), producer('1234567890'))
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
								"body":"<user><name>Jozo</name><jobId>&lt;test&gt;</jobId></user>",
								"transformers" : [ "response-template", "foo-transformer" ]
							}
						}
						'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToJson'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url "/users"
					body equalToJson('''{"name":"Jan"}''')
				}
				response {
					status OK()
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
						"status": 200,
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should use equalToXml'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url "/users"
					body equalToXml("""<foo><name>${value(consumer('Jozo'), producer('Denis'))}</name><jobId>${
						value(consumer("&lt;test&gt;"), producer('1234567890'))
					}</jobId></foo>""")
				}
				response {
					status OK()
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
									"equalToXml":"<foo><name>Jozo</name><jobId>&lt;test&gt;</jobId></foo>"
								}
							]
						},
						"response": {
							"status": 200,
							"transformers" : [ "response-template", "foo-transformer" ]
						}
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should convert groovy dsl stub with regexp Body as String to wireMock stub for the client side'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(regex('/[0-9]{2}')), producer('/12'))
					body """
						{
							"personalId": "${value(consumer(regex('^[0-9]{11}$')), producer('57593728525'))}"
						}
						"""
				}
				response {
					status OK()
					body("""\
							{
								"name": "Jan"
							}
					 """
					)
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.['personalId'] =~ /^[0-9]{11}$/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should convert groovy dsl stub with a regexp and an integer in request body'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'PUT'
					url '/fraudcheck'
					body("""
						{
						"clientPesel":"${value(consumer(regex('[0-9]{10}')), producer('1234567890'))}",
						"loanAmount":123.123
						}
					"""
					)
					headers {
						header('Content-Type', 'application/vnd.fraud.v1+json')
					}

				}
				response {
					status OK()
					body(
							fraudCheckStatus: "OK",
							rejectionReason: $(consumer(null), producer(execute('assertThatRejectionReasonIsNull($it)')))
					)
					headers {
						header('Content-Type': 'application/vnd.fraud.v1+json')
					}
				}

			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"url" : "/fraudcheck",
	"method" : "PUT",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.['loanAmount'] == 123.123)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['clientPesel'] =~ /[0-9]{10}/)]"
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
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def "should generate request with urlPath and queryParameters for client side"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					urlPath($(consumer("users"), producer("items"))) {
						queryParameters {
							parameter 'limit': $(consumer(equalTo("20")), producer("10"))
							parameter 'offset': $(consumer(containing("10")), producer("10"))
							parameter 'filter': "email"
							parameter 'sort': $(consumer(~/^[0-9]{10}$/), producer("1234567890"))
							parameter 'search': $(consumer(notMatching(~/^\/[0-9]{2}$/)), producer("10"))
							parameter 'age': $(consumer(notMatching("^\\w*\$")), producer(10))
							parameter 'name': $(consumer(matching("Denis.*")), producer("Denis"))
							parameter 'credit': absent()
						}
					}
				}
				response {
					status OK()
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
					"transformers" : [ "response-template", "foo-transformer" ]
				}
			}
			'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	@Issue("#353")
	def "should generate request with absent header for client side"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					urlPath( '/some/path/*')
					headers {
						header('''Authentication''', absent())
					}
				}
				response {
					status OK()
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
			  "request" : {
				"urlPath" : "/some/path/*",
				"method" : "GET",
				"headers" : {
				  "Authentication" : {
					"absent" : true
				  }
				}
			  },
			  "response" : {
				"status" : 200,
				"transformers" : [ "response-template", "foo-transformer" ]
			  }
			}
			'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	@Issue('#230')
	def "should generate request with urlPathPattern and queryParameters for client side\
			when both contains regular expressions"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'GET'
				urlPath($(consumer(regex("/users/[0-9]+")), producer("/users/1"))) {
					queryParameters {
						parameter 'search': $(consumer(notMatching(~/^\/[0-9]{2}$/)), producer("10"))
					}
				}
			}
			response {
				status OK()
			}
		}
		when:
		def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
				"request": {
					"method": "GET",
					"urlPathPattern": "/users/[0-9]+",
					"queryParameters": {
					  "search": {
						"doesNotMatch": "^/[0-9]{2}$"
					  }
					}
				},
				"response": {
					"status": 200,
					"transformers" : [ "response-template", "foo-transformer" ]
				}
			}
			'''), json)
		and:
		stubMappingIsValidWireMockStub(json)
	}


	def "should generate request with urlPath for client side"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					urlPath $(consumer("boxes"), producer("items"))
				}
				response {
					status OK()
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
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def "should generate simple request with urlPath for client side"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					urlPath "boxes"
				}
				response {
					status OK()
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
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def "should not allow regexp in url for server value"() {
		when:
			org.springframework.cloud.contract.spec.Contract.make {
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
					status OK()
				}
			}
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Url can't be a pattern for the server side"
	}

	def "should not allow regexp in query parameter for server value"() {
		when:
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url("abc") {
						queryParameters {
							parameter 'age': $(consumer(notMatching("^\\w*\$")), producer(regex(".*")))
						}
					}
				}
				response {
					status OK()
				}
			}
		then:
			thrown(IllegalStateException)
	}

	def "should not allow query parameter unresolvable for a server value"() {
		when:
			org.springframework.cloud.contract.spec.Contract.make {
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
					status OK()
				}
			}
		then:
			def e = thrown(IllegalStateException)
			e.message.contains "Query parameter 'age' can't be of a matching type: NOT_MATCHING for the server side"
	}

	def "should not allow query parameter with a different absent variation for server/client"() {
		when:
			org.springframework.cloud.contract.spec.Contract.make dsl
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
									parameter 'name': $(consumer(absent()), producer(""))
								}
							}
						}
						response {
							status OK()
						}
					},
					{
						request {
							method 'GET'
							urlPath("users") {
								queryParameters {
									parameter 'name': $(consumer(""), producer(absent()))
								}
							}
						}
						response {
							status OK()
						}
					},
					{
						request {
							method 'GET'
							urlPath("users") {
								queryParameters {
									parameter 'name': $(consumer(absent()), producer(matching("abc")))
								}
							}
						}
						response {
							status OK()
						}
					}
			]
	}

	def "should generate request with url and queryParameters for client side"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'GET'
					url($(consumer(regex(/users\/[0-9]*/)), producer("users/123"))) {
						queryParameters {
							parameter 'age': $(consumer(notMatching("^\\w*\$")), producer(10))
							parameter 'name': $(consumer(matching("Denis.*")), producer("Denis"))
						}
					}
				}
				response {
					status OK()
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request": {
						"method": "GET",
						"urlPathPattern": "users/[0-9]*",
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
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
				'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	def 'should convert groovy dsl stub with rich tree Body as String to wireMock stub for the client side'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('GET')
					url $(consumer(~/\/[0-9]{2}/), producer('/12'))
					body """\
						{
						  "personalId": "${value(consumer(regex('[0-9]{11}')), producer('57593728525'))}",
						  "firstName": "${value(consumer(regex('.*')), producer('Bruce'))}",
						  "lastName": "${value(consumer(regex('.*')), producer('Lee'))}",
						  "birthDate": "${value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}')), producer('1985-12-12'))}",
						  "errors": [
									{
									  "propertyName": "${value(consumer(regex('[0-9]{2}')), producer('04'))}",
									  "providerValue": "Test"
									},
									{
									  "propertyName": "${value(consumer(regex('[0-9]{2}')), producer('08'))}",
									  "providerValue": "Test"
									}
								  ]
						}
						"""
				}
				response {
					status OK()
					body("""\
								{
									"name": "Jan"
								}
							"""
					)
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"urlPattern" : "/[0-9]{2}",
	"method" : "GET",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$.['errors'][*][?(@.['propertyName'] =~ /[0-9]{2}/)]"
	}, {
	  "matchesJsonPath" : "$.['errors'][*][?(@.['providerValue'] == 'Test')]"
	}, {
	  "matchesJsonPath" : "$[?(@.['lastName'] =~ /.*/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['firstName'] =~ /.*/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['birthDate'] =~ /[0-9]{4}-[0-9]{2}-[0-9]{2}/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['personalId'] =~ /[0-9]{11}/)]"
	}]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"name\\":\\"Jan\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
			'''), wireMockStub)
	}

	def 'should use regexp matches when request body match is defined using a map with a pattern'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'POST'
					url '/reissue-payment-order'
					body(
							loanNumber: "999997001",
							amount: value(consumer(regex('[0-9.]+')), producer('100.00')),
							currency: "DKK",
							applicationName: value(consumer(regex('.*')), producer("Auto-Repayments")),
							username: value(consumer(regex('.*')), producer("scheduler")),
							cardId: 1
					)
				}
				response {
					status OK()
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
	  "matchesJsonPath" : "$[?(@.['loanNumber'] == '999997001')]"
	}, {
	  "matchesJsonPath" : "$[?(@.['username'] =~ /.*/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['amount'] =~ /[0-9.]+/)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['cardId'] == 1)]"
	}, {
	  "matchesJsonPath" : "$[?(@.['currency'] == 'DKK')]"
	}, {
	  "matchesJsonPath" : "$[?(@.['applicationName'] =~ /.*/)]"
	} ]
  },
  "response" : {
	"status" : 200,
	"body" : "{\\"status\\":\\"OK\\"}",
	"headers" : {
	  "Content-Type" : "application/json"
	},
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
				'''), json)
	}

	def "should generate stub for empty body"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
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
		  "status": 406,
		  "transformers" : [ "response-template", "foo-transformer" ]
		}
			}
'''), json)
	}

	def "should generate stub with priority"() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
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
						"status": 406,
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
			'''), json)
	}

	@Issue("#127")
	def 'should use "test" as an alias for "server"'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('POST')
					url("foo")
					body(
							property: value(consumer("value"), producer("value"))
					)
				}
				response {
					status OK()
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
{
  "request" : {
	"method" : "POST",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[?(@.['property'] == 'value')]"
	} ]
  },
  "response" : {
	"status" : 200,
	"transformers" : [ "response-template", "foo-transformer" ]
  }
}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue("#121")
	def 'should generate stub with empty list as a value of a field'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method('POST')
					url("foo")
					body(
							values: []
					)
				}
				response {
					status OK()
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
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
						"status": 200,
						"transformers" : [ "response-template", "foo-transformer" ]
					}
				}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should generate stub properly resolving GString with regular expression'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
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
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
		{
		  "request" : {
			"url" : "/users/password",
			"method" : "POST",
			"bodyPatterns" : [ {
			  "matchesJsonPath" : "$[?(@.['callback_url'] =~ /((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?/)]"
			}, {
			  "matchesJsonPath" : "$[?(@.['email'] =~ /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6}/)]"
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
			},
			"transformers" : [ "response-template", "foo-transformer" ]
		  },
		  "priority" : 1
		}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	def 'should generate stub properly resolving GString with regular expression in url'() {
		given:
			org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {

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
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
		{
		  "request" : {
			"urlPattern" : "/partners/^[0-9]*$/agents/11/customers/09665703Z",
			"method" : "PUT",
			"bodyPatterns" : [ {
			  "matchesJsonPath" : "$[?(@.['first_name'] == 'Josef')]"
			} ],
			"headers" : {
			  "Content-Type" : {
				"equalTo" : "application/json"
			  }
			}
		  },
		  "response" : {
			"status" : 422,
			"transformers" : [ "response-template", "foo-transformer" ]
		  }
		}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('42')
	def 'should generate stub without optional parameters'() {
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
			{
			  "request" : {
				"url" : "/users/password",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "matchesJsonPath" : "$[?(@.['callback_url'] =~ /((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?/)]"
				}, {
				  "matchesJsonPath" : "$[?(@.['email'] =~ /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6})?/)]"
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
				},
				"transformers" : [ "response-template", "foo-transformer" ]
			  },
			  "priority" : 1
			}
			'''), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		where:
		contractDsl << [
				org.springframework.cloud.contract.spec.Contract.make {
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
								code: $(consumer("123123"), producer(optional("123123"))),
								message: "User not found by email = [${value(producer(regex(email())), consumer('not.existing@user.com'))}]"
						)
					}
				},
				org.springframework.cloud.contract.spec.Contract.make {
					priority 1
					request {
						method 'POST'
						url '/users/password'
						headers {
							header 'Content-Type': 'application/json'
						}
						body(
								""" {
								"email" : "${value(consumer(optional(regex(email()))), producer('abc@abc.com'))}",
								"callback_url" : "${value(consumer(regex(hostname())), producer('http://partners.com'))}"
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
								"message" : "User not found by email = [${value(producer(regex(email())), consumer('not.existing@user.com'))}]"
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

	String toWireMockClientJsonStub(Contract groovyDsl) {
		return new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
	}

	@Issue('180')
	def 'should generate stub with multipart parameters'() {
		given:
		// tag::multipartdsl[]
			org.springframework.cloud.contract.spec.Contract contractDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method "PUT"
					url "/multipart"
					headers {
						contentType('multipart/form-data;boundary=AaB03x')
					}
					multipart(
							// key (parameter name), value (parameter value) pair
							formParameter: $(c(regex('".+"')), p('"formParameterValue"')),
							someBooleanParameter: $(c(regex(anyBoolean())), p('true')),
							// a parameter name (e.g. file)
							file: named(
									// name of the file
									name: $(c(regex(nonEmpty())), p('filename.csv')),
									// content of the file
									content: $(c(regex(nonEmpty())), p('file content')))
					)
				}
				response {
					status OK()
				}
			}
		// end::multipartdsl[]
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, contractDsl), contractDsl).toWireMockClientStub()
		then:
			println wireMockStub
			AssertionUtil.assertThatJsonsAreEqual((
					// tag::multipartwiremock[]
					'''
		{
		  "request" : {
			"url" : "/multipart",
			"method" : "PUT",
			"headers" : {
			  "Content-Type" : {
				"matches" : "multipart/form-data;boundary=AaB03x.*"
			  }
			},
			"bodyPatterns" : [ {
				"matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"formParameter\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Transfer-Encoding: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n\\".+\\"\\r\\n--\\\\1.*"
    		}, {
      			"matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"someBooleanParameter\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Transfer-Encoding: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n(true|false)\\r\\n--\\\\1.*"
    		}, {			
			  "matches" : ".*--(.*)\\r\\nContent-Disposition: form-data; name=\\"file\\"; filename=\\"[\\\\S\\\\s]+\\"\\r\\n(Content-Type: .*\\r\\n)?(Content-Transfer-Encoding: .*\\r\\n)?(Content-Length: \\\\d+\\r\\n)?\\r\\n[\\\\S\\\\s]+\\r\\n--\\\\1.*"
			} ]
		  },
		  "response" : {
			"status" : 200,
			"transformers" : [ "response-template", "foo-transformer" ]
		  }
		}
			'''
					// end::multipartwiremock[]
			), wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('#219')
	def "should generate request with an optional queryParameter for client side"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'GET'
				urlPath ('/some/api') {
					queryParameters {
						parameter 'size': value(
								consumer(regex('[0-9]+')),
								producer(1)
						)
						parameter 'page': value(
								consumer(regex('[0-9]+')),
								producer(0)
						)
						parameter sort: value(
								consumer(optional(regex('^[a-z]+$'))),
								producer('id')
						)
					}
				}
			}
			response {
				status OK()
				body(
						content: [[
										  id      : '00000000-0000-0000-0000-000000000000',
										  type  : 'Extraordinary',
										  state : 'ACTIVE',
								  ]],
						totalPages: 1,
						totalElements: 1,
						last: true,
						sort: [[
									   direction: 'ASC',
									   property: 'id',
									   ignoreCase: false,
									   nullHandling: 'NATIVE',
									   ascending: true
							   ]],
						first: true,
						numberOfElements: 1,
						size: 1,
						number: 0
				)
			}
		}
		when:
		def json = toWireMockClientJsonStub(groovyDsl)
		then:
		AssertionUtil.assertThatJsonsAreEqual(('''
				{
				  "request" : {
					"urlPath" : "/some/api",
					"method" : "GET",
					"queryParameters" : {
					  "size" : {
						"matches" : "[0-9]+"
					  },
					  "page" : {
						"matches" : "[0-9]+"
					  },
					  "sort" : {
						"matches" : "(^[a-z]+$)?"
					  }
					}
				  },
				  "response" : {
					"status" : 200,
					"body" : "{\\"number\\":0,\\"last\\":true,\\"numberOfElements\\":1,\\"size\\":1,\\"totalPages\\":1,\\"sort\\":[{\\"nullHandling\\":\\"NATIVE\\",\\"ignoreCase\\":false,\\"property\\":\\"id\\",\\"ascending\\":true,\\"direction\\":\\"ASC\\"}],\\"content\\":[{\\"id\\":\\"00000000-0000-0000-0000-000000000000\\",\\"state\\":\\"ACTIVE\\",\\"type\\":\\"Extraordinary\\"}],\\"first\\":true,\\"totalElements\\":1}",
					"transformers" : [ "response-template", "foo-transformer" ]
				  }
				}
				'''), json)
		and:
		stubMappingIsValidWireMockStub(json)
	}

	@Issue('#30')
	def "should not create a stub for a skipped contract"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				ignored()
				method 'GET'
				urlPath ('/some/api') {
					queryParameters {
						parameter 'size': value(
								consumer(regex('[0-9]+')),
								producer(1)
						)
					}
				}
			}
			response {
				status OK()
				body('')
			}
		}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
			json == ''
	}

	@Issue('#30')
	def "should not create a stub for a contract matching ignored pattern"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'GET'
				urlPath ('/some/api') {
					queryParameters {
						parameter 'size': value(
								consumer(regex('[0-9]+')),
								producer(1)
						)
					}
				}
			}
			response {
				status OK()
				body('')
			}
		}
		when:
			def json = new WireMockStubStrategy("Test", new ContractMetadata(null, true, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			json == ''
	}

	@Issue('#237')
	def "should work with legacy mappings"() {
		given:
			def oldJsonMapping = '''
					{
					  "request" : {
						"urlPath" : "/api/v1/xxxx",
						"method" : "POST",
						"headers" : {
						  "Authorization" : {
							"equalTo" : "secret2"
						  }
						},
						"cookies" : {
						  "foo" : {
							"equalTo" : "bar"
						  }
						},
						"queryParameters" : {
						  "foo" : {
							"equalTo" : "bar2"
						  }
						},
						"bodyPatterns" : [ {
						  "matchesJsonPath" : "$[?(@.['baz'] == 5)]"
						}, {
						  "matchesJsonPath" : "$[?(@.['foo'] == 'bar')]"
						} ]
					  },
					  "response" : {
						"status" : 200,
						"body" : "{\\"authorization\\":\\"{{{request.headers.Authorization.[0]}}}\\",\\"path\\":\\"{{{request.path}}}\\",\\"responseBaz\\":{{{jsonpath this '$.baz'}}} ,\\"param\\":\\"{{{request.query.foo.[0]}}}\\",\\"pathIndex\\":\\"{{{request.path.[1]}}}\\",\\"responseBaz2\\":\\"Bla bla {{{jsonpath this '$.foo'}}} bla bla\\",\\"responseFoo\\":\\"{{{jsonpath this '$.foo'}}}\\",\\"authorization2\\":\\"{{{request.headers.Authorization.[1]}}}\\",\\"fullBody\\":\\"{{{escapejsonbody}}}\\",\\"url\\":\\"{{{request.url}}}\\",\\"paramIndex\\":\\"{{{request.query.foo.[1]}}}\\"}",
						"headers" : {
						  "Authorization" : "{{{request.headers.Authorization.[0]}}};foo"
						},
						"transformers" : [ "response-template", "foo-transformer" ]
					  }
					}
					'''
		and:
			int port = SocketUtils.findAvailableTcpPort()
			WireMockServer server = new WireMockServer(config().port(port))
			server.start()
		and:
			server.addStubMapping(WireMockStubMapping.buildFrom(oldJsonMapping))
		when:
			ResponseEntity<String> entity = call(port)
		then:
			entity.headers.find { it.key == "Authorization" && it.value.contains("secret;foo") }
			AssertionUtil.assertThatJsonsAreEqual(('''
				{
				  "url" : "/api/v1/xxxx?foo=bar&foo=bar2",
				  "param" : "bar",
				  "paramIndex" : "bar2",
				  "authorization" : "secret",
				  "authorization2" : "secret2",
				  "fullBody" : "{\\"foo\\":\\"bar\\",\\"baz\\":5}",
				  "responseFoo" : "bar",
				  "responseBaz" : 5,
				  "responseBaz2" : "Bla bla bar bla bla"
				}
				'''), entity.body)
		cleanup:
			server?.shutdown()
	}

	@Issue("#664")
	def "should work with byte arrays"() {
		given:
			File request = new File(WireMockGroovyDslSpec.getResource("/body_builder/request.pdf").file)
			File response = new File(WireMockGroovyDslSpec.getResource("/body_builder/response.pdf").file)
		and:
			File file = new File(WireMockGroovyDslSpec.getResource("/body_builder/worksWithPdf.groovy").file)
			Contract contract = ContractVerifierDslConverter.convertAsCollection(
					file.parentFile, file
			).first()
		and:
			def json = toWireMockClientJsonStub(contract)
		and:
			int port = SocketUtils.findAvailableTcpPort()
			WireMockServer server = new WireMockServer(config().port(port))
			server.start()
		and:
			server.addStubMapping(WireMockStubMapping.buildFrom(json))
		when:
			ResponseEntity<byte[]> entity = callBytes(port, request)
		then:
			entity.statusCodeValue == 200
			entity.body == response.bytes
		cleanup:
			server?.shutdown()
	}

	@Issue('#540')
	def "should generate a stub with standard WireMock request template"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'POST'
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
				cookies {
					cookie("foo", "bar")
				}
				body(foo: "bar", baz: 5)
			}
			response {
				status OK()
				headers {
					header(authorization(), "${fromRequest().header(authorization())};foo")
				}
				body(
						url: fromRequest().url(),
						path: fromRequest().path(),
						pathIndex: fromRequest().path(1),
						param: fromRequest().query("foo"),
						paramIndex: fromRequest().query("foo", 1),
						authorization: fromRequest().header("Authorization"),
						authorization2: fromRequest().header("Authorization", 1),
						fullBody: fromRequest().body(),
						responseFoo: fromRequest().body('$.foo'),
						responseBaz: fromRequest().body('$.baz'),
						responseBaz2: "Bla bla ${fromRequest().body('$.foo')} bla bla",
						rawUrl: fromRequest().rawUrl(),
						rawPath: fromRequest().rawPath(),
						rawPathIndex: fromRequest().rawPath(1),
						rawParam: fromRequest().rawQuery("foo"),
						rawParamIndex: fromRequest().rawQuery("foo", 1),
						rawAuthorization: fromRequest().rawHeader("Authorization"),
						rawAuthorization2: fromRequest().rawHeader("Authorization", 1),
						rawResponseFoo: fromRequest().rawBody('$.foo'),
						rawResponseBaz: fromRequest().rawBody('$.baz'),
						rawResponseBaz2: "Bla bla ${fromRequest().rawBody('$.foo')} bla bla"
				)
			}
		}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
			AssertionUtil.assertThatJsonsAreEqual(('''
					{
					  "request" : {
						"urlPath" : "/api/v1/xxxx",
						"method" : "POST",
						"headers" : {
						  "Authorization" : {
							"equalTo" : "secret2"
						  }
						},
						"cookies" : {
						  "foo" : {
							"equalTo" : "bar"
						  }
						},
						"queryParameters" : {
						  "foo" : {
							"equalTo" : "bar2"
						  }
						},
						"bodyPatterns" : [ {
						  "matchesJsonPath" : "$[?(@.['baz'] == 5)]"
						}, {
						  "matchesJsonPath" : "$[?(@.['foo'] == 'bar')]"
						} ]
					  },
					  "response" : {
						"status" : 200,
						"body" : "{\\"rawAuthorization2\\":\\"{{request.headers.Authorization.[1]}}\\",\\"responseBaz\\":{{{jsonPath request.body '$.baz'}}} ,\\"pathIndex\\":\\"{{{request.path.[1]}}}\\",\\"rawAuthorization\\":\\"{{request.headers.Authorization.[0]}}\\",\\"authorization2\\":\\"{{{request.headers.Authorization.[1]}}}\\",\\"rawParam\\":\\"{{request.query.foo.[0]}}\\",\\"url\\":\\"{{{request.url}}}\\",\\"paramIndex\\":\\"{{{request.query.foo.[1]}}}\\",\\"authorization\\":\\"{{{request.headers.Authorization.[0]}}}\\",\\"path\\":\\"{{{request.path}}}\\",\\"rawUrl\\":\\"{{request.url}}\\",\\"rawPath\\":\\"{{request.path}}\\",\\"rawResponseBaz2\\":\\"Bla bla {{jsonPath request.body '$.foo'}} bla bla\\",\\"param\\":\\"{{{request.query.foo.[0]}}}\\",\\"rawResponseBaz\\":{{jsonPath request.body '$.baz'}} ,\\"responseBaz2\\":\\"Bla bla {{{jsonPath request.body '$.foo'}}} bla bla\\",\\"rawResponseFoo\\":\\"{{jsonPath request.body '$.foo'}}\\",\\"responseFoo\\":\\"{{{jsonPath request.body '$.foo'}}}\\",\\"rawPathIndex\\":\\"{{request.path.[1]}}\\",\\"fullBody\\":\\"{{{escapejsonbody}}}\\",\\"rawParamIndex\\":\\"{{request.query.foo.[1]}}\\"}",
						"headers" : {
						  "Authorization" : "{{{request.headers.Authorization.[0]}}};foo"
						},
						"transformers" : [ "response-template", "foo-transformer" ]
					  }
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
		and:
			int port = SocketUtils.findAvailableTcpPort()
			WireMockServer server = new WireMockServer(config().port(port))
			server.start()
			server.addStubMapping(WireMockStubMapping.buildFrom(json))
		then:
			ResponseEntity<String> entity = call(port)
			entity.headers.find { it.key == "Authorization" && it.value.contains("secret;foo") }
		and:
			AssertionUtil.assertThatJsonsAreEqual(('''
				{  
				   "rawAuthorization2":"secret2",
				   "responseBaz":5,
				   "pathIndex":"v1",
				   "rawAuthorization":"secret",
				   "authorization2":"secret2",
				   "rawParam":"bar",
				   "url":"/api/v1/xxxx?foo=bar&foo=bar2",
				   "paramIndex":"bar2",
				   "authorization":"secret",
				   "path":"/api/v1/xxxx",
				   "rawUrl":"/api/v1/xxxx?foo&#x3D;bar&amp;foo&#x3D;bar2",
				   "rawPath":"/api/v1/xxxx",
				   "rawResponseBaz2":"Bla bla bar bla bla",
				   "param":"bar",
				   "rawResponseBaz":5,
				   "responseBaz2":"Bla bla bar bla bla",
				   "rawResponseFoo":"bar",
				   "responseFoo":"bar",
				   "rawPathIndex":"v1",
				   "fullBody":"{\\"foo\\":\\"bar\\",\\"baz\\":5}",
				   "rawParamIndex":"bar2"
				}
				'''), entity.body)
		cleanup:
			server?.shutdown()
	}

	@Issue('#578')
	def "should generate a stub for a request with form parameters"() {
		given:
			Contract groovyDsl = Contract.make {
				request {
					method POST()
					urlPath('/oauth/token')
					headers {
						header(authorization(), anyNonBlankString())
						header(contentType(), applicationFormUrlencoded())
						header(accept(), applicationJson())
					}
					body([
							username  : 'user',
							password  : 'password',
							grant_type: 'password'
					])
				}
				response {
					status 200
					headers {
						header(contentType(), applicationJson())
					}
					body([
							refresh_token: 'RANDOM_REFRESH_TOKEN',
							access_token : 'RANDOM_ACCESS_TOKEN',
							token_type   : 'bearer',
							expires_in   : 3600,
							scope        : ['task'],
							user         : [
									id      : 1,
									username: 'user',
									name    : 'User'
							]
					])
				}
			}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
			AssertionUtil.assertThatJsonsAreEqual(('''
				{
					"request" : {
					"urlPath" : "/oauth/token",
					"method" : "POST",
					"headers" : {
						"Authorization" : {
						"matches" : "^\\\\s*\\\\S[\\\\S\\\\s]*"
					},
					  "Content-Type" : {
						"equalTo" : "application/x-www-form-urlencoded"
					  },
					  "Accept" : {
						"equalTo" : "application/json"
					  }
					},
					"bodyPatterns" : [ {
					  "equalTo" : "username=user&password=password&grant_type=password"
					} ]
				},
				"response" : {
					"status" : 200,
					"body" : "{\\"access_token\\":\\"RANDOM_ACCESS_TOKEN\\",\\"refresh_token\\":\\"RANDOM_REFRESH_TOKEN\\",\\"scope\\":[\\"task\\"],\\"token_type\\":\\"bearer\\",\\"expires_in\\":3600,\\"user\\":{\\"name\\":\\"User\\",\\"id\\":1,\\"username\\":\\"user\\"}}",
					"headers" : {
					  "Content-Type" : "application/json"
					},
					"transformers" : [ "response-template", "foo-transformer" ]
			  		}
			  	}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
	}

	@Issue('#266')
	def "should work with array of arrays"() {
		given:
		org.springframework.cloud.contract.spec.Contract groovyDsl = Contract.make {
			request {
				method 'POST'
				urlPath '/api/categories'
				body([["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]])
				headers {
					header('Content-Type': 'application/json;charset=UTF-8')
				}
			}
			response {
				status 200
				body([["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]])
				headers {
					header('Content-Type': 'application/json;charset=UTF-8')
				}
			}
		}
		when:
			def json = toWireMockClientJsonStub(groovyDsl)
		then:
			AssertionUtil.assertThatJsonsAreEqual(('''
					{
					  "request" : {
						"urlPath" : "/api/categories",
						"method" : "POST",
						"headers" : {
						  "Content-Type" : {
							"equalTo" : "application/json;charset=UTF-8"
						  }
						},
						"bodyPatterns" : [ {
						  "matchesJsonPath" : "$[*][?(@ == 'Spring')]"
						}, {
						  "matchesJsonPath" : "$[*][?(@ == 'Boot')]"
						}, {
						  "matchesJsonPath" : "$[*][?(@ == 'Programming')]"
						}, {
						  "matchesJsonPath" : "$[*][?(@ == 'Java')]"
						} ]
					  },
					  "response" : {
						"status" : 200,
						"body" : "[\\"[\\\\\\"Programming\\\\\\",\\\\\\"Java\\\\\\"]\\",\\"[\\\\\\"Programming\\\\\\",\\\\\\"Java\\\\\\",\\\\\\"Spring\\\\\\",\\\\\\"Boot\\\\\\"]\\"]",
						"headers" : {
						  "Content-Type" : "application/json;charset=UTF-8"
						},
						"transformers" : [ "response-template", "foo-transformer" ]
					  }
					}
					'''), json)
		and:
			stubMappingIsValidWireMockStub(json)
		and:
			int port = SocketUtils.findAvailableTcpPort()
			WireMockServer server = new WireMockServer(config().port(port))
			server.start()
			server.addStubMapping(WireMockStubMapping.buildFrom(json))
		then:
			String entity = callApiCategories(port)
		and:
			AssertionUtil.assertThatJsonsAreEqual(('''
				["[\\"Programming\\",\\"Java\\"]","[\\"Programming\\",\\"Java\\",\\"Spring\\",\\"Boot\\"]"]
				'''), entity)
		cleanup:
			server?.shutdown()
	}

	@Issue('#269')
	def "should create a stub for dot separated keys"() {
		given:
			Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			request {
				method 'PUT'
				url '/fraudcheck'
				body([ // (4)
					   "client.id": $(regex('[0-9]{10}')),
					   loanAmount: 99999
				])
				headers {
					contentType('application/vnd.fraud.v1+json')
				}
			}
			response {
				status OK()
			}
		}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			AssertionUtil.assertThatJsonsAreEqual('''
				{
				  "request" : {
					  "url" : "/fraudcheck",
					  "method" : "PUT",
					  "headers" : {
						"Content-Type" : {
						  "matches" : "application/vnd\\\\.fraud\\\\.v1\\\\+json.*"
						}
					  },
					  "bodyPatterns" : [ {
						"matchesJsonPath" : "$[?(@.['loanAmount'] == 99999)]"
					  }, {
						"matchesJsonPath" : "$[?(@.['client.id'] =~ /[0-9]{10}/)]"
					  } ]
					}
				  },
				  "response" : {
					"status" : 200
				  }
				}
				''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('#427')
	def "should not fail to generate a stub when arrays are there in the request"() {
		given:
			Contract groovyDsl = Contract.make {
				request {
					method "POST"
					urlPath('/batch/persons')
					body("""
					[{
						"fruitsILike": [
						  "apple"
						]
					},
					{              
						"fruitsILike": [
						]
					},
					{
						"fruitsILike": [
							"orange"
						]
					}]
            """)
				}
				response {
					status 201
					headers {
						contentType(applicationJsonUtf8())
					}
					body("""{  
							"id": "foo"  
						}"  
					""")
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			AssertionUtil.assertThatJsonsAreEqual('''
				{
				  "request" : {
					"urlPath" : "/batch/persons",
					"method" : "POST",
					"bodyPatterns" : [ {
					  "matchesJsonPath" : "$[*].['fruitsILike'][?(@ == 'orange')]"
					}, {
					  "matchesJsonPath" : "$[*].['fruitsILike'][?(@ == 'apple')]"
					} ]
				  },
				  "response" : {
					"status" : 201,
					"body" : "{\\"id\\":\\"foo\\"}",
					"headers" : {
					  "Content-Type" : "application/json;charset=UTF-8"
					}
				  }
				}
				''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('#385')
	def "should not escape unicode characters"() {
		given:
			Contract groovyDsl = Contract.make {
			request {
				method 'PUT'
				url '/fraudcheck'
				body([ // (4)
					   "client.id": $(regex('[0-9]{10}')),
					   loanAmount: 99999
				])
				headers {
					contentType('application/vnd.fraud.v1+json')
				}
			}
			response {
				status OK()
				body("""
						{
							"code": 91015,
							"description": "订单已失效",
							"lastUpdateTime": "0",
							"payload": null
						}
					""")
			}
		}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			AssertionUtil.assertThatJsonsAreEqual('''
				{
				  "request" : {
					"url" : "/fraudcheck",
					"method" : "PUT",
					"headers" : {
					  "Content-Type" : {
						"matches" : "application/vnd\\\\.fraud\\\\.v1\\\\+json.*"
					  }
					},
					"bodyPatterns" : [ {
					  "matchesJsonPath" : "$[?(@.['client.id'] =~ /[0-9]{10}/)]"
					}, {
					  "matchesJsonPath" : "$[?(@.['loanAmount'] == 99999)]"
					} ]
				  },
				  "response" : {
					"status" : 200,
					"body" : "{\\"code\\":91015,\\"payload\\":null,\\"description\\":\\"订单已失效\\",\\"lastUpdateTime\\":\\"0\\"}",
					"transformers" : [ "response-template", "foo-transformer" ]
				  }
				}
				''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)

	}

	@Issue('#493')
	def "should not escape &"() {
		given:
			Contract groovyDsl = Contract.make {
				request {
					method POST()
					urlPath('/oauth/token')
					headers {
						header(authorization(), anyNonBlankString())
						header(contentType(), 'application/x-www-form-urlencoded; charset=UTF-8')
						header(accept(), anyNonBlankString())
					}
					body('username=user&password=password&grant_type=password')
				}
				response {
					status 200
					headers {
						header(contentType(), applicationJsonUtf8())
					}
					body([
							refresh_token: 'RANDOM_REFRESH_TOKEN',
							access_token : 'RANDOM_ACCESS_TOKEN',
							token_type   : 'bearer',
							expires_in   : 3600,
							scope        : ['task'],
							user         : [
									id      : 1,
									username: 'user',
									name    : 'User'
							]
					])
				}
			}
		when:
			String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
			!wireMockStub.contains("&amp;")
		and:
			AssertionUtil.assertThatJsonsAreEqual('''
				{
				  "request" : {
					"urlPath" : "/oauth/token",
					"method" : "POST",
					"headers" : {
					  "Authorization" : {
						"matches" : "^\\\\s*\\\\S[\\\\S\\\\s]*"
					  },
					  "Content-Type" : {
						"equalTo" : "application/x-www-form-urlencoded; charset=UTF-8"
					  },
					  "Accept" : {
						"matches" : "^\\\\s*\\\\S[\\\\S\\\\s]*"
					  }
					},
					"bodyPatterns" : [ {
					  "equalTo" : "username=user&password=password&grant_type=password"
					} ]
				  },
				  "response" : {
					"status" : 200,
					"body" : "{\\"access_token\\":\\"RANDOM_ACCESS_TOKEN\\",\\"refresh_token\\":\\"RANDOM_REFRESH_TOKEN\\",\\"scope\\":[\\"task\\"],\\"token_type\\":\\"bearer\\",\\"expires_in\\":3600,\\"user\\":{\\"name\\":\\"User\\",\\"id\\":1,\\"username\\":\\"user\\"}}",
					"headers" : {
					  "Content-Type" : "application/json;charset=UTF-8"
					},
					"transformers" : [ "response-template", "foo-transformer" ]
				  }
				}
				''', wireMockStub)
		and:
			stubMappingIsValidWireMockStub(wireMockStub)

	}

	@Issue('#667')
	def "should not double escape backslashes"() {
		given:
		Contract groovyDsl = org.springframework.cloud.contract.spec.Contract.make {
			priority 1
			request {
				method 'GET'
				urlPath(value(consumer(regex(/\/data\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)),
						producer('/data/444d57b2-e309-4576-83cb-5530ee03106a')))
			}
			response {
				status 200
				headers {
					header("Content-Type", "application/json;charset=UTF-8")
				}
				body([
						jsonString: '{\\"attribute\\": \\"value\\"}'
				])
			}
		}
		when:
		String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
				{
					"request" : {
						"urlPathPattern" : "/data/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
						"method" : "GET"
					},
					"response" : {
						"status" : 200,
						"body" : "{\\"jsonString\\":\\"{\\\\\\"attribute\\\\\\": \\\\\\"value\\\\\\"}\\"}",
						"headers" : {
							"Content-Type" : "application/json;charset=UTF-8"
					},
					"transformers" : [ "response-template", "foo-transformer" ]
					},
					"priority" : 1
				}
				''', wireMockStub)
		and:
		stubMappingIsValidWireMockStub(wireMockStub)
	}

	@Issue('#708')
	def "should work with boolean body"() {
		given:
		Contract groovyDsl = Contract.make {
			request {
				method('DELETE')
				urlPath("/item/1")
			}
			response {
				status(200)
				headers {
					contentType(applicationJsonUtf8())
				}
				body("true")
			}
		}
		when:
		String wireMockStub = new WireMockStubStrategy("Test", new ContractMetadata(null, false, 0, null, groovyDsl), groovyDsl).toWireMockClientStub()
		then:
		AssertionUtil.assertThatJsonsAreEqual('''
				{
				  "request" : {
					"urlPath" : "/item/1",
					"method" : "DELETE"
				  },
				  "response" : {
					"status" : 200,
					"body" : "true",
					"headers" : {
					  "Content-Type" : "application/json;charset=UTF-8"
					},
					"transformers" : [ "response-template", "foo-transformer" ]
				  }
				}
				''', wireMockStub)
		and:
		stubMappingIsValidWireMockStub(wireMockStub)
	}

	WireMockConfiguration config() {
		return new WireMockConfiguration().extensions(responseTemplateTransformer())
	}

	private ResponseTemplateTransformer responseTemplateTransformer() {
		return new ResponseTemplateTransformer(false,
				[(HandlebarsJsonPathHelper.NAME): new HandlebarsJsonPathHelper(),
				(HandlebarsEscapeHelper.NAME): new HandlebarsEscapeHelper()])
	}

	ResponseEntity<String> call(int port) {
		return new TestRestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/xxxx?foo=bar&foo=bar2"))
						.header("Authorization", "secret")
						.header("Authorization", "secret2")
						.header("Cookie", "foo=bar")
						.body("{\"foo\":\"bar\",\"baz\":5}"), String.class)
	}

	ResponseEntity<byte[]> callBytes(int port, File request) {
		return new TestRestTemplate().exchange(
				RequestEntity.put(URI.create("http://localhost:" + port + "/1"))
						.header("Content-Type", "application/octet-stream")
						.body(request.bytes), byte[].class)
	}

	String callApiCategories(int port) {
		return new TestRestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + port + "/api/categories"))
						.header("Content-Type", "application/json;charset=UTF-8")
						.body(JsonOutput.toJson([["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]])), String.class).body
	}
}