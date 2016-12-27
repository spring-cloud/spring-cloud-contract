package org.springframework.cloud.contract.verifier.messaging.internal

import spock.lang.Specification

class ContractVerifierObjectMapperSpec extends Specification {

	ContractVerifierObjectMapper mapper = new ContractVerifierObjectMapper()

	def "should convert an object into a json representation"() {
		given:
			MyClass input = new MyClass(foo: "bar")
		when:
			String result = mapper.writeValueAsString(input)
		then:
			result == '''{"foo":"bar"}'''
	}

	def "should convert bytes into a json representation"() {
		given:
			String input = '''{"foo":"bar"}'''
		when:
			String result = mapper.writeValueAsString(input.bytes)
		then:
			result == '''{"foo":"bar"}'''
	}

	class MyClass {
		String foo
	}
}
