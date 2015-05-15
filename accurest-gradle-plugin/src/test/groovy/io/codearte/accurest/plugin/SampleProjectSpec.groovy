package io.codearte.accurest.plugin

import nebula.test.IntegrationSpec
import spock.lang.Stepwise

@Stepwise
class SampleProjectSpec extends IntegrationSpec {

	void setup() {
		copyResources("functionalTest/sampleProject", "")
		runTasksSuccessfully('clean')   //delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow"() {
		given:
			assert fileExists('build.gradle')
		expect:
			runTasksSuccessfully('check')
	}

}
