package io.codearte.accurest.plugin

import groovy.json.JsonSlurper
import nebula.test.IntegrationSpec
import spock.lang.Stepwise

@Stepwise
class BasicFunctionalSpec extends IntegrationSpec {

	def "should pass basic flow"() {
		when:
			copyResources("functionalTest/bootSimple", "")
		then:
			fileExists('build.gradle')
		when:
			def result = runTasksSuccessfully('clean', 'check') //clean to remove accidental output when importing SimpleBoot into Idea
		then:
			result.wasExecuted(":generateWiremockClientStubs")
			result.wasExecuted(":generateAccurest")

		and: "tests generated"
			fileExists("build/generated-sources/accurest/accurest/PairIdSpec.groovy")

		and: "client stubs generated"
			fileExists("build/production/bootSimple-stubs/repository/mappings/pairId/colleratePlacesFromTweet.json")

		and: "generated tests executed"
			fileExists("build/test-results/TEST-accurest.PairIdSpec.xml")
	}

	def "should generate valid client json stubs for simple input"() {
		given:
			copyResources("functionalTest/bootSimple", "")
		when:
			runTasksSuccessfully('generateWiremockClientStubs')
		then:
			def generatedClientJsonStub = file("build/production/bootSimple-stubs/repository/mappings/pairId/colleratePlacesFromTweet.json").text
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
        "body": {
            "equalTo": "[{\\"text\\":\\"Gonna see you at Warsaw\\"}]"
        }
    },
    "response": {
        "status": 200
    }
}
""")
	}
}
