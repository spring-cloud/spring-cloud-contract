package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class ResponseSpec extends Specification {

	def 'should throw exception when on response side a value contains regex for client'() {
		given:
			Response response = new Response()
		when:
			response.with {
				value(producer("foo"), consumer(regex("foo")))
			}
		then:
			thrown(IllegalStateException)
		when:
			response.with {
				value(consumer(regex("foo")), producer("foo"))
			}
		then:
			thrown(IllegalStateException)
	}

	def 'should generate a value if only regex is passed for server'() {
		given:
			Response request = new Response()
			DslProperty property
		when:
			request.with {
				property = value(producer(regex("[0-9]{5}")))
			}
		then:
			(property.serverValue as String).matches(/[0-9]{5}/)
	}
}
