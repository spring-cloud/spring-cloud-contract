package io.codearte.accurest.dsl

import groovy.json.JsonSlurper
import spock.lang.Specification

class WireMockGroovyDslResponseSpec extends Specification {

	def 'should generate response without body for client side'() {
		given:
			GroovyDsl dsl = GroovyDsl.make {
				response {
					status 200
				}
			}
		expect:
			new WireMockResponseStubStrategy(dsl).buildClientResponseContent() == new JsonSlurper().parseText(expectedStub)
		where:
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
						header 'Content-Type', $(client('text/xml'), server('text/*'))
					}
					status 200
				}
			}
		expect:
			new WireMockResponseStubStrategy(dsl).buildClientResponseContent() == new JsonSlurper().parseText('''
    {
            "headers": {
                "Content-Type": "text/xml"
            },
            "status": 200
    }
    ''')
	}

}
