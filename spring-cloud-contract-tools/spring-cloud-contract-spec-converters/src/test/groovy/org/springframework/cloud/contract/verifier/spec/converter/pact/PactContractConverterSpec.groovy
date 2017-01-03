package org.springframework.cloud.contract.verifier.spec.converter.pact

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Marcin Grzejszczak
 */
class PactContractConverterSpec extends Specification {

	File pactJson = new File(PactContractConverterSpec.getResource("/pact/pact.json").toURI())
	@Subject PactContractConverter converter = new PactContractConverter()

	def "should accept json files that are pact files"() {
		expect:
			converter.isAccepted(pactJson)
	}

	def "should reject json files that are pact files"() {
		given:
			File invalidPact = new File(PactContractConverterSpec.getResource("/pact/invalid_pact.json").toURI())
		expect:
			converter.isAccepted(invalidPact)
	}

	def "should convert from pact to contract"() {
		given:
			Contract expectedContract = Contract.make {
				description("Consumer [Consumer] -> provider [Alice Service] interaction no [0]\n\na retrieve Mallory request")
				request {
					method(GET())
					url("/mallory") {
						queryParameters {
							parameter("name", "ron")
							parameter("status", "good")
						}
					}
					body(id: "123", method: "create")
				}
				response {
					status(200)
					headers {
						header("Content-Type", applicationJson())
					}
					body([[
							[email: "rddtGwwWMEhnkAPEmsyE",
							id: "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
							userName: "AJQrokEGPAVdOHprQpKP"]
						]])
				}
			}
		when:
			Collection<Contract> contracts = converter.convertFrom(pactJson)
		then:
			contracts == [expectedContract]
	}
}
