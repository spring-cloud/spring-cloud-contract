/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.minidev.json.JSONArray
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.MatchingType
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import java.util.regex.Pattern

class JsonToJsonPathsConverterSpec extends Specification {

	def 'should convert a json with list as root to a map of path to value'() {
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").field("['json']").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").field("['anothervalue']").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").field("['anothernested']").field("['name']").isEqualTo("name3")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*].anothernested[?(@.name == 'name3')]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		where:
			json << [
					'''
						[ {
								"some" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}, {"anothernested": { "name": "name3"} }
										]
									}
								}
							},
							{
								"someother" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}
										]
									}
								}
							}
						]
	''',
		'''
							[{
								"someother" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}
										]
									}
								}
							},
						 {
								"some" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											 {"name": "name2"}, {"anothernested": { "name": "name3"} }, { "name" :"name1"}
										]
									}
								}
							}
						]''']
		}

	def 'should convert a json with a map as root to a map of path to value'() {
		given:
			String json = '''
					 {
							"some" : {
								"nested" : {
									"json" : "with value",
									"anothervalue": 4,
									"withlist" : [
										{ "name" :"name1"} , {"name": "name2"}
									]
								}
							}
						}
'''
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method() == """.field("['some']").field("['nested']").field("['json']").isEqualTo("with value")""" &&
			it.jsonPath() == '''$.some.nested[?(@.json == 'with value')]'''
		}
		pathAndValues.find {
			it.method() == """.field("['some']").field("['nested']").field("['anothervalue']").isEqualTo(4)""" &&
			it.jsonPath() == '''$.some.nested[?(@.anothervalue == 4)]'''
		}
		pathAndValues.find {
			it.method() == """.field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name1")""" &&
			it.jsonPath() == '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
		}
		pathAndValues.find {
			it.method() == """.field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name2")""" &&
			it.jsonPath() == '''$.some.nested.withlist[*][?(@.name == 'name2')]'''
		}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}

	def 'should convert a json with a list'() {
		given:
			String json = '''
					 {
							"items" : ["HOP"]
					}
'''
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("['items']").arrayField().isEqualTo("HOP").value()""" &&
				it.jsonPath() == '''$.items[?(@ == 'HOP')]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}

	def 'should convert a json with null and boolean values'() {
		given:
			String json = '''
					 {
							"property1" : null,
							"property2" : true
					}
'''
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("['property1']").isNull()""" &&
				it.jsonPath() == '''$[?(@.property1 == null)]'''
			}
			pathAndValues.find {
				it.method() == """.field("['property2']").isEqualTo(true)""" &&
				it.jsonPath() == '''$[?(@.property2 == true)]'''
			}
	}

	def "should convert numbers map"() {
		given:
			String json = ''' {
                     "extensions": {"7":28.00,"14":41.00,"30":60.00}
                     }
 '''
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("['extensions']").field("['7']").isEqualTo(28.00)""" &&
				it.jsonPath() == '''$.extensions[?(@.7 == 28.00)]'''
			}
			pathAndValues.find {
				it.method() == """.field("['extensions']").field("['14']").isEqualTo(41.00)""" &&
				it.jsonPath() == '''$.extensions[?(@.14 == 41.00)]'''
			}
			pathAndValues.find {
				it.method() == """.field("['extensions']").field("['30']").isEqualTo(60.00)""" &&
				it.jsonPath() == '''$.extensions[?(@.30 == 60.00)]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
	}

	def 'should convert a json with a list of errors'() {
		given:
			String json = '''
					 {
							"errors" : [
								{ "property" : "email", "message" : "inconsistent value" },
								{ "property" : "email", "message" : "inconsistent value2" }
							]
					}
'''
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("['errors']").contains("['property']").isEqualTo("email")""" &&
				it.jsonPath() == '''$.errors[*][?(@.property == 'email')]'''
			}
			pathAndValues.find {
				it.method() == """.array("['errors']").contains("['message']").isEqualTo("inconsistent value")""" &&
				it.jsonPath() == '''$.errors[*][?(@.message == 'inconsistent value')]'''
			}
			pathAndValues.find {
				it.method() == """.array("['errors']").contains("['message']").isEqualTo("inconsistent value2")""" &&
				it.jsonPath() == '''$.errors[*][?(@.message == 'inconsistent value2')]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}

	def 'should convert a map json with a regex pattern'() {
		given:
			List json = [
					[some:
							 [nested: [
									 json: "with value",
									 anothervalue: 4,
									 withlist:
											 [
													 [name: "name2"],
													 [name: "name1"],
													 [anothernested:
															  [name: Pattern.compile("[a-zA-Z]+")]
													 ],
													 [age: "123456789"]
											 ]
							 ]
							 ]
					],
					[someother:
							 [nested: [
									 json: "with value",
									 anothervalue: 4,
									 withlist:
											 [
													 [name: "name2"],
													 [name: "name1"]
											 ]
							 ]
							 ]
					]
			]
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").field("['json']").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").field("['anothervalue']").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").contains("['name']").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("['some']").field("['nested']").array("['withlist']").field("['anothernested']").field("['name']").matches("[a-zA-Z]+")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*].anothernested[?(@.name =~ /[a-zA-Z]+/)]'''
			}
		when:
			json.some.nested.withlist[0][2].anothernested.name = "Kowalski"
		then:
			assertThatJsonPathsInMapAreValid(JsonOutput.prettyPrint(JsonOutput.toJson(json)), pathAndValues)
		}
	

	def "should generate assertions for simple response body"() {
		given:
		String json =  """{
		"property1": "a",
		"property2": "b"
	}"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.field("['property2']").isEqualTo("b")""" &&
			it.jsonPath() == """\$[?(@.property2 == 'b')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	def "should generate assertions for null and boolean values"() {
		given:
		String json =  """{
		"property1": "true",
		"property2": null,
		"property3": false
	}"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("['property1']").isEqualTo("true")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'true')]"""
		}
		pathAndValues.find {
			it.method()== """.field("['property2']").isNull()""" &&
			it.jsonPath() == """\$[?(@.property2 == null)]"""
		}
		pathAndValues.find {
			it.method()== """.field("['property3']").isEqualTo(false)""" &&
			it.jsonPath() == """\$[?(@.property3 == false)]"""
		}
		and:
		pathAndValues.size() == 3
	}

	def "should generate assertions for simple response body constructed from map with a list"() {
		given:
		Map json =  [
				property1: 'a',
				property2: [
						[a: 'sth'],
						[b: 'sthElse']
				]
		]
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.field("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property2']").contains("['a']").isEqualTo("sth")""" &&
			it.jsonPath() == """\$.property2[*][?(@.a == 'sth')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property2']").contains("['b']").isEqualTo("sthElse")""" &&
			it.jsonPath() == """\$.property2[*][?(@.b == 'sthElse')]"""
		}
		and:
		pathAndValues.size() == 3
	}

	@RestoreSystemProperties
	def "should generate assertions for simple response body constructed from map with a list with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
		Map json =  [
				property1: 'a',
				property2: [
						[a: 'sth'],
						[b: 'sthElse']
				]
		]
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.field("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property2']").contains("['a']").isEqualTo("sth")""" &&
			it.jsonPath() == """\$.property2[*][?(@.a == 'sth')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property2']").hasSize(2)""" &&
			it.jsonPath() == """\$.property2[*]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property2']").contains("['b']").isEqualTo("sthElse")""" &&
			it.jsonPath() == """\$.property2[*][?(@.b == 'sthElse')]"""
		}
		and:
		pathAndValues.size() == 4
	}

	def "should generate assertions for a response body containing map with integers as keys"() {
		given:
		Map json =  [
				property: [
						14: 0.0,
						7 : 0.0
				]
		]
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.field("['property']").field(7).isEqualTo(0.0)""" &&
			it.jsonPath() == """\$.property[?(@.7 == 0.0)]"""
		}
		pathAndValues.find {
			it.method()== """.field("['property']").field(14).isEqualTo(0.0)""" &&
			it.jsonPath() == """\$.property[?(@.14 == 0.0)]"""
		}
		and:
		pathAndValues.size() == 2
	}

	def "should generate assertions for array in response body"() {
		given:
		String json =  """[
	{
		"property1": "a"
	},
	{
		"property2": "b"
	}]"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().contains("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[*][?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array().contains("['property2']").isEqualTo("b")""" &&
			it.jsonPath() == """\$[*][?(@.property2 == 'b')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	@RestoreSystemProperties
	def "should generate assertions for array in response body with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
		String json =  """[
	{
		"property1": "a"
	},
	{
		"property2": "b"
	}]"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().contains("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[*][?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array().contains("['property2']").isEqualTo("b")""" &&
			it.jsonPath() == """\$[*][?(@.property2 == 'b')]"""
		}
		pathAndValues.find {
			it.method()== """.hasSize(2)""" &&
			it.jsonPath() == """\$"""
		}
		and:
		pathAndValues.size() == 3
	}

	def "should generate assertions for array inside response body element"() {
		given:
		String json =  """{
	"property1": [
	{ "property2": "test1"},
	{ "property3": "test2"}
	]
}"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array("['property1']").contains("['property2']").isEqualTo("test1")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property2 == 'test1')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property1']").contains("['property3']").isEqualTo("test2")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property3 == 'test2')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	@RestoreSystemProperties
	def "should generate assertions for array inside response body element with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
		String json =  """{
	"property1": [
	{ "property2": "test1"},
	{ "property3": "test2"}
	]
}"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array("['property1']").contains("['property2']").isEqualTo("test1")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property2 == 'test1')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property1']").contains("['property3']").isEqualTo("test2")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property3 == 'test2')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['property1']").hasSize(2)""" &&
			it.jsonPath() == """\$.property1[*]"""
		}
		and:
		pathAndValues.size() == 3
	}

	def "should generate assertions for nested objects in response body"() {
		given:
		String json =  """{
		"property1": "a",
		"property2": {"property3": "b"}
	}"""
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("['property2']").field("['property3']").isEqualTo("b")""" &&
			it.jsonPath() == """\$.property2[?(@.property3 == 'b')]"""
		}
		pathAndValues.find {
			it.method()== """.field("['property1']").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	def "should generate regex assertions for map objects in response body"() {
		given:
			Map json =  [
					property1: "a",
					property2: Pattern.compile('[0-9]{3}')
			]
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method()== """.field("['property2']").matches("[0-9]{3}")""" &&
				it.jsonPath() == """\$[?(@.property2 =~ /[0-9]{3}/)]"""
			}
			pathAndValues.find {
				it.method()== """.field("['property1']").isEqualTo("a")""" &&
				it.jsonPath() == """\$[?(@.property1 == 'a')]"""
			}
		and:
			pathAndValues.size() == 2
	}

	def "should generate escaped regex assertions for string objects in response body"() {
		given:
			Map json =  [
					property2: Pattern.compile('\\d+')
			]
		when:
			JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method()== """.field("['property2']").matches("\\\\d+")""" &&
				it.jsonPath() == """\$[?(@.property2 =~ /\\d+/)]"""
			}
		and:
			pathAndValues.size() == 1
	}

	def "should work with more complex stuff and jsonpaths"() {
		given:
		Map json =  [
				errors: [
						[property: "bank_account_number",
						 message: "incorrect_format"]
				]
		]
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.array("['errors']").contains("['property']").isEqualTo("bank_account_number")""" &&
			it.jsonPath() == """\$.errors[*][?(@.property == 'bank_account_number')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['errors']").contains("['message']").isEqualTo("incorrect_format")""" &&
			it.jsonPath() == """\$.errors[*][?(@.message == 'incorrect_format')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	@RestoreSystemProperties
	def "should work with more complex stuff and jsonpaths with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
		Map json =  [
				errors: [
						[property: "bank_account_number",
						 message: "incorrect_format"]
				]
		]
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.array("['errors']").contains("['property']").isEqualTo("bank_account_number")""" &&
			it.jsonPath() == """\$.errors[*][?(@.property == 'bank_account_number')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['errors']").contains("['message']").isEqualTo("incorrect_format")""" &&
			it.jsonPath() == """\$.errors[*][?(@.message == 'incorrect_format')]"""
		}
		pathAndValues.find {
			it.method()== """.array("['errors']").hasSize(1)""" &&
			it.jsonPath() == """\$.errors[*]"""
		}
		and:
		pathAndValues.size() == 3
	}

	def "should manage to parse a double array"() {
		given:
		String json = '''
						[{
							"place":
							{
								"bounding_box":
								{
									"coordinates":
										[[
											[-77.119759,38.995548],
											[-76.909393,38.791645]
										]]
								}
							}
						}]
					'''
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(38.995548)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(-77.119759)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(-76.909393)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(38.791645)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"""
		}
		and:
			pathAndValues.size() == 4
		and:
			pathAndValues.each {
				JsonAssertion.assertThat(json).matchesJsonPath(it.jsonPath())
			}
	}

	@RestoreSystemProperties
	def "should manage to parse a double array with array size check"() {
		given:
		System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
		String json = '''
						[{
							"place":
							{
								"bounding_box":
								{
									"coordinates":
										[[
											[-77.119759,38.995548],
											[-76.909393,38.791645]
										]]
								}
							}
						}]
					'''
		when:
		JsonPaths pathAndValues = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(38.995548)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(-77.119759)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(-76.909393)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().arrayField().isEqualTo(38.791645)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"""
		}
		pathAndValues.find {
			it.method()== """.hasSize(1)""" &&
			it.jsonPath() == """\$"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").array().hasSize(2)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("['place']").field("['bounding_box']").array("['coordinates']").hasSize(1)""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*]"""
		}
		and:
			pathAndValues.size() == 7
		and:
			pathAndValues.each {
				JsonAssertion.assertThat(json).matchesJsonPath(it.jsonPath())
			}
	}

	def "should convert a json path with regex to a regex checking json path"() {
		given:
			String jsonPath = '$.a.b.c.d'
			String regexPattern = ".*"
		expect:
			'$.a.b.c[?(@.d =~ /(.*)/)]' == JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.REGEX, jsonPath, regexPattern))
	}

	def "should convert a json path with regex to a regex checking json path that has a / in it"() {
		given:
			String jsonPath = '$.a.b.c.d'
			String regexPattern = "/.*/"
		expect:
			'$.a.b.c[?(@.d =~ /(\\\\/.*\\\\/)/)]' == JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.REGEX, jsonPath, regexPattern))
	}

	def "should convert a json path with value to a equality checking json path without quotes for numbers"() {
		given:
			String jsonPath = '$.a.b.c.d'
		and:
			def body = [ a: [ b: [ c: [ d: 1234 ] ] ] ]
		expect:
			'$.a.b.c[?(@.d == 1234)]' == JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.EQUALITY, jsonPath, null), body)
	}

	def "should convert a json path with value to a equality checking json path with quotes for strings"() {
		given:
			String jsonPath = '$.a.b.c.d'
		and:
			def body = [ a: [ b: [ c: [ d: "foo" ] ] ] ]
		expect:
			'$.a.b.c[?(@.d == \'foo\')]' == JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.EQUALITY, jsonPath, null), body)
	}

	def "should return the path if no value is provided"() {
		given:
			String jsonPath = '$.a.b.c.d'
		expect:
			'$.a.b.c.d' == JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.REGEX, jsonPath, null))
	}

	def "should throw an exception when null body is passed to check for equality"() {
		given:
			String jsonPath = '$.a.b.c.d'
		and:
			def body = null
		when:
			JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.EQUALITY, jsonPath, null), body)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Body")
	}

	def "should throw an exception when nonexisting jsonpath is passed to check for equality"() {
		given:
			String jsonPath = '$.a.b.c.d'
		and:
			def body = [ foo: "bar" ]
		when:
			JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher(MatchingType.EQUALITY, jsonPath, null), body)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("not found")
	}

	private BodyMatcher matcher(final MatchingType matchingType, final String jsonPath, final Object value) {
		return new BodyMatcher() {
			@Override
			MatchingType matchingType() {
				return matchingType
			}

			@Override
			String path() {
				return jsonPath
			}

			@Override
			Object value() {
				return value
			}

			@Override
			Integer minTypeOccurrence() {
				return null
			}

			@Override
			Integer maxTypeOccurrence() {
				return null
			}
		}
	}

	private void assertThatJsonPathsInMapAreValid(String json, JsonPaths pathAndValues) {
		DocumentContext parsedJson = JsonPath.using(Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build()).parse(json);
		pathAndValues.each {
			assert !parsedJson.read(it.jsonPath(), JSONArray).empty
		}
	}

}
