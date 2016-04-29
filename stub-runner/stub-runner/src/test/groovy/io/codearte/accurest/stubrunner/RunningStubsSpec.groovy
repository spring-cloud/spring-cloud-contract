package io.codearte.accurest.stubrunner

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class RunningStubsSpec extends Specification {
	RunningStubs runningStubs = new RunningStubs([(new StubConfiguration('group', 'artifact', 'version', 'classifier')) : 100])

	def "should get port by group and artifact id"() {
		expect:
			runningStubs.getPort('group', 'artifact') == 100
	}

	def "should get port by artifact id"() {
		expect:
			runningStubs.getPort('artifact') == 100
	}

	def "should get port by group and artifact id in Ivy notation"() {
		expect:
			runningStubs.getPort('group:artifact') == 100
	}

	def "should get port by group, artifact id and version in Ivy notation"() {
		expect:
			runningStubs.getPort('group:artifact:version') == 100
	}

	def "should get port by group, artifact id, version and classifier in Ivy notation"() {
		expect:
			runningStubs.getPort('group:artifact:version:classifier') == 100
	}

	def "should return null if no stub has been found"() {
		expect:
			runningStubs.getPort('missing artifact id') == null
	}

	def "should find stub by group and artifact id"() {
		expect:
			runningStubs.isPresent('group', 'artifact')
	}

	def "should find stub by artifact id"() {
		expect:
			runningStubs.isPresent('artifact')
	}

	def "should find stub by group and artifact id in Ivy notation"() {
		expect:
			runningStubs.isPresent('group:artifact')
	}

	def "should find stub by group, artifact id and version in Ivy notation"() {
		expect:
			runningStubs.isPresent('group:artifact:version')
	}

	def "should find stub by group, artifact id, version and classifier in Ivy notation"() {
		expect:
			runningStubs.isPresent('group:artifact:version:classifier')
	}

	def "should return false if no stub has been found"() {
		expect:
			runningStubs.isPresent('missing artifact id') == false
	}

}
