package io.codearte.accurest.plugin

import nebula.test.IntegrationSpec
import spock.lang.Stepwise

@Stepwise
class ScenarioProjectSpec extends AccurestIntegrationSpec {

	void setup() {
		copyResources("functionalTest/scenarioProject", "")
		runTasksSuccessfully('clean')   //delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow for Spock"() {
		given:
			assert fileExists('build.gradle')
		expect:
			runTasksSuccessfully('check')
	}

	def "should pass basic flow for JUnit"() {
		given:
			assert fileExists('build.gradle')
		expect:
			switchToJunitTestFramework()
			runTasksSuccessfully('check')
	}

}
