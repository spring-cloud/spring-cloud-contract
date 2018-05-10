package org.springframework.cloud.contract.verifier.dsl.wiremock

import groovy.json.JsonSlurper
import org.springframework.cloud.contract.spec.Contract
import spock.lang.Issue
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

	@Issue("#468")
	def "should not quote generated numbers"() {
		given:
			def irrelevantStatus = 200
			def contract = Contract.make {
				request {
				}
				response {
					status irrelevantStatus
					body([
						number: anyNumber(),
						integer: anyInteger(),
						positiveInt: anyPositiveInt(),
						double: anyDouble(),
					])
				}
			}
		when:
			def subject = new WireMockResponseStubStrategy(contract)
			def content = subject.buildClientResponseContent()
		then:
			Map body = new JsonSlurper().parseText(content.body) as Map
			assert body.get("number") instanceof Number
			assert body.get("integer") instanceof Integer
			assert body.get("positiveInt") instanceof Integer
			assert body.get("double") instanceof BigDecimal
	}
}
