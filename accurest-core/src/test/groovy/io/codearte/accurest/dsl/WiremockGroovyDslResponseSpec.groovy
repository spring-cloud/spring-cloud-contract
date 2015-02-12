package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl
import io.coderate.accurest.dsl.WiremockResponseStubStrategy
import spock.lang.Specification

class WiremockGroovyDslResponseSpec extends Specification {

    def 'should generate response without body for #side side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                }
            }
        expect:
            new WiremockResponseStubStrategy(dsl)."build${side}ResponseContent"() == new JsonSlurper().parseText(expectedStub)
        where:
            side << ['Client', 'Server']
            expectedStub << ['''
    {
        "status": 200
    }
    ''',

    '''
    {
        "status": 200
    }
    ''']
    }

    def 'should generate headers for response for client side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    headers {
                        header('Content-Type').matches $(client('text/xml'), server('text/*'))
                    }
                    status 200
                }
            }
        expect:
            new WiremockResponseStubStrategy(dsl).buildClientResponseContent() == new JsonSlurper().parseText('''
    {
            "headers": {
                "Content-Type": {
                    "matches": "text/xml"
                },
            },
            "status": 200
    }
    ''')
    }

    def 'should generate headers for response for server side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                    headers {
                        header('Content-Type').matches $(client('text/xml'), server('text/*'))
                    }
                }
            }
        expect:
            new WiremockResponseStubStrategy(dsl).buildServerResponseContent() == new JsonSlurper().parseText('''
    {
        "status": 200,
        "headers": {
                "Content-Type": {
                    "matches": "text/*"
                }
        }
    }
    ''')
    }

    def 'should generate an exact header for response for both sides '() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                    headers {
                        header 'Content-Type': 'text/xml'
                    }
                }
            }
        expect:
            new WiremockResponseStubStrategy(dsl).buildServerResponseContent() == new JsonSlurper().parseText('''
    {
        "status": 200,
        "headers": {
                "Content-Type": "text/xml"
        }
    }
    ''')
    }
}
