package io.codearte.accurest.wiremock

import io.codearte.accurest.file.Contract
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.skyscreamer.jsonassert.JSONAssert
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
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
						method('PUT')
						url \$(client(~/\\/[0-9]{2}/), server('/12'))
					}
					response {
						status 200
					}
				}
""")
		when:
			String json = converter.convertContent("Test", new Contract(file.toPath(), false, 0, null))
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
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
					}
					response {
						status 200
						fixedDelayMilliseconds 1000
					}
			}
""")
		when:
			String json = converter.convertContent("test", new Contract(file.toPath(), false, 0, null))
		then:
			JSONAssert.assertEquals('''
{"request":{},"response":{"status":200,"fixedDelayMilliseconds":1000}}
''', json, false)
	}

	def "should convert DSL file with a nested list to WireMock JSON"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl2.groovy")
			file.write("""
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
						method 'PUT'
						url '/api/12'
						headers {
							header 'Content-Type': 'application/vnd.com.ofg.twitter-places-analyzer.v1+json'

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
			String json = converter.convertContent("Test", new Contract(file.toPath(), false, 0, null))
		then:
		JSONAssert.assertEquals('''
{
  "request" : {
	"url" : "/api/12",
	"method" : "PUT",
	"bodyPatterns" : [ {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.country == 'United States')]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.name == 'Washington')]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box[?(@.type == 'Polygon')]"
	}, {
	  "matchesJsonPath" : "$[*][?(@.id_str == '492967299297845248')]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.country_code == 'US')]"
	}, {
	  "matchesJsonPath" : "$[*][?(@.id == 492967299297845248)]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.id == '01fbe706f872cb32')]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.url == 'http://api.twitter.com/1/geo/id/01fbe706f872cb32.json')]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"
	}, {
	  "matchesJsonPath" : "$[*][?(@.text == 'Gonna see you at Warsaw')]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.place_type == 'city')]"
	}, {
	  "matchesJsonPath" : "$[*][?(@.created_at == 'Sat Jul 26 09:38:57 +0000 2014')]"
	}, {
	  "matchesJsonPath" : "$[*].place[?(@.full_name == 'Washington, DC')]"
	}, {
	  "matchesJsonPath" : "$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"
	} ],
	"headers" : {
	  "Content-Type" : {
		"equalTo" : "application/vnd.com.ofg.twitter-places-analyzer.v1+json"
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
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
                method 'GET'
                urlPath '/foos'
            }
            response {
                status 200
                body([[id: value(
                        client('123'),
                        server(regex('[0-9]+'))
                )], [id: value(
                        client('567'),
                        server(regex('[0-9]+'))
                )]])
                headers {
									header 'Content-Type': 'application/json'
								}
            }
			}
""")
		when:
			String json = converter.convertContent("test", new Contract(file.toPath(), false, 0, null))
		then:
			JSONAssert.assertEquals('''
{"request":{"urlPath":"/foos","method":"GET"},"response":{"body":"[{\\"id\\":\\"123\\"},{\\"id\\":\\"567\\"}]"}}
''', json, false)
	}
	
	def 'should convert dsl to wiremock to show it in the docs'() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			File file = tmpFolder.newFile("dsl_from_docs.groovy")
			file.write('''
			io.codearte.accurest.dsl.GroovyDsl.make {
				priority 1
				request {
					method 'POST'
					url '/users/password'
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							email: $(stub(optional(regex(email()))), test('abc@abc.com')),
							callback_url: $(stub(regex(hostname())), test('http://partners.com'))
					)
				}
				response {
					status 404
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							code: value(stub("123123"), test(optional("123123"))),
							message: "User not found by email == [${value(test(regex(email())), stub('not.existing@user.com'))}]"
					)
				}
			}
	''')
		when:
		String json = converter.convertContent("Test", new Contract(file.toPath(), false, 0, null))
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
