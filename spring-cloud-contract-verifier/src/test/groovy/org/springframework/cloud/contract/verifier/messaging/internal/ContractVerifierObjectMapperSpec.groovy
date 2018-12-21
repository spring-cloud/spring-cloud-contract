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

	def "should convert an object into a byte[] representation"() {
		given:
			MyClass input = new MyClass(foo: "bar")
		when:
			byte[] result = mapper.writeValueAsBytes(input)
		then:
			result == '''{"foo":"bar"}'''.bytes
	}

	def "should convert a String into a byte[] representation"() {
		when:
			byte[] result = mapper.writeValueAsBytes('''{"foo":"bar"}''')
		then:
			result == '''{"foo":"bar"}'''.bytes
	}

	def "should pass byte[] as a byte[] representation"() {
		when:
			byte[] result = mapper.writeValueAsBytes('''{"foo":"bar"}'''.bytes)
		then:
			result == '''{"foo":"bar"}'''.bytes
	}

	static class MyClass {
		String foo
	}
}
