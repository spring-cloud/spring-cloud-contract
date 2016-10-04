/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.wiremock

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import spock.lang.Issue
import spock.lang.Specification

class DslToWireMockClientConverterSpec extends Specification {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	def "should convert DSL file to WireMock JSON"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl1.groovy")
			file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method('PUT')
						url \$(consumer(~/\\/[0-9]{2}/), producer('/12'))
					}
					response {
						status 200
					}
				}
""")
		when:
			String json = converter.convertContent("Test", new ContractMetadata(file.toPath(), false, 0, null))
		then:
		JSONAssert.assertEquals('''
{"request":{"method":"PUT","urlPattern":"/[0-9]{2}"},"response":{"status":200}}
''', json, false)
	}

	@Issue("196")
	def "should creation of delayed stub responses be possible"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl-delay.groovy")
			file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'GET'
						url '/foo'
					}
					response {
						status 200
						fixedDelayMilliseconds 1000
					}
			}
""")
		when:
			String json = converter.convertContent("test", new ContractMetadata(file.toPath(), false, 0, null))
		then:
			JSONAssert.assertEquals('''
{"request":{
	"url" : "/foo",
	"method" : "GET"},
"response":{
"status":200,"fixedDelayMilliseconds":1000
}}
''', json, false)
	}

	def "should convert DSL file with a nested list to WireMock JSON"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl2.groovy")
			file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
						method 'PUT'
						url '/api/12'
						headers {
							header 'Content-Type': 'application/vnd.org.springframework.cloud.contract.verifier.twitter-places-analyzer.v1+json'

						}
						body '''
					[{
						"created_at": "Sat Jul 26 09:38:57 +0000 2014",
						"id": 492967299297845248,
						"id_str": "492967299297845248",
						"text": "Gonna see you at Warsaw",
						"place":
						{
							"attributes":{},
							"bounding_box":
							{
								"coordinates":
									[[
										[-77.119759,38.791645],
										[-76.909393,38.791645],
										[-76.909393,38.995548],
										[-77.119759,38.995548]
									]],
								"type":"Polygon"
							},
							"country":"United States",
							"country_code":"US",
							"full_name":"Washington, DC",
							"id":"01fbe706f872cb32",
							"name":"Washington",
							"place_type":"city",
							"url": "http://api.twitter.com/1/geo/id/01fbe706f872cb32.json"
						}
					}]
				'''
					}
					response {
						status 200
					}
				}
""")
		when:
			String json = converter.convertContent("Test", new ContractMetadata(file.toPath(), false, 0, null))
		then:
		JSONAssert.assertEquals('''
{
  "request" : {
	"url" : "/api/12",
    "method" : "PUT",
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"
    }, {
      "matchesJsonPath" : "$[*][?(@.text == 'Gonna see you at Warsaw')]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.place_type == 'city')]"
    }, {
      "matchesJsonPath" : "$[*][?(@.id == 492967299297845248)]"
    }, {
      "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.country == 'United States')]"
    }, {
      "matchesJsonPath" : "$[*][?(@.id_str == '492967299297845248')]"
    }, {
      "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.name == 'Washington')]"
    }, {
      "matchesJsonPath" : "$[*].place.bounding_box[?(@.type == 'Polygon')]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.url == 'http://api.twitter.com/1/geo/id/01fbe706f872cb32.json')]"
    }, {
      "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.country_code == 'US')]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.full_name == 'Washington, DC')]"
    }, {
      "matchesJsonPath" : "$[*][?(@.created_at == 'Sat Jul 26 09:38:57 +0000 2014')]"
    }, {
      "matchesJsonPath" : "$[*].place[?(@.id == '01fbe706f872cb32')]"
    } ],
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "application/vnd.org.springframework.cloud.contract.verifier.twitter-places-analyzer.v1+json"
	  }
	}
  },
  "response" : {
	"status" : 200
  }
}
''', json, false)
	}


	@Issue("262")
	def "should create stub with map inside list"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl-mapinlist.groovy")
			file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
                method 'GET'
                urlPath '/foos'
            }
            response {
                status 200
                body([[id: value(
                        consumer('123'),
                        producer(regex('[0-9]+'))
                )], [id: value(
                        consumer('567'),
                        producer(regex('[0-9]+'))
                )]])
                headers {
									header 'Content-Type': 'application/json'
								}
            }
			}
""")
		when:
			String json = converter.convertContent("test", new ContractMetadata(file.toPath(), false, 0, null))
		then:
			JSONAssert.assertEquals('''
{"request":{"urlPath":"/foos","method":"GET"},"response":{"body":"[{\\"id\\":\\"123\\"},{\\"id\\":\\"567\\"}]"}}
''', json, false)
	}


	@Issue("94")
	def "should create stub when response has only one side of the dynamic value"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl-dynamic.groovy")
			file.write("""
				org.springframework.cloud.contract.spec.Contract.make {
					request {
                method 'GET'
                urlPath '/foos'
            }
            response {
				status 200
				body(
					digit: \$(producer(regex('[0-9]{1}'))),
					id: \$(producer(regex(number())))
				)
			}
			}
""")
		when:
			String json = converter.convertContent("test", new ContractMetadata(file.toPath(), false, 0, null))
			StubMapping.buildFrom(json)
		then:
			noExceptionThrown()
		and:
			!json.contains('cursor')
	}

	def 'should convert dsl to wiremock to show it in the docs'() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl_from_docs.groovy")
			file.write('''
			org.springframework.cloud.contract.spec.Contract.make {
				priority 1
				request {
					method 'POST'
					url '/users/password'
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							email: $(consumer(optional(regex(email()))), producer('abc@abc.com')),
							callback_url: $(consumer(regex(hostname())), producer('http://partners.com'))
					)
				}
				response {
					status 404
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							code: value(consumer("123123"), producer(optional("123123"))),
							message: "User not found by email == [${value(producer(regex(email())), consumer('not.existing@user.com'))}]"
					)
				}
			}
	''')
		when:
		String json = converter.convertContent("Test", new ContractMetadata(file.toPath(), false, 0, null))
		then:
		JSONAssert.assertEquals( // tag::wiremock[]
'''
{
  "request" : {
    "url" : "/users/password",
    "method" : "POST",
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$[?(@.email =~ /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4})?/)]"
    }, {
      "matchesJsonPath" : "$[?(@.callback_url =~ /((http[s]?|ftp):\\\\/)\\\\/?([^:\\\\/\\\\s]+)(:[0-9]{1,5})?/)]"
    } ],
    "headers" : {
      "Content-Type" : {
        "equalTo" : "application/json"
      }
    }
  },
  "response" : {
    "status" : 404,
    "body" : "{\\"code\\":\\"123123\\",\\"message\\":\\"User not found by email == [not.existing@user.com]\\"}",
    "headers" : {
      "Content-Type" : "application/json"
    }
  },
  "priority" : 1
}
'''
// end::wiremock[]
				, json, false)
	}

}
