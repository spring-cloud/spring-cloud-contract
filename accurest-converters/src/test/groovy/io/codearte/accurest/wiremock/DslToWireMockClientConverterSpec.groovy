package io.codearte.accurest.wiremock

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Issue
import spock.lang.Specification

class DslToWireMockClientConverterSpec extends Specification {

	def "should convert DSL file to WireMock JSON"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			String dslBody = """
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
						method('PUT')
						url \$(client(~/\\/[0-9]{2}/), server('/12'))
					}
					response {
						status 200
					}
				}
"""
		when:
			String json = converter.convertContent(dslBody)
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
			String dslBody = """
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
						method "GET"
						url "/test"
					}
					response {
						status 200
						fixedDelayMilliseconds 1000
					}
			}
"""
		when:
			String json = converter.convertContent(dslBody)
		then:
			JSONAssert.assertEquals('''
{"request":{"method":"GET","url":"/test"},"response":{"status":200, "fixedDelayMilliseconds":1000}}
''', json, false)
	}

	def "should convert DSL file with a nested list to WireMock JSON"() {
		given:
			def converter = new DslToWireMockClientConverter()
		and:
			String dslBody = """
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
"""
		when:
			String json = converter.convertContent(dslBody)
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
}
