package io.codearte.accurest.stubrunner

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class RunningStubsSpec extends Specification {
	RunningStubs runningStubs = new RunningStubs([(new StubConfiguration('a', 'b', 'c')) : 100])

	def "should get port by group and artifact id"() {
		expect:
			runningStubs.getPort('a', 'b') == 100
	}

	def "should get port by artifact id"() {
		expect:
			runningStubs.getPort('b') == 100
	}

	def "should get port by group and artifact id in Ivy notation"() {
		expect:
			runningStubs.getPort('a:b') == 100
	}

	def "should get port by group, artifact id and classifier in Ivy notation"() {
		expect:
			runningStubs.getPort('a:b:c') == 100
	}

	def "should return null if no stub has been found"() {
		expect:
			runningStubs.getPort('missing artifact id') == null
	}

	def "should find stub by group and artifact id"() {
		expect:
			runningStubs.isPresent('a', 'b')
	}

	def "should find stub by artifact id"() {
		expect:
			runningStubs.isPresent('b')
	}

	def "should find stub by group and artifact id in Ivy notation"() {
		expect:
			runningStubs.isPresent('a:b')
	}

	def "should find stub by group, artifact id and classifier in Ivy notation"() {
		expect:
			runningStubs.isPresent('a:b:c')
	}

	def "should return false if no stub has been found"() {
		expect:
			runningStubs.isPresent('missing artifact id') == false
	}

}
