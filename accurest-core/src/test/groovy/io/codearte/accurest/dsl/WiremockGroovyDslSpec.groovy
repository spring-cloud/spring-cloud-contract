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
                "equalTo":"{\\"name\\":\\"Jan\\"}"
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
        	{
				"matches":"\\\\{\\"personalId\\":\\"^[0-9]{11}$\\"\\\\}"
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
            {
                "matches": "\\\\{\\"clientPesel\\":\\"[0-9]{10}\\",\\"loanAmount\\":123.123\\\\}"
            }
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
