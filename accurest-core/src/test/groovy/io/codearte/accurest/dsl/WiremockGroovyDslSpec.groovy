package io.codearte.accurest.dsl
import groovy.json.JsonSlurper

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
				"matches":"{\\"personalId\\":\\"^[0-9]{11}$\\"}"
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
}
