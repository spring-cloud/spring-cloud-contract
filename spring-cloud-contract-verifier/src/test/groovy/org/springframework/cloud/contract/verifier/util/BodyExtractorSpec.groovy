package org.springframework.cloud.contract.verifier.util

import org.springframework.cloud.contract.spec.internal.FromFileProperty
import spock.lang.Specification

/**
 * @author Adam Białas
 */
class BodyExtractorSpec extends Specification {

	def "should extract body from json file"() {
		given:
			def uri = BodyExtractorSpec.getResource("/classpath/response.json").toURI()
			def jsonFromFile = new FromFileProperty(new File(uri), String.class)
		expect:
			["status" : "RESPONSE"] == BodyExtractor.extractClientValueFromBody(jsonFromFile)
	}

}
