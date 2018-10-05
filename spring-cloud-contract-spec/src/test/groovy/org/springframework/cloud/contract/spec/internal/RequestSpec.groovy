package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class RequestSpec extends Specification {

	def 'should throw exception when on request side a value contains regex for server'() {
		given:
			Request request = new Request()
		when:
			request.with {
				value(consumer("foo"), producer(regex("foo")))
			}
		then:
			thrown(IllegalStateException)
		when:
			request.with {
				value(producer(regex("foo")), consumer("foo"))
			}
		then:
			thrown(IllegalStateException)
	}

	def 'should generate a value if only regex is passed for client'() {
		given:
			Request request = new Request()
			DslProperty property
		when:
			request.with {
				property = value(consumer(regex("[0-9]{5}")))
			}
		then:
			(property.serverValue as String).matches(/[0-9]{5}/)
	}
}
