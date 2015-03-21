package io.codearte.accurest.plugin

import groovy.json.JsonSlurper
import nebula.test.IntegrationSpec
import spock.lang.Stepwise

@Stepwise
class BasicFunctionalSpec extends IntegrationSpec {

	private static final String GENERATED_TEST = "build//generated-sources//accurest//accurest//PairIdSpec.groovy"
	private static final String GENERATED_CLIENT_JSON_STUB = "build//production//bootSimple-stubs//repository//mappings//pairId//colleratePlacesFromTweet.json"
	private static final String GROOVY_DSL_CONTRACT = "repository//mappings//com//ofg//twitter-places-analyzer//pairId//colleratePlacesFromTweet.groovy"
	private static final String TEST_EXECUTION_XML_REPORT = "build/test-results/TEST-accurest.PairIdSpec.xml"

	void setup() {
		copyResources("functionalTest/bootSimple", "")
		runTasksSuccessfully('clean')   //delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow"() {
		given:
			assert fileExists('build.gradle')
		when:
			def result = runTasksSuccessfully('check')
		then:
			result.wasExecuted(":generateWiremockClientStubs")
			result.wasExecuted(":generateAccurest")

		and: "tests generated"
			fileExists(GENERATED_TEST)

		and: "client stubs generated"
			fileExists(GENERATED_CLIENT_JSON_STUB)

		and: "generated tests executed"
			fileExists(TEST_EXECUTION_XML_REPORT)
	}

	def "should generate valid client json stubs for simple input"() {
		when:
			runTasksSuccessfully('generateWiremockClientStubs')
		then:
			def generatedClientJsonStub = file(GENERATED_CLIENT_JSON_STUB).text
			new JsonSlurper().parseText(generatedClientJsonStub) == new JsonSlurper().parseText("""
{
    "request": {
        "method": "PUT",
        "headers": {
            "Content-Type": {
                "equalTo": "application/json"
            }
        },
        "url": "/api/12",
        "bodyPatterns": {
            "equalTo": "[{\\"text\\":\\"Gonna see you at Warsaw\\"}]"
        }
    },
    "response": {
        "status": 200
    }
}
""")
	}

	def "tasks should be up-to-date when appropriate"() {
		given:
			assert !fileExists(GENERATED_CLIENT_JSON_STUB)
			assert !fileExists(TEST_EXECUTION_XML_REPORT)
		when:
			runTasksSuccessfully('generateWiremockClientStubs', 'generateAccurest')
		then:
			fileExists(GENERATED_CLIENT_JSON_STUB)
			fileExists(GENERATED_TEST)

		when: "running generation without change inputs"
			def secondExecutionResult = runTasksSuccessfully('generateWiremockClientStubs', 'generateAccurest')

		then: "tasks should be up-to-date"
			secondExecutionResult.wasUpToDate(":generateWiremockClientStubs")
			secondExecutionResult.wasUpToDate(":generateAccurest")

		when: "inputs changed"
			def groovyDslFile = file(GROOVY_DSL_CONTRACT)
			groovyDslFile.text = groovyDslFile.text.replace("200", "599")

		and: "tasks run"
			def thirdExecutionResult = runTasksSuccessfully('generateWiremockClientStubs', 'generateAccurest')

		then: "tasks should be reexecuted"
			thirdExecutionResult.wasExecuted(":generateWiremockClientStubs")
			thirdExecutionResult.wasExecuted(":generateAccurest")

		and: "changes visible in generate files"
			file(GENERATED_CLIENT_JSON_STUB).text.contains("599")
			file(GENERATED_TEST).text.contains("599")
	}
}
