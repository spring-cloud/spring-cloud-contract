/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore
import spock.lang.Stepwise

@Ignore
@Stepwise
class ScenarioProjectKotlinSpec extends ContractVerifierKotlinIntegrationSpec {

	def setup() {
		setupForProject("functionalTest/scenarioProjectKotlin")
		runTasksSuccessfully('clean')
		//delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow for Spock"() {
		given:
			assert fileExists('build.gradle.kts')
		expect:
			runTasksSuccessfully(checkAndPublishToMavenLocal())
			jarContainsContractVerifierContracts('fraudDetectionService/build/libs')
			BuildResult result = run("check", "--info", "--stacktrace")
			result.task(":fraudDetectionService:check").outcome == TaskOutcome.UP_TO_DATE
			result.task(":loanApplicationService:check").outcome == TaskOutcome.UP_TO_DATE
	}

	def "should pass basic flow for JUnit"() {
		given:
			assert fileExists('build.gradle.kts')
		expect:
			switchToJunitTestFramework()
			runTasksSuccessfully(checkAndPublishToMavenLocal())
			jarContainsContractVerifierContracts('fraudDetectionService/build/libs')
			BuildResult result = run("check", "--info", "--stacktrace")
			result.task(":fraudDetectionService:check").outcome == TaskOutcome.UP_TO_DATE
			result.task(":loanApplicationService:check").outcome == TaskOutcome.UP_TO_DATE
	}

}
