package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification
/**
 * @author Tim Ysewyn
 */
class OutputMessageSpec extends Specification {

	def 'should set property when using the $() convenience method'() {
		given:
			Input input = new Input()
			DslProperty property
		when:
			input.with {
				property = $(consumer(regex("[0-9]{5}")))
			}
			def value = Integer.valueOf(property.serverValue as String)
		then:
			value >= 0
			value <= 99_999
	}
}
