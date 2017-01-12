package org.springframework.cloud.contract.verifier.spec.converter.pact

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSpecVersion
import groovy.json.JsonOutput
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.cloud.contract.spec.Contract
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import spock.lang.Ignore
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
				description("a retrieve Mallory request a user with username 'username' and password 'password' exists")
				request {
					method(GET())
					url("/mallory") {
						queryParameters {
							parameter("name", "ron")
							parameter("status", "good")
						}
					}
					headers {
						contentType(applicationJson())
					}
					body(id: "123", method: "create")
					stubMatchers {
						jsonPath('$.id', byRegex("[0-9]{3}"))
					}
				}
				response {
					status(200)
					headers {
						contentType(applicationJson())
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

	def "should convert from contract to pact"() {
		given:
			Collection<Contract> inputContracts = [
					Contract.make {
						description("a retrieve Mallory request")
						request {
							method(GET())
							url("/mallory") {
								queryParameters {
									parameter("name", "ron")
									parameter("status", "good")
								}
							}
							headers {
								contentType(applicationJson())
							}
							body(
									id: 123,
									method: $(stub(regex("[0][1][2]"))),
									something: "foo"
							)
							stubMatchers {
								jsonPath('$.id', byRegex("[0-9]{3}"))
								jsonPath('$.something', byEquality())
							}
						}
						response {
							status(200)
							headers {
								contentType(applicationJson())
							}
							body([[
										  [email: "rddtGwwWMEhnkAPEmsyE",
										   id: "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
										   number: $(producer(regex("[0-9]{3}")), consumer(923)),
										   something: "foo",
										   userName: "AJQrokEGPAVdOHprQpKP"]
								  ]])
							testMatchers {
								jsonPath('$[0][*].email', byType())
								jsonPath('$[0][*].id', byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
								jsonPath('$[0]', byType() {
									minOccurrence(1)
									maxOccurrence(5)
								})
								jsonPath('$[0][*].userName', byType())
								jsonPath('$[0][*].something', byEquality())
							}
						}
					}
			]
		String expectedJson = '''
{
  "provider": {
    "name": "Provider"
  },
  "consumer": {
    "name": "Consumer"
  },
  "interactions": [
    {
      "description": "a retrieve Mallory request",
      "request": {
        "method": "GET",
        "path": "\\/mallory",
        "query": "name=ron&status=good",
        "headers": {
          "Content-Type": "application\\/json"
        },
        "body": {
          "id": 123,
          "method": "012",
          "something": "foo"
        },
        "matchingRules": {
          "$.id": {
            "match": "regex",
            "regex": "[0-9]{3}"
          },
          "$.something": {
            "match": "equality"
          }
        }
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application\\/json"
        },
        "body": [
          [
            {
              "email": "rddtGwwWMEhnkAPEmsyE",
              "id": "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
              "number": 923,
              "userName": "AJQrokEGPAVdOHprQpKP"
            }
          ]
        ],
        "matchingRules": {
          "$[0][*].email": {
            "match": "type"
          },
          "$[0][*].id": {
            "match": "regex",
            "regex": "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
          },
          "$[0]": {
            "match": "type",
            "min": 1,
            "max": 5
          },
          "$[0][*].userName": {
            "match": "type"
          },
          "$[0][*].something": {
            "match": "equality"
          }
        }
      }
    }
  ],
  "metadata": {
    "pact-specification": {
      "version": "2.0.0"
    },
    "pact-jvm": {
      "version": "2.4.18"
    }
  }
}
'''
		when:
			Pact pact = converter.convertTo(inputContracts)
		then:
			String actual = JsonOutput.toJson(pact.toMap(PactSpecVersion.V2))
			JSONAssert.assertEquals(expectedJson, actual, false)
	}

	// TODO: Convert from the dsls to pact and reuse those pacts in samples/pact
	@Ignore
	def "should convert contracts from samples to pacts"() {
		given:
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources("contracts/*.groovy")
		expect:
			converter.isAccepted(invalidPact)
	}
}
