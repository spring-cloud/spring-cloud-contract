package org.springframework.cloud.contract.verifier.dsl.wiremock

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification

class WireMockResponseStubStrategySpec extends Specification {
	def "should not quote floating point numbers"() {
		given:
			def irrelevantStatus = 200
			def contract = Contract.make {
				request {
				}
				response {
					status irrelevantStatus
					body([
						value: 1.5
					])
				}
			}
		when:
			def subject = new WireMockResponseStubStrategy(contract)
			def content = subject.buildClientResponseContent()
		then:
			'{"value":1.5}'.equals(content.body)
	}
}
