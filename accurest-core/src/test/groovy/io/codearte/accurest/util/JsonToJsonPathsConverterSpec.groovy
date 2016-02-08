package io.codearte.accurest.util

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.minidev.json.JSONArray
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class JsonToJsonPathsConverterSpec extends Specification {

	@Unroll
	def 'should convert a json with list as root to a map of path to value'() {
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("json").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").field("anothernested").field("name").isEqualTo("name3")""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method() == """.field("some").field("nested").field("json").isEqualTo("with value")""" &&
			it.jsonPath() == '''$.some.nested[?(@.json == 'with value')]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
			it.jsonPath() == '''$.some.nested[?(@.anothervalue == 4)]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
			it.jsonPath() == '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("items").contains("HOP").value()""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("property1").isNull()""" &&
				it.jsonPath() == '''$[?(@.property1 == null)]'''
			}
			pathAndValues.find {
				it.method() == """.field("property2").isEqualTo(true)""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("extensions").field("7").isEqualTo(28)""" &&
				it.jsonPath() == '''$.extensions[?(@.7 == 28)]'''
			}
			pathAndValues.find {
				it.method() == """.field("extensions").field("14").isEqualTo(41)""" &&
				it.jsonPath() == '''$.extensions[?(@.14 == 41)]'''
			}
			pathAndValues.find {
				it.method() == """.field("extensions").field("30").isEqualTo(60)""" &&
				it.jsonPath() == '''$.extensions[?(@.30 == 60)]'''
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("errors").contains("property").isEqualTo("email")""" &&
				it.jsonPath() == '''$.errors[*][?(@.property == 'email')]'''
			}
			pathAndValues.find {
				it.method() == """.array("errors").contains("message").isEqualTo("inconsistent value")""" &&
				it.jsonPath() == '''$.errors[*][?(@.message == 'inconsistent value')]'''
			}
			pathAndValues.find {
				it.method() == """.array("errors").contains("message").isEqualTo("inconsistent value2")""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("json").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").field("anothernested").field("name").matches("[a-zA-Z]+")""" &&
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("property1").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.field("property2").isEqualTo("b")""" &&
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("property1").isEqualTo("true")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'true')]"""
		}
		pathAndValues.find {
			it.method()== """.field("property2").isNull()""" &&
			it.jsonPath() == """\$[?(@.property2 == null)]"""
		}
		pathAndValues.find {
			it.method()== """.field("property3").isEqualTo(false)""" &&
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.field("property1").isEqualTo("a")""" &&
			it.jsonPath() == """\$[?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array("property2").contains("a").isEqualTo("sth")""" &&
			it.jsonPath() == """\$.property2[*][?(@.a == 'sth')]"""
		}
		pathAndValues.find {
			it.method()== """.array("property2").contains("b").isEqualTo("sthElse")""" &&
			it.jsonPath() == """\$.property2[*][?(@.b == 'sthElse')]"""
		}
		and:
		pathAndValues.size() == 3
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.field("property").field(7).isEqualTo(0.0)""" &&
			it.jsonPath() == """\$.property[?(@.7 == 0.0)]"""
		}
		pathAndValues.find {
			it.method()== """.field("property").field(14).isEqualTo(0.0)""" &&
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().contains("property1").isEqualTo("a")""" &&
			it.jsonPath() == """\$[*][?(@.property1 == 'a')]"""
		}
		pathAndValues.find {
			it.method()== """.array().contains("property2").isEqualTo("b")""" &&
			it.jsonPath() == """\$[*][?(@.property2 == 'b')]"""
		}
		and:
		pathAndValues.size() == 2
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array("property1").contains("property2").isEqualTo("test1")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property2 == 'test1')]"""
		}
		pathAndValues.find {
			it.method()== """.array("property1").contains("property3").isEqualTo("test2")""" &&
			it.jsonPath() == """\$.property1[*][?(@.property3 == 'test2')]"""
		}
		and:
		pathAndValues.size() == 2
	}

	def "should generate assertions for nested objects in response body"() {
		given:
		String json =  """{
		"property1": "a",
		"property2": {"property3": "b"}
	}"""
		when:
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.field("property2").field("property3").isEqualTo("b")""" &&
			it.jsonPath() == """\$.property2[?(@.property3 == 'b')]"""
		}
		pathAndValues.find {
			it.method()== """.field("property1").isEqualTo("a")""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method()== """.field("property2").matches("[0-9]{3}")""" &&
				it.jsonPath() == """\$[?(@.property2 =~ /[0-9]{3}/)]"""
			}
			pathAndValues.find {
				it.method()== """.field("property1").isEqualTo("a")""" &&
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method()== """.field("property2").matches("\\d+")""" &&
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.method()== """.array("errors").contains("property").isEqualTo("bank_account_number")""" &&
			it.jsonPath() == """\$.errors[*][?(@.property == 'bank_account_number')]"""
		}
		pathAndValues.find {
			it.method()== """.array("errors").contains("message").isEqualTo("incorrect_format")""" &&
			it.jsonPath() == """\$.errors[*][?(@.message == 'incorrect_format')]"""
		}
		and:
		pathAndValues.size() == 2
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
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method()== """.array().field("place").field("bounding_box").array("coordinates").array().contains(38.995548).value()""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("place").field("bounding_box").array("coordinates").array().contains(-77.119759).value()""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("place").field("bounding_box").array("coordinates").array().contains(-76.909393).value()""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"""
		}
		pathAndValues.find {
			it.method()== """.array().field("place").field("bounding_box").array("coordinates").array().contains(38.791645).value()""" &&
			it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"""
		}
		and:
		pathAndValues.size() == 4
	}

	private void assertThatJsonPathsInMapAreValid(String json, JsonPaths pathAndValues) {
		DocumentContext parsedJson = JsonPath.using(Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build()).parse(json);
		pathAndValues.each {
			assert !parsedJson.read(it.jsonPath(), JSONArray).empty
		}
	}

}
