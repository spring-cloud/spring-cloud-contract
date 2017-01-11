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

	def "should convert from contract to pact"() {
		given:
			Contract expectedContract = Contract.make {
				description("a retrieve Mallory request a user with username 'username' and password 'password' exists")
				request {
					method(GET())
					url("/mallory") {
						queryParameters {
							parameter("name", "ron")
							parameter("status", "good")
						}
					}
					body(id: "123", method: "create")
					stubMatchers {
						jsonPath('$.id', byRegex("[0-9]{3}"))
					}
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
					testMatchers {
						jsonPath('$[0][*].email', byType())
						jsonPath('$[0][*].id', byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
						jsonPath('$[0]', byType() {
							maxOccurrence(5)
						})
						jsonPath('$[0][*].userName', byType())
					}
				}
			}
		when:
			Collection<Contract> contracts = converter.convertFrom(pactJson)
		then:
			contracts == [expectedContract]
	}
}
