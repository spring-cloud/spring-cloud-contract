package io.codearte.accurest.wiremock

import io.coderate.accurest.dsl.GroovyDsl
import spock.lang.Specification

class WiremockToDslConverterSpec extends Specification {

	def 'should produce a Groovy DSL from Wiremock stub'() {
		given:
			String wiremockStub = '''\
{
    "request": {
        "method": "GET",
        "url": "/path",
        "headers" : {
            "Accept": {
                "matches": "text/.*"
            },
            "X-Custom-Header": {
                "contains": "2134"
            }
        }
    },
    "response": {
        "status": 200,
        "body": "{ \\"id\\": { \\"value\\": \\"132\\" }, \\"surname\\": \\"Kowalsky\\", \\"name\\": \\"Jan\\", \\"created\\": \\"2014-02-02 12:23:43\\" }",
        "headers": {
            "Content-Type": "text/plain",
        }
    }
}
'''
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url '/path'
					headers {
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
				response {
					status 200
					body(
							id: [value: '132'],
							surname: 'Kowalsky',
							name: 'Jan',
							created: '2014-02-02 12:23:43'
					)
					headers {
						header 'Content-Type': 'text/plain'

					}
				}
			}
		when:
			String groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
		then:
			new GroovyShell(this.class.classLoader).evaluate(
					""" io.coderate.accurest.dsl.GroovyDsl.make {
                $groovyDsl
            }""") == expectedGroovyDsl
	}


	def 'should convert Wiremock stub with body containing simple JSON'() {
		given:
			String wiremockStub = '''\
{
    "request": {
        "method": "DELETE",
        "urlPattern": "/credit-card-verification-data/[0-9]+",
        "headers": {
            "Content-Type": {
                "equalTo": "application/vnd.mymoid-adapter.v2+json; charset=UTF-8"
            }
        }
    },
    "response": {
        "status": 200,
        "body": "{\\"status\\": \\"OK\\"}",
        "headers": {
            "Content-Type": "application/json"
        }
    }
}
'''
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'DELETE'
					url $(client(~/\/credit-card-verification-data\/[0-9]+/), server(''))
					headers {
						header('Content-Type': 'application/vnd.mymoid-adapter.v2+json; charset=UTF-8')
					}
				}
				response {
					status 200
					body("""{
    "status": "OK"
}""")
					headers {
						header 'Content-Type': 'application/json'

					}
				}
			}
		when:
			String groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
		then:
			new GroovyShell(this.class.classLoader).evaluate(
					""" io.coderate.accurest.dsl.GroovyDsl.make {
                $groovyDsl
            }""") == expectedGroovyDsl
	}

	def 'should convert Wiremock stub with body containing integer'() {
		given:
			String wiremockStub = '''\
{
  "request": {
    "method": "POST",
    "url": "/charge/count",
    "headers": {
      "Content-Type": {
        "equalTo": "application/vnd.creditcard-reporter.v1+json"
      }
    }
  },
  "response": {
    "status": 200,
    "body": 200,
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
'''
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body(200)
					headers {
						header 'Content-Type': 'application/json'

					}
				}
			}
		when:
			String groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
		then:
			new GroovyShell(this.class.classLoader).evaluate(
					""" io.coderate.accurest.dsl.GroovyDsl.make {
                $groovyDsl
            }""") == expectedGroovyDsl
	}

	def 'should convert Wiremock stub with body as a list'() {
		given:
			String wiremockStub = '''\
{
  "request": {
    "method": "POST",
    "url": "/charge/count",
    "headers": {
      "Content-Type": {
        "equalTo": "application/vnd.creditcard-reporter.v1+json"
      }
    }
  },
  "response": {
    "status": 200,
    "body": "[ {\\"a\\":1, \\"c\\":\\"3\\"}, \\"b\\", \\"a\\" ]",
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
'''
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body([
							[a: 1, c: '3'],
							'b',
							'a'
					])
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
		then:
			new GroovyShell(this.class.classLoader).evaluate(
					""" io.coderate.accurest.dsl.GroovyDsl.make {
                $groovyDsl
            }""") == expectedGroovyDsl
	}


	def 'should convert Wiremock stub with body containing a nested list'() {
		given:
			String wiremockStub = '''\
{
  "request": {
    "method": "POST",
    "url": "/charge/search?pageNumber=0&size=2147483647",
    "headers": {
      "Content-Type": {
        "equalTo": "application/vnd.creditcard-reporter.v1+json"
      }
    }
  },
  "response": {
    "status": 200,
    "body":"[{\\"amount\\":1.01,\\"name\\":\\"Name\\",\\"info\\":{\\"title\\":\\"title1\\",\\"payload\\":null},\\"booleanvalue\\":true,\\"user\\":null},{\\"amount\\":2.01,\\"name\\":\\"Name2\\",\\"info\\":{\\"title\\":\\"title2\\",\\"payload\\":null},\\"booleanvalue\\":true,\\"user\\":null}]"
    }
}
'''
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/search?pageNumber=0&size=2147483647'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body("""[
    {
        "amount": 1.01,
        "name": "Name",
        "info": {
            "title": "title1",
            "payload": null
        },
        "booleanvalue": true,
        "user": null
    },
    {
        "amount": 2.01,
        "name": "Name2",
        "info": {
            "title": "title2",
            "payload": null
        },
        "booleanvalue": true,
        "user": null
    }
]""")
				}
			}
		when:
			String groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
		then:
			new GroovyShell(this.class.classLoader).evaluate(
					""" io.coderate.accurest.dsl.GroovyDsl.make {
                $groovyDsl
            }""") == expectedGroovyDsl
	}

}
