package io.codearte.accurest.wiremock

import io.codearte.accurest.file.Contract
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class WiremockScenarioConverterSpec extends Specification {

	def "should generate first scenario step"() {
		given:
			DslToWireMockClientConverter converter = new DslToWireMockClientConverter()
			Path dsl = Paths.get(this.getClass().getResource("/converter/scenario/main_scenario/01_login.groovy").toURI())
		when:
			String content = converter.convertContent("Test", new Contract(dsl, false, 3, 0))
		then:
			content.contains('"requiredScenarioState" : "Started"')
			content.contains('"newScenarioState" : "Step1"')
			content.contains('"scenarioName" : "Scenario_Test"')
	}

	def "should generate mid scenario step"() {
		given:
			DslToWireMockClientConverter converter = new DslToWireMockClientConverter()
			Path dsl = Paths.get(this.getClass().getResource("/converter/scenario/main_scenario/02_showCart.groovy").toURI())
		when:
			String content = converter.convertContent("Test", new Contract(dsl, false, 3, 1))
		then:
			content.contains('"requiredScenarioState" : "Step1"')
			content.contains('"newScenarioState" : "Step2"')
			content.contains('"scenarioName" : "Scenario_Test"')
	}

	def "should generate last scenario step"() {
		given:
			DslToWireMockClientConverter converter = new DslToWireMockClientConverter()
			Path dsl = Paths.get(this.getClass().getResource("/converter/scenario/main_scenario/03_logout.groovy").toURI())
		when:
			String content = converter.convertContent("Test", new Contract(dsl, false, 3, 2))
		then:
			content.contains('"requiredScenarioState" : "Step2"')
			!content.contains('"newScenarioState"')
			content.contains('"scenarioName" : "Scenario_Test"')
	}
}
