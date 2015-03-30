package io.codearte.accurest.wiremock

import groovy.json.JsonSlurper
import spock.lang.Specification

class DslToWiremockClientConverterSpec extends Specification {

	def "should convert DSL file to Wiremock JSON"() {
		given:
			def converter = new DslToWiremockClientConverter()
		and:
			String dslBody = """
                io.coderate.accurest.dsl.GroovyDsl.make {
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
			new JsonSlurper().parseText(json) == new JsonSlurper().parseText("""
{"request":{"method":"PUT","urlPattern":"/[0-9]{2}"},"response":{"status":200}}""")
	}


	def "should convert DSL file with a nested list to Wiremock JSON"() {
		given:
			def converter = new DslToWiremockClientConverter()
		and:
			String dslBody = """
                io.coderate.accurest.dsl.GroovyDsl.make {
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
			new JsonSlurper().parseText(json) == new JsonSlurper().parseText("""{
				"request":{
					"method":"PUT",
					"url":"/api/12",
					"bodyPatterns": [
						{ "equalTo": "[{\\"created_at\\":\\"Sat Jul 26 09:38:57 +0000 2014\\",\\"id\\":492967299297845248,\\"id_str\\":\\"492967299297845248\\",\\"place\\":{\\"attributes\\":{},\\"bounding_box\\":{\\"coordinates\\":[[[-77.119759,38.791645],[-76.909393,38.791645],[-76.909393,38.995548],[-77.119759,38.995548]]],\\"type\\":\\"Polygon\\"},\\"country\\":\\"United States\\",\\"country_code\\":\\"US\\",\\"full_name\\":\\"Washington, DC\\",\\"id\\":\\"01fbe706f872cb32\\",\\"name\\":\\"Washington\\",\\"place_type\\":\\"city\\",\\"url\\":\\"http://api.twitter.com/1/geo/id/01fbe706f872cb32.json\\"},\\"text\\":\\"Gonna see you at Warsaw\\"}]" }
					],
					"headers": {
						"Content-Type": {
							"equalTo": "application/vnd.com.ofg.twitter-places-analyzer.v1+json"
						}
					}
				},
				"response":{
					"status":200}
				}
""")
	}
}
