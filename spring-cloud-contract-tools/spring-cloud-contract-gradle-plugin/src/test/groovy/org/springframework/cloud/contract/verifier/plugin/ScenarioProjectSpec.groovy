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
class ScenarioProjectSpec extends ContractVerifierIntegrationSpec {

	def setup() {
		setupForProject("functionalTest/scenarioProject")
		runTasksSuccessfully('clean')
		//delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow for Spock"() {
		given:
			assert fileExists('build.gradle')
		expect:
			runTasksSuccessfully(checkAndPublishToMavenLocal())
			jarContainsContractVerifierContracts('fraudDetectionService/build/libs')
			BuildResult result = run("check", "--info", "--stacktrace")
			result.task(":fraudDetectionService:check").outcome == TaskOutcome.UP_TO_DATE
			result.task(":loanApplicationService:check").outcome == TaskOutcome.UP_TO_DATE
	}

	def "should pass basic flow for JUnit"() {
		given:
			assert fileExists('build.gradle')
		expect:
			switchToJunitTestFramework()
			emptySourceSet()
			runTasksSuccessfully(checkAndPublishToMavenLocal())
			jarContainsContractVerifierContracts('fraudDetectionService/build/libs')
			BuildResult result = run("check", "--info", "--stacktrace")
			result.task(":fraudDetectionService:check").outcome == TaskOutcome.UP_TO_DATE
			result.task(":loanApplicationService:check").outcome == TaskOutcome.UP_TO_DATE
	}

	def "should properly work with build cache"() {
		given:
			def gradleUserHomeDir = new File(testProjectDir, ".gradleUserHome")
			gradleUserHomeDir.mkdirs()
			String[] tasks = ["-g ${gradleUserHomeDir}", "clean", "check", "publishToMavenLocal", "--info", "--stacktrace", "--build-cache"]
			assert fileExists("build.gradle")

		expect:
			runTasksSuccessfully(tasks)
			jarContainsContractVerifierContracts('fraudDetectionService/build/libs')
			BuildResult result = run(tasks)
			result.task(":fraudDetectionService:copyContracts").outcome == TaskOutcome.FROM_CACHE
			result.task(":fraudDetectionService:generateContractTests").outcome == TaskOutcome.FROM_CACHE
			result.task(":fraudDetectionService:contractTest").outcome == TaskOutcome.FROM_CACHE
			result.task(":fraudDetectionService:generateClientStubs").outcome == TaskOutcome.FROM_CACHE
			result.task(":loanApplicationService:check").outcome == TaskOutcome.UP_TO_DATE
	}
}
