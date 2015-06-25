package io.codearte.accurest.dsl

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Issue

class WiremockGroovyDslSpec extends WiremockSpec {

	def 'should convert groovy dsl stub to wiremock stub for the client side'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}"
    },
    "response": {
        "status": 200,
        "body": "{\\"id\\":\\"123\\",\\"surname\\":\\"Kowalsky\\",\\"name\\":\\"Jan\\",\\"created\\":\\"2014-02-02 12:23:43\\"}",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
	}

	@Issue("#79")
	def 'should convert groovy dsl stub to wiremock stub for the client side with a body containing a map'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "GET",
        "headers": {
            "Content-Type": {
                "equalTo": "application/vnd.pl.devoxx.aggregatr.v1+json"
            }
        },
        "url": "/ingredients"
    },
    "response": {
        "status": 200,
        "body": "{\\"ingredients\\":[{\\"type\\":\\"MALT\\",\\"quantity\\":100},{\\"type\\":\\"WATER\\",\\"quantity\\":200},{\\"type\\":\\"HOP\\",\\"quantity\\":300},{\\"type\\":\\"YIEST\\",\\"quantity\\":400}]}"
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
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
					body("""paymentType=INCOMING&transferType=BANK&amount=${value(client(regex('[0-9]{3}\\.[0-9]{2}')), server(500.00))}&bookingDate=${value(client(regex('[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])')), server('2015-05-18'))}""")
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
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
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
	}

	def 'should convert groovy dsl stub with Body as String to wiremock stub for the client side'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}"
    },
    "response": {
        "status": 200,
        "body": "{\\"created\\":\\"2014-02-02 12:23:43\\",\\"id\\":\\"123\\",\\"name\\":\\"Jan\\",\\"surname\\":\\"Kowalsky\\"}",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
	}

	def 'should convert groovy dsl stub with simple Body as String to wiremock stub for the client side'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}",
        "bodyPatterns": [
            {
                "equalToJson":"{\\"name\\":\\"Jan\\"}"
            }
        ]
    },
    "response": {
        "status": 200,
        "body": "{\\"name\\":\\"Jan\\"}",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
			{
				"request": {
					"method": "GET",
					"urlPattern": "/[0-9]{2}",
					"bodyPatterns": [
						{
							"equalToJson": "{\\"id\\":\\"123\\",\\"surname\\":\\"Kowalsky\\",\\"name\\":\\"Jan\\",\\"created\\":\\"2014-02-02 12:23:43\\"}"
						}
					]
				},
				"response": {
					"status": 200,
				}
			}
			''')
		and:
		stubMappingIsValidWiremockStub(wiremockStub)
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
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
			{
				"request": {
					"method": "GET",
					"url": "/users",
				    "headers": {
                        "Content-Type": {
                            "equalTo": "customtype/json"
                        }
                    },
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
			''')
		and:
			stubMappingIsValidWiremockStub(json)
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
					body """<name>${value(client('Jozo'), server('Denis'))}</name><jobId>${value(client("<test>"), server('1234567890'))}</jobId>"""
				}
				response {
					status 200
				}
			}
		when:
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
				''')
		and:
			stubMappingIsValidWiremockStub(json)
	}

	def 'should use equalToXml when content type is parsable xml'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					body """<user><name>${value(client('Jozo'), server('Denis'))}</name><jobId>${value(client("<test>"), server('1234567890'))}</jobId></user>"""
				}
				response {
					status 200
				}
			}
		when:
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
					''')
		and:
			stubMappingIsValidWiremockStub(json)
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
					body """<user><name>${value(client('Jozo'), server('Denis'))}</name><jobId>${value(client("<test>"), server('1234567890'))}</jobId></user>"""
				}
			}
		when:
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
						''')
		and:
			stubMappingIsValidWiremockStub(json)
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
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
				''')
		and:
			stubMappingIsValidWiremockStub(json)
	}

	def 'should use equalToXml'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url "/users"
					body equalToXml("""<name>${value(client('Jozo'), server('Denis'))}</name><jobId>${value(client("<test>"), server('1234567890'))}</jobId>""")
				}
				response {
					status 200
				}
			}
		when:
			String json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
					''')
		and:
			stubMappingIsValidWiremockStub(json)
	}

	def 'should convert groovy dsl stub with regexp Body as String to wiremock stub for the client side'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}",
        "bodyPatterns": [
			{"matches": "\\\\s*\\\\{\\\\s*\\\"personalId\\\"\\\\s*:\\\\s*\\\"?^[0-9]{11}$\\\"?\\\\s*\\\\}\\\\s*"}
        ]
    },
    "response": {
        "status": 200,
        "body": "{\\"name\\":\\"Jan\\"}",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
{
    "request": {
        "method": "PUT",
        "headers": {
            "Content-Type": {
                "equalTo": "application/vnd.fraud.v1+json"
            }
        },
        "url": "/fraudcheck",
        "bodyPatterns": [
			{"matches": "\\\\s*\\\\{\\\\s*\\"clientPesel\\"\\\\s*:\\\\s*\\"?[0-9]{10}\\"?\\\\s*,\\\\s*\\"loanAmount\\"\\\\s*:\\\\s*\\"?123.123\\"?\\\\s*\\\\}\\\\s*"}
        ]
    },
    "response": {
        "status": 200,
        "headers": {
            "Content-Type": "application/vnd.fraud.v1+json"
        },
        "body": "{\\"fraudCheckStatus\\":\\"OK\\",\\"rejectionReason\\":null}"
    }
}
''')
		and:
			stubMappingIsValidWiremockStub(wiremockStub)
	}


	def "should generate stub with GET"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method("GET")
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "method":"GET"
    }
    ''')
	}

	def "should generate request when two elements are provided "() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method("GET")
					url("/sth")
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "method":"GET",
        "url":"/sth"
    }
    ''')
	}

	def "should generate request with urlPattern for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					url $(
							client(~/^\/[0-9]{2}$/),
							server('/12')
					)
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "urlPattern":"^/[0-9]{2}$"
    }
    ''')
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
			def json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
			''')
		and:
			stubMappingIsValidWiremockStub(json)
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
			def json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
				{
					"request": {
						"method": "GET",
						"urlPath": "boxes"
					},
					"response": {
						"status": 200,
					}
				}
				''')
		and:
			stubMappingIsValidWiremockStub(json)
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
			def json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
				{
					"request": {
						"method": "GET",
						"urlPath": "boxes"
					},
					"response": {
						"status": 200,
					}
				}
				''')
		and:
			stubMappingIsValidWiremockStub(json)
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
			def json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
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
				''')
		and:
			stubMappingIsValidWiremockStub(json)
	}

	def "should generate stub with some headers section for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					headers {
						header('Content-Type': 'text/xml')
						header('Accept': $(
								client(regex('text/.*')),
								server('text/plain')
						))
						header('X-Custom-Header': $(
								client(regex('^.*2134.*$')),
								server('121345')
						))
					}
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "headers": {
            "Content-Type": {
                "equalTo": "text/xml"
            },
            "Accept": {
                "matches": "text/.*"
            },
            "X-Custom-Header": {
                "matches": "^.*2134.*$"
            }
        }
    }
    ''')
	}

	def 'should convert groovy dsl stub with rich tree Body as String to wiremock stub for the client side'() {
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
			String wiremockStub = new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
		then:
			new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
			{
				"request": {
							"method": "GET",
							"urlPattern": "/[0-9]{2}",
						    "bodyPatterns": [
							     {
								    "matches": "\\\\s*\\\\{\\\\s*\\"birthDate\\"\\\\s*:\\\\s*\\"?[0-9]{4}-[0-9]{2}-[0-9]{2}\\"?\\\\s*,\\\\s*\\"errors\\"\\\\s*:\\\\s*\\\\[\\\\s*\\\\{\\\\s*\\"propertyName\\"\\\\s*:\\\\s*\\"?[0-9]{2}\\"?\\\\s*,\\\\s*\\"providerValue\\"\\\\s*:\\\\s*\\"?Test\\"?\\\\s*\\\\}\\\\s*,\\\\s*\\\\{\\\\s*\\"propertyName\\"\\\\s*:\\\\s*\\"?[0-9]{2}\\"?\\\\s*,\\\\s*\\"providerValue\\"\\\\s*:\\\\s*\\"?Test\\"?\\\\s*\\\\}\\\\s*\\\\]\\\\s*,\\\\s*\\"firstName\\"\\\\s*:\\\\s*\\"?.*\\"?\\\\s*,\\\\s*\\"lastName\\"\\\\s*:\\\\s*\\"?.*\\"?\\\\s*,\\\\s*\\"personalId\\"\\\\s*:\\\\s*\\"?[0-9]{11}\\"?\\\\s*\\\\}\\\\s*"
							     }
						     ] },
				"response": {
							"status": 200,
							"body": "{\\"name\\":\\"Jan\\"}",
							"headers": {
										"Content-Type": "text/plain"
										}
							}
			}
			''')
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
			def json = toWiremockClientJsonStub(groovyDsl)
		then:
			parseJson(json) == parseJson('''
				{
					"request": {
								"method": "POST",
								"url": "/reissue-payment-order",
								"bodyPatterns": [
									{
									   "matches": "\\\\s*\\\\{\\\\s*\\"loanNumber\\"\\\\s*:\\\\s*\\"?999997001\\"?\\\\s*,\\\\s*\\"amount\\"\\\\s*:\\\\s*\\"?[0-9.]+\\"?\\\\s*,\\\\s*\\"currency\\"\\\\s*:\\\\s*\\"?DKK\\"?\\\\s*,\\\\s*\\"applicationName\\"\\\\s*:\\\\s*\\"?.*\\"?\\\\s*,\\\\s*\\"username\\"\\\\s*:\\\\s*\\"?.*\\"?\\\\s*,\\\\s*\\"cardId\\"\\\\s*:\\\\s*\\"?1\\"?\\\\s*\\\\}\\\\s*"
									}
								]
								},
					"response": {
								"status": 200,
								"body": "{\\"status\\":\\"OK\\"}",
								"headers": {
											"Content-Type": "application/json"
											}
								}
				}
				''')
	}

	String toJsonString(value) {
		new JsonBuilder(value).toPrettyString()
	}

	Object parseJson(json) {
		new JsonSlurper().parseText(json)
	}

	String toWiremockClientJsonStub(groovyDsl) {
		new WiremockStubStrategy(groovyDsl).toWiremockClientStub()
	}
}
