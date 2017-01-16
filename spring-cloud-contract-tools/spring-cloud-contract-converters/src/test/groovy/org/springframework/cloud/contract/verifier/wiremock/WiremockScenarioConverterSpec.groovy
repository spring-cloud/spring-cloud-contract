/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.wiremock

import org.springframework.cloud.contract.verifier.file.ContractMetadata
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class WiremockScenarioConverterSpec extends Specification {

	def "should generate first scenario step"() {
		given:
			DslToWireMockClientConverter converter = new DslToWireMockClientConverter()
			Path dsl = Paths.get(this.getClass().getResource("/converter/scenario/main_scenario/01_login.groovy").toURI())
		when:
			String content = converter.convertContent("Test", new ContractMetadata(dsl, false, 3, 0))
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
			String content = converter.convertContent("Test", new ContractMetadata(dsl, false, 3, 1))
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
			String content = converter.convertContent("Test", new ContractMetadata(dsl, false, 3, 2))
		then:
			content.contains('"requiredScenarioState" : "Step2"')
			!content.contains('"newScenarioState"')
			content.contains('"scenarioName" : "Scenario_Test"')
	}
}
