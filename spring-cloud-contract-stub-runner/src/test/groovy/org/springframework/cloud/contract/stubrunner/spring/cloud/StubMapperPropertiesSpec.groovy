package org.springframework.cloud.contract.stubrunner.spring.cloud

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubMapperPropertiesSpec extends Specification {

	def "should convert ivy notation to serviceId by fallbacking to artifactId if nothing else matches"() {
		given:
			Map<String, String> idsToServiceIds = [
					fraudDetectionServer: 'someNameThatShouldMapFraudDetectionServer'
			]
			StubMapperProperties properties = new StubMapperProperties(idsToServiceIds: idsToServiceIds)
		expect:
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('fraudDetectionServer')
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('groupid:fraudDetectionServer')
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('groupid:fraudDetectionServer:+:classifier')
	}
}
