package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl
import io.coderate.accurest.dsl.WiremockRequestStubStrategy
import io.coderate.accurest.dsl.WiremockStubStrategy
import spock.lang.Ignore
import spock.lang.Specification

class WiremockGroovyDslSpec extends Specification {

	def 'should convert groovy dsl stub to wiremock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					urlPattern $(client('/[0-9]{2}'), server('/12'))
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
        "body": {
            "id": "123",
            "surname": "Kowalsky",
            "name": "Jan",
            "created" : "2014-02-02 12:23:43"
        },
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
	}

	def 'should convert groovy dsl stub with Body as String to wiremock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					urlPattern $(client('/[0-9]{2}'), server('/12'))
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
        "body": {
            "id": "123",
            "surname": "Kowalsky",
            "name": "Jan",
            "created" : "2014-02-02 12:23:43"
        },
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
	}

	def 'should convert groovy dsl stub with simple Body as String to wiremock stub for the client side'() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					method('GET')
					urlPattern $(client('/[0-9]{2}'), server('/12'))
					body("""\
                            {
                                "name": "Jan"
                            }
                        """
					)
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
        "body": {
            "name": "Jan"
        }
    },
    "response": {
        "status": 200,
        "body": {
            "name": "Jan"
        },
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
''')
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
					urlPattern $(
							client('/^[0-9]{2}$'),
							server('/12')
					)
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "urlPattern":"/^[0-9]{2}$"
    }
    ''')
	}

	def "should generate stub with urlPath for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					urlPath('/12')
				}
			}
		expect:
			new WiremockRequestStubStrategy(groovyDsl).buildClientRequestContent() == new JsonSlurper().parseText('''
    {
        "urlPath":"/12"
    }
    ''')
	}

	def "should generate stub with some headers section for client side"() {
		given:
			GroovyDsl groovyDsl = GroovyDsl.make {
				request {
					headers {
						header('Content-Type').equalTo('text/xml')
						header('Accept').matches $(
								client('text/.*'),
								server('text/plain')
						)
						header('etag').doesNotMatch $(
								client('abcd.*'),
								server('abcdef')
						)
						header('X-Custom-Header').contains $(
								client('2134'),
								server('121345')
						)
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
            "etag": {
                "doesNotMatch": "abcd.*"
            },
            "X-Custom-Header": {
                "contains": "2134"
            }
        }
    }
    ''')
	}
}
