package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSpecVersion
import groovy.json.JsonOutput
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
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
          "$.body.id": {
            "match": "regex",
            "regex": "[0-9]{3}"
          },
          "$.body.something": {
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
          "$.body[0][*].email": {
            "match": "type"
          },
          "$.body[0][*].id": {
            "match": "regex",
            "regex": "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
          },
          "$.body[0]": {
            "match": "type",
            "min": 1,
            "max": 5
          },
          "$.body[0][*].userName": {
            "match": "type"
          },
          "$.body[0][*].something": {
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

	def "should fail to convert from contract to pact when contract has execution property in request"() {
		given:
			Collection<Contract> inputContracts = [
					Contract.make {
						request {
							method(GET())
							url("/mallory")
							body(
									id: $(c("foo"), p(execute("foo")))
							)
						}
						response {
							status(200)

						}
					}
			]
		when:
			converter.convertTo(inputContracts)
		then:
			def e = thrown(UnsupportedOperationException)
			e.message.contains("execution property")
	}

	def "should fail to convert from contract to pact when contract has execution property in response"() {
		given:
			Collection<Contract> inputContracts = [
					Contract.make {
						request {
							method(GET())
							url("/mallory")
						}
						response {
							status(200)
							body(
									id: $(c(execute("foo")), p("foo"))
							)
						}
					}
			]
		when:
			converter.convertTo(inputContracts)
		then:
			def e = thrown(UnsupportedOperationException)
			e.message.contains("execution property")
	}

	def "should convert contracts from samples to pacts"() {
		given:
			Resource[] contractResources = new PathMatchingResourcePatternResolver().getResources("contracts/*.groovy")
			Resource[] pactResources = new PathMatchingResourcePatternResolver().getResources("contracts/*.json")
			Map<String, Collection<Contract>> contracts = contractResources.collectEntries { [(it.filename) : ContractVerifierDslConverter.convertAsCollection(it.file)] }
			Map<String, String> jsonPacts = pactResources.collectEntries { [(it.filename) : it.file.text] }
		when:
			Map<String, Pact> pacts = contracts.entrySet().collectEntries { [(it.key) : converter.convertTo(it.value)] }
		then:
			pacts.entrySet().each {
				String convertedPactAsText = JsonOutput.toJson(it.value.toMap(PactSpecVersion.V2))
				String pactFileName = it.key.replace("groovy", "json")
				println "File name [${it.key}]"
				JSONAssert.assertEquals(jsonPacts.get(pactFileName), convertedPactAsText, false)
			}
	}
}



// file creator
/*
pacts.entrySet().each {
	new File("target/${it.key.replace("groovy", "json")}").text = JsonOutput.toJson(it.value.toMap(PactSpecVersion.V2))
}
 */
