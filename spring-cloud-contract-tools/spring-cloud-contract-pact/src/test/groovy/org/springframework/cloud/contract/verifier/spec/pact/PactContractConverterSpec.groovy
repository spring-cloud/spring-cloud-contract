/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSpecVersion
import groovy.json.JsonOutput
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Subject

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
class PactContractConverterSpec extends Specification {

	File pactJson = new File(PactContractConverterSpec.getResource("/pact/pact.json").
		toURI())
	File pact509Json = new File(PactContractConverterSpec.
		getResource("/pact/pact_509.json").toURI())
	File pactv2Json = new File(PactContractConverterSpec.
		getResource("/pact/pact_v2.json").toURI())
	File pactv3Json = new File(PactContractConverterSpec.
		getResource("/pact/pact_v3.json").toURI())
	File pactv3Issue889Json = new File(PactContractConverterSpec.
		getResource("/pact/pact_v3_issue_889.json").toURI())
	File pactv3MessagingJson = new File(PactContractConverterSpec.
		getResource("/pact/pact_v3_messaging.json").toURI())
	File pactv3UnsupportedRuleLogicJson = new File(PactContractConverterSpec.
		getResource("/pact/pact_v3_unsupported_rule_logic.json").toURI())
	@Subject
	PactContractConverter converter = new PactContractConverter()

	def "should accept json files that are pact files"() {
		expect:
			converter.isAccepted(pactJson)
	}

	def "should reject json files that are pact files"() {
		given:
			File invalidPact = new File(PactContractConverterSpec.
				getResource("/pact/invalid_pact.json").toURI())
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
					bodyMatchers {
						jsonPath('$.id', byRegex("[0-9]{3}"))
					}
				}
				response {
					status(200)
					headers {
						contentType(applicationJson())
					}
					body([[
							  [email   : "rddtGwwWMEhnkAPEmsyE",
							   id      : "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
							   userName: "AJQrokEGPAVdOHprQpKP"]
						  ]])
					bodyMatchers {
						jsonPath('$[0][*].email', byType())
						jsonPath('$[0][*].id',
							byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
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

	@Issue("#509")
	def "should convert from pact with matching rules to whole body to contract"() {
		given:
			Contract expectedContract = Contract.make {
				description("a request to POST a person provider accepts a new person")
				request {
					method(POST())
					url("/user-service/users")
					headers {
						contentType(applicationJson())
					}
					body(firstName: "Arthur", lastName: "Dent")
				}
				response {
					status(201)
					headers {
						contentType(applicationJson())
					}
					body(id: 42, firstName: "Arthur", lastName: "Dent")
					bodyMatchers {
						jsonPath('''$.['id']''', byType())
						jsonPath('''$.['lastName']''', byType())
						jsonPath('''$.['firstName']''', byType())
					}
				}
			}
		when:
			Collection<Contract> contracts = converter.convertFrom(pact509Json)
		then:
			contracts == [expectedContract]
	}

	def "should convert from contract to pact"() {
		given:
			Collection<Contract> inputContracts = [
				Contract.make {
					name("my_consumer___my_producer___testname")
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
						bodyMatchers {
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
								  [email                : "rddtGwwWMEhnkAPEmsyE",
								   id                   : "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
								   number               :
									   $(producer(regex("[0-9]{3}")), consumer(923)),
								   positiveInteger      : 1234567890,
								   negativeInteger      : -1234567890,
								   positiveDecimalNumber: 123.4567890,
								   negativeDecimalNumber: -123.4567890,
								   something            : "foo",
								   userName             : "AJQrokEGPAVdOHprQpKP",
								   nullValue            : null]
							  ]])
						bodyMatchers {
							jsonPath('$[0][*].email', byType())
							jsonPath('$[0][*].id',
								byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
							jsonPath('$[0]', byType() {
								minOccurrence(1)
								maxOccurrence(5)
							})
							jsonPath('$[0][*].number', byRegex(number()))
							jsonPath('$[0][*].positiveInteger', byRegex(anInteger()))
							jsonPath('$[0][*].negativeInteger', byRegex(anInteger()))
							jsonPath('$[0][*].positiveDecimalNumber', byRegex(aDouble()))
							jsonPath('$[0][*].negativeDecimalNumber', byRegex(aDouble()))
							jsonPath('$[0][*].userName', byType())
							jsonPath('$[0][*].something', byEquality())
							jsonPath('$[0][*].nullValue', byNull())
						}
					}
				}
			]
			String expectedJson = '''
{
  "provider": {
    "name": "my_producer"
  },
  "consumer": {
    "name": "my_consumer"
  },
  "interactions": [
    {
      "description": "a retrieve Mallory request",
      "request": {
        "method": "GET",
        "path": "\\/mallory",
        "query": {
        	"name": ["ron"],
        	"status": ["good"]
        },
        "headers": {
          "Content-Type": "application\\/json"
        },
        "body": {
          "id": 123,
          "method": "012",
          "something": "foo"
        },
        "matchingRules": {
          "body": {
            "$.id": {
              "matchers": [{
                "match": "regex",
                "regex": "[0-9]{3}"
              }]
            },
            "$.something": {
              "matchers": [{
                "match": "equality"
              }]
            }
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
              "positiveInteger": 1234567890,
              "negativeInteger": -1234567890,
              "positiveDecimalNumber": 123.4567890,
              "negativeDecimalNumber": -123.4567890,
              "userName": "AJQrokEGPAVdOHprQpKP",
              "something": "foo",
              "nullValue": null
            }
          ]
        ],
        "matchingRules": {
          "body": {
            "$[0][*].email": {
              "matchers": [{
                "match": "type"
              }]
            },
            "$[0][*].id": {
              "matchers": [{
                "match": "regex",
                "regex": "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
              }]
            },
            "$[0]": {
              "matchers": [{
                "match": "type",
                "min": 1,
                "max": 5
              }]
            },
            "$[0][*].number": {
              "matchers": [{
                "match": "number"
              }]
            },
            "$[0][*].positiveInteger": {
              "matchers": [{
                "match": "integer"
              }]
            },
            "$[0][*].negativeInteger": {
              "matchers": [{
                "match": "integer"
              }]
            },
            "$[0][*].positiveDecimalNumber": {
              "matchers": [{
                "match": "decimal"
              }]
            },
            "$[0][*].negativeDecimalNumber": {
              "matchers": [{
                "match": "decimal"
              }]
            },
            "$[0][*].userName": {
              "matchers": [{
                "match": "type"
              }]
            },
            "$[0][*].something": {
              "matchers": [{
                "match": "equality"
              }]
            },
            "$[0][*].nullValue": {
              "matchers": [{
                "match": "null"
              }]
            }
          }
        }
      }
    }
  ],
  "metadata": {
    "pact-specification": {
      "version": "3.0.0"
    },
    "pact-jvm": {
      "version": "3.5.13"
    }
  }
}
'''
		when:
			Pact pact = converter.convertTo(inputContracts).get(0)
		then:
			String actual = JsonOutput.toJson(pact.toMap(PactSpecVersion.V3))
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
			Resource[] contractResources = new PathMatchingResourcePatternResolver().
				getResources("contracts/*.groovy")
			Resource[] pactResources = new PathMatchingResourcePatternResolver().
				getResources("contracts/*.json")
			Map<String, Collection<Contract>> contracts = contractResources.
				collectEntries {
					[(it.filename): ContractVerifierDslConverter.
						convertAsCollection(new File("/"), it.file)]
				}
			Map<String, String> jsonPacts = pactResources.
				collectEntries { [(it.filename): it.file.text] }
		when:
			Map<String, Collection<Pact>> pacts = contracts.entrySet().
				collectEntries { [(it.key): converter.convertTo(it.value)] }
		then:
			pacts.entrySet().each {
				String convertedPactAsText = JsonOutput.
					toJson(it.value[0].toMap(PactSpecVersion.V3))
				String pactFileName = it.key.replace("groovy", "json")
				println "File name [${it.key}]"
				JSONAssert.
					assertEquals(jsonPacts.get(pactFileName), convertedPactAsText, true)
			}
	}

	def "should convert contracts from grouped contracts to pacts"() {
		given:
			List<Contract> contracts = ContractVerifierDslConverter.
				convertAsCollection(new File("/"),
					new File("src/test/resources/contracts/grouped/shouldWorkWithBeer.groovy"))
		when:
			Collection<Pact> pacts = converter.convertTo(contracts)
		then:
			pacts.size() == 1
			String convertedPactAsText = JsonOutput.
				toJson(pacts.first().toMap(PactSpecVersion.V3))
			JSONAssert.assertEquals(
				new File("src/test/resources/contracts/grouped/shouldWorkWithBeer.json").text,
				convertedPactAsText, false)
	}

	def "should convert pacts to strings"() {
		given:
			List<Contract> contracts = ContractVerifierDslConverter.
				convertAsCollection(new File("/"),
					new File("src/test/resources/contracts/grouped/shouldWorkWithBeer.groovy"))
		and:
			Collection<Pact> pacts = converter.convertTo(contracts)
		when:
			Map<String, byte[]> strings = converter.store(pacts)
		then:
			strings.size() == 1
			strings.keySet().first().
				startsWith("10-04-pact-consumer_10-05-pact-producer_")
			strings.keySet().first().endsWith(".json")
			JSONAssert.assertEquals(
				new File("src/test/resources/contracts/grouped/shouldWorkWithBeer.json").text,
				new String(strings.values().first()), false)
	}

	def "should convert from pact v2 to two SC contracts"() {
		given:
			Collection<Contract> expectedContracts = [
				Contract.make {
					description("get all users for max a user with an id named 'user' exists")
					request {
						method(GET())
						url("/idm/user")
					}
					response {
						status(200)
						headers {
							contentType(applicationJson())
						}
						body([[
								  [email   : "rddtGwwWMEhnkAPEmsyE",
								   id      : "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
								   userName: "AJQrokEGPAVdOHprQpKP"]
							  ]])
						bodyMatchers {
							jsonPath('$[0][*].email', byType())
							jsonPath('$[0][*].id',
								byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
							jsonPath('$[0]', byType() {
								maxOccurrence(5)
							})
							jsonPath('$[0][*].userName', byType())
						}
					}
				},
				Contract.make {
					description("get all users for min a user with an id named 'user' exists")
					request {
						method(GET())
						url("/idm/user")
					}
					response {
						status(200)
						headers {
							contentType(applicationJson())
						}
						body([[
								  [email   : "DPvAfkCZpOBZWzKYiDMC",
								   id      : "95d0371b-bf30-4943-90a8-8bb1967c4cb2",
								   userName: "GIUlVKoiLdHLYNKGbcSy"]
							  ]])
						bodyMatchers {
							jsonPath('$[0][*].email', byType())
							jsonPath('$[0][*].id',
								byRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
							jsonPath('$[0]', byType() {
								minOccurrence(5)
							})
							jsonPath('$[0][*].userName', byType())
						}
					}
				}
			]
		when:
			Collection<Contract> contracts = converter.convertFrom(pactv2Json)
		then:
			contracts == expectedContracts
	}

	def "should convert from pact v3 to three SC contracts"() {
		given:
			Collection<Contract> expectedContracts = [
				Contract.make {
					description("java test interaction with a DSL array body")
					request {
						method(GET())
						url("/")
						headers {
							contentType(applicationJsonUtf8())
							header("Some-Header",
								$(c(regex("[a-zA-Z]{9}")), p("someValue")))
							header("someHeaderWithJsonContent", '{"issue":"#595"}')
						}
					}
					response {
						status(200)
						headers {
							contentType(applicationJsonUtf8())
							header("Some-Header",
								$(c("someValue"), p(regex("[a-zA-Z]{9}"))))
							header("someHeaderWithJsonContent", '{"issue":"#595"}')
						}
						body([
							[
								"dob"                  : "07/19/2016",
								"id"                   : 8958464620,
								"name"                 : "Rogger the Dogger",
								"timestamp"            : "2016-07-19T12:14:39",
								"nullValue"            : null,
								"aNumber"              : 1234567890,
								"positiveInteger"      : 1234567890,
								"negativeInteger"      : -1234567890,
								"positiveDecimalNumber": 123.4567890,
								"negativeDecimalNumber": -123.4567890
							],
							[
								"dob"                  : "07/19/2016",
								"id"                   : 4143398442,
								"name"                 : "Cat in the Hat",
								"timestamp"            : "2016-07-19T12:14:39",
								"nullValue"            : null,
								"aNumber"              : 1234567890,
								"positiveInteger"      : 1234567890,
								"negativeInteger"      : -1234567890,
								"positiveDecimalNumber": 123.4567890,
								"negativeDecimalNumber": -123.4567890
							]
						])
						bodyMatchers {
							jsonPath('$[0].id', byType())
							jsonPath('$[1].id', byType())
							jsonPath('$[*].nullValue', byNull())
							jsonPath('$[*].aNumber', byRegex(number()))
							jsonPath('$[*].positiveInteger', byRegex(anInteger()))
							jsonPath('$[*].negativeInteger', byRegex(anInteger()))
							jsonPath('$[*].positiveDecimalNumber', byRegex(aDouble()))
							jsonPath('$[*].negativeDecimalNumber', byRegex(aDouble()))
						}
					}
				},
				Contract.make {
					description("test interaction with a array body with templates")
					request {
						method(GET())
						url("/")
					}
					response {
						status(200)
						headers {
							contentType(applicationJsonUtf8())
						}
						body([
							[
								"dob" : "2016-07-19",
								"id"  : 1943791933,
								"name": "ZSAICmTmiwgFFInuEuiK"
							],
							[
								"dob" : "2016-07-19",
								"id"  : 1943791933,
								"name": "ZSAICmTmiwgFFInuEuiK"
							],
							[
								"dob" : "2016-07-19",
								"id"  : 1943791933,
								"name": "ZSAICmTmiwgFFInuEuiK"
							]
						])
						bodyMatchers {
							jsonPath('$[2].name', byType())
							jsonPath('$[0].id', byType())
							jsonPath('$[1].id', byType())
							jsonPath('$[2].id', byType())
							jsonPath('$[1].name', byType())
							jsonPath('$[0].name', byType())
							jsonPath('$[0].dob', byDate())
						}
					}
				},
				Contract.make {
					description("test interaction with an array like matcher")
					request {
						method(GET())
						url("/")
					}
					response {
						status(200)
						headers {
							contentType(applicationJsonUtf8())
						}
						body([
							"data": [
								"array1": [[
											   "dob" : "2016-07-19",
											   "id"  : 1600309982,
											   "name": "FVsWAGZTFGPLhWjLuBOd"
										   ]],
								"array2": [[
											   "address": "127.0.0.1",
											   "name"   : "jvxrzduZnwwxpFYrQnpd"
										   ]],
								"array3": [[
											   [
												   "itemCount": 652571349
											   ]
										   ]]
							],
							"id"  : 7183997828
						])
						bodyMatchers {
							jsonPath('$.data.array3[0]', byType() {
								maxOccurrence(5)
							})
							jsonPath('$.data.array1', byType() {
								minOccurrence(0)
							})
							jsonPath('$.data.array2', byType() {
								minOccurrence(1)
							})
							jsonPath('$.id', byType())
							jsonPath('$.data.array2[*].name', byType())
							jsonPath('$.data.array2[*].address',
								byRegex("(\\d{1,3}\\.)+\\d{1,3}"))
							jsonPath('$.data.array1[*].name', byType())
							jsonPath('$.data.array1[*].id', byType())
						}
					}
				}
			]
		when:
			Collection<Contract> contracts = converter.convertFrom(pactv3Json)
		then:
			contracts == expectedContracts
	}

	@Issue("#889")
	def "should not throw an exception when parsing Pact file with different matchers"() {
		when:
			converter.convertFrom(pactv3Issue889Json)
		then:
			noExceptionThrown()
	}

	def "should convert from pact v3 messaging to one SC message contract"() {
		given:
			Collection<Contract> expectedContracts = [
				Contract.make {
					description()
					label 'message sent to activemq:output'
					input {
						triggeredBy('bookReturnedTriggered()')
					}
					outputMessage {
						sentTo 'activemq:output'
						body([
							bookName: "foo"
						])
						headers {
							header('BOOK-NAME', 'foo')
							messagingContentType(applicationJson())
						}
						bodyMatchers {
							jsonPath('$.bookName', byType())
						}
					}
				}
			]
		when:
			Collection<Contract> contracts = converter.convertFrom(pactv3MessagingJson)
		then:
			contracts == expectedContracts
	}

	def "should fail to convert a pact v3 contract with unsupported rule logic"() {
		when:
			converter.convertFrom(pactv3UnsupportedRuleLogicJson)
		then:
			def e = thrown(UnsupportedOperationException)
			e.message.
				contains("Currently only the AND combination rule logic is supported")
	}
}


// file creator
/*
pacts.entrySet().each {
	new File("target/${it.key.replace("groovy", "json")}").text = JsonOutput.toJson(it.value.toMap(PactSpecVersion.V3))
}
 */
