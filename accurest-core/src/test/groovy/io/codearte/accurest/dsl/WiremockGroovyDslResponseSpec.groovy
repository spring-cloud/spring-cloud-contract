package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import io.coderate.accurest.dsl.GroovyDsl
import io.coderate.accurest.dsl.WiremockResponseStubStrategy
import spock.lang.Specification

class WiremockGroovyDslResponseSpec extends Specification {

    def 'should generate response without body for client side'() {
        given:
            GroovyDsl dsl = GroovyDsl.make {
                response {
                    status 200
                }
            }
        when:
            String wiremockStub = new WiremockResponseStubStrategy(dsl).toWiremockClientStub()
        then:
            new JsonSlurper().parseText(wiremockStub) == new JsonSlurper().parseText('''
    {
        "response": {
            "status": 200
        }
    }
    ''')
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
}
