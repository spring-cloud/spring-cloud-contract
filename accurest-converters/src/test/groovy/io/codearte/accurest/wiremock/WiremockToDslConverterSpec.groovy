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
        "urlPattern": "/[0-9]{2}",
        "headers" : {
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
    },
    "response": {
        "status": 200,
        "body": {
            "id": {
                "value": "132"
            },
            "surname": "Kowalsky",
            "name": "Jan",
            "created" : "2014-02-02 12:23:43"
        },
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
                                                                urlPattern '/[0-9]{2}'
                                                                headers {
                                                                    header('Accept').matches('text/.*')
                                                                    header('etag').doesNotMatch('abcd.*')
                                                                    header('X-Custom-Header').contains('2134')
                                                                }
                                                            }
                                                            response {
                                                                status 200
                                                                body (
                                                                        id : [value: '132'],
                                                                        surname : 'Kowalsky',
                                                                        name: 'Jan',
                                                                        created : '2014-02-02 12:23:43'
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
}
