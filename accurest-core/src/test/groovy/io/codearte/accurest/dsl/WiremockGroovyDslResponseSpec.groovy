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
		expect:
			new WiremockResponseStubStrategy(dsl).buildClientResponseContent() == new JsonSlurper().parseText(expectedStub)
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

}
