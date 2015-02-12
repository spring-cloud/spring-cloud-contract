package io.codearte.accurest.wiremock

import io.coderate.accurest.dsl.GroovyDsl
import spock.lang.Specification

class WiremockToDslConverterSpec extends Specification {

    def 'should produce a Groovy DSL from Wiremock stub'() {
        given:
            String wiremockStub = '''
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
'''
        and:
            GroovyDsl expectedGroovyDsl = GroovyDsl.make {
                request {
                    method 'GET'
                    urlPattern '/[0-9]{2}'
                }
                response {
                    status 200
                    body (
                            id : '123',
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
            GroovyDsl groovyDsl = WiremockToDslConverter.fromWiremockStub(wiremockStub)
        then:
            groovyDsl == expectedGroovyDsl
    }
}
