package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl
import io.coderate.accurest.dsl.WiremockResponseStubStrategy
import spock.lang.Ignore
import spock.lang.Specification

class WiremockGroovyDslResponseSpec extends Specification {

    def 'should generate response without body for #side side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                }
            }
        when:
            String wiremockStub = new WiremockResponseStubStrategy(dsl)."toWiremock${side}Stub"()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText(expectedStub)
        where:
            side << ['Client', 'Server']
            expectedStub << ['''
    {
        "response": {
            "status": 200
        }
    }
    ''',

    '''
    {
        "response": {
            "status": 200
        }
    }
    ''']
    }

    def 'should generate headers for response for client side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    headers {
                        header('Content-Type').equalTo('text/xml')
                    }
                    status 200
                }
            }
        when:
            String wiremockStub = new WiremockResponseStubStrategy(dsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "response": {
            "headers": {
                "Content-Type": {
                    "equalTo": "text/xml"
                },
            },
            "status": 200
        }
    }
    ''')
    }

    @Ignore("Not implemented yet")
    def 'should generate headers for response for server side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                    headers {
                        header('Content-Type').equalTo('text/xml')
                    }
                }
            }
        when:
            String wiremockStub = new WiremockResponseStubStrategy(dsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "response": {
            "status": 200,
            "headers":
                "Content-Type": "text/xml"
            }
        }
    }
    ''')
    }
}
