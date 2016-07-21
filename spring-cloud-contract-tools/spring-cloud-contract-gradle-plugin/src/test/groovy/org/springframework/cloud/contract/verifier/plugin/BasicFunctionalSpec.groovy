/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.testkit.runner.BuildResult
import org.springframework.cloud.contract.verifier.util.AssertionUtil
import spock.lang.Stepwise

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

@Stepwise
class BasicFunctionalSpec extends ContractVerifierIntegrationSpec {

	private static final String GENERATED_TEST = "build//generated-test-sources//contracts//contracts//spring//cloud//twitter_places_analyzer//PairIdSpec.groovy"
	private static final String GENERATED_CLIENT_JSON_STUB = "build//production//bootSimple-stubs//repository//mappings//spring//cloud//twitter-places-analyzer//pairId//collerate_PlacesFrom_Tweet.json"
	private static final String GROOVY_DSL_CONTRACT = "repository//mappings//spring//cloud//twitter-places-analyzer//pairId//collerate_PlacesFrom_Tweet.groovy"
	private static final String TEST_EXECUTION_XML_REPORT = "build/test-results/TEST-contracts.spring.cloud.twitter_places_analyzer.PairIdSpec.xml"

	def setup() {
		setupForProject("functionalTest/bootSimple")
		runTasksSuccessfully('clean') //delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow"() {
		when:
			BuildResult result = run("check", "publishToMavenLocal")
		then:
			result.task(":generateWireMockClientStubs").outcome == SUCCESS
			result.task(":generateContractTests").outcome == SUCCESS

		and: "tests generated"
			fileExists(GENERATED_TEST)

		and: "client stubs generated"
			fileExists(GENERATED_CLIENT_JSON_STUB)

		and: "generated tests executed"
			fileExists(TEST_EXECUTION_XML_REPORT)
	}

	def "should generate valid client json stubs for simple input"() {
		when:
			run('generateWireMockClientStubs')
		then:
			def generatedClientJsonStub = file(GENERATED_CLIENT_JSON_STUB).text
			AssertionUtil.assertThatJsonsAreEqual("""
	{
	  "request" : {
		"url" : "/api/12",
		"method" : "PUT",
		"bodyPatterns" : [ {
		  "matchesJsonPath" : "\$[*][?(@.text == 'Gonna see you at Warsaw')]"
		} ],
		"headers" : {
		  "Content-Type" : {
			"equalTo" : "application/json"
		  }
		}
	  },
	  "response" : {
		"status" : 200
	  },
	  "priority" : 2
	}
	""", generatedClientJsonStub)
	}

	//@Ignore("for some reason it's flickering")
	def "tasks should be up-to-date when appropriate"() {
		given:
			assert !fileExists(GENERATED_CLIENT_JSON_STUB)
			assert !fileExists(TEST_EXECUTION_XML_REPORT)
		when:
			runTasksSuccessfully('generateWireMockClientStubs', 'generateContractTests')
		then:
			fileExists(GENERATED_CLIENT_JSON_STUB)
			fileExists(GENERATED_TEST)

		when: "running generation without change inputs"
			def secondExecutionResult = run('generateWireMockClientStubs', 'generateContractTests')

		then: "tasks should be up-to-date"
			validateTasksOutcome(secondExecutionResult, UP_TO_DATE, 'generateWireMockClientStubs', 'generateContractTests')

		when: "inputs changed"
			def groovyDslFile = file(GROOVY_DSL_CONTRACT)
			groovyDslFile.text = groovyDslFile.text.replace("200", "599")

		and: "tasks run"
			def thirdExecutionResult = run('generateWireMockClientStubs', 'generateContractTests')

		then: "tasks should be reexecuted"
			validateTasksOutcome(thirdExecutionResult, SUCCESS, 'generateWireMockClientStubs', 'generateContractTests')

		and: "changes visible in generate files"
			file(GENERATED_CLIENT_JSON_STUB).text.contains("599")
			file(GENERATED_TEST).text.contains("599")
	}

}
