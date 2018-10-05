package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Tim Ysewyn
 */
class InputSpec extends Specification {

	def 'should set property when using the $() convenience method'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}")))
			}
		then:
			(property.serverValue as String).matches(/[0-9]{5}/)
	}
}
