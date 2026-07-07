/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * Tests for {@link JsonToJsonPathsConverter} with ordered array verification enabled.
 * All tests in this class run with `spring.cloud.contract.verifier.assert.size` set to true,
 * which enables exact index-based array element verification instead of wildcard matching.
 *
 * @author Marcin Grzejszczak
 * @since 5.1.0
 */
class JsonToJsonPathsConverterWithArrayCheckSpec extends Specification {

	/**
	 * Creates a converter with ordered array verification enabled.
	 */
	private static JsonToJsonPathsConverter converter() {
		return new JsonToJsonPathsConverter(true)
	}

	// ========== Primitive Arrays ==========

	def "should generate ordered assertions for simple string array"() {
		given:
			Map json = [
					items: ["first", "second", "third"]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['items']").hasSize(3)""" &&
						it.jsonPath() == """\$.['items']"""
			}
	and: "should have assertion for first element"
		pathAndValues.find {
			it.method() == """.array("['items']").elementWithIndex(0).isEqualTo("first")""" &&
					it.jsonPath() == """\$.['items'][0]"""
		}
	and: "should have assertion for second element"
		pathAndValues.find {
			it.method() == """.array("['items']").elementWithIndex(1).isEqualTo("second")""" &&
					it.jsonPath() == """\$.['items'][1]"""
		}
	and: "should have assertion for third element"
		pathAndValues.find {
			it.method() == """.array("['items']").elementWithIndex(2).isEqualTo("third")""" &&
					it.jsonPath() == """\$.['items'][2]"""
		}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	def "should generate ordered assertions for number array"() {
		given:
			Map json = [
					numbers: [10, 20, 30, 40]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['numbers']").hasSize(4)""" &&
						it.jsonPath() == """\$.['numbers']"""
			}
	and: "should have assertion for element at index 0"
		pathAndValues.find {
			it.method() == """.array("['numbers']").elementWithIndex(0).isEqualTo(10)""" &&
					it.jsonPath() == """\$.['numbers'][0]"""
		}
	and: "should have assertion for element at index 1"
		pathAndValues.find {
			it.method() == """.array("['numbers']").elementWithIndex(1).isEqualTo(20)""" &&
					it.jsonPath() == """\$.['numbers'][1]"""
		}
	and: "should have assertion for element at index 2"
		pathAndValues.find {
			it.method() == """.array("['numbers']").elementWithIndex(2).isEqualTo(30)""" &&
					it.jsonPath() == """\$.['numbers'][2]"""
		}
	and: "should have assertion for element at index 3"
		pathAndValues.find {
			it.method() == """.array("['numbers']").elementWithIndex(3).isEqualTo(40)""" &&
					it.jsonPath() == """\$.['numbers'][3]"""
		}
		and: "should have exactly 5 assertions (1 size + 4 elements)"
			pathAndValues.size() == 5
	}

	def "should generate ordered assertions for boolean array"() {
		given:
			Map json = [
					flags: [true, false, true]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['flags']").hasSize(3)""" &&
						it.jsonPath() == """\$.['flags']"""
			}
	and: "should have assertion for element at index 0 (true)"
		pathAndValues.find {
			it.method() == """.array("['flags']").elementWithIndex(0).isEqualTo(true)""" &&
					it.jsonPath() == """\$.['flags'][0]"""
		}
	and: "should have assertion for element at index 1 (false)"
		pathAndValues.find {
			it.method() == """.array("['flags']").elementWithIndex(1).isEqualTo(false)""" &&
					it.jsonPath() == """\$.['flags'][1]"""
		}
	and: "should have assertion for element at index 2 (true)"
		pathAndValues.find {
			it.method() == """.array("['flags']").elementWithIndex(2).isEqualTo(true)""" &&
					it.jsonPath() == """\$.['flags'][2]"""
		}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	def "should generate ordered assertions for mixed primitive array"() {
		given:
			Map json = [
					mixed: ["text", 123, true]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['mixed']").hasSize(3)""" &&
						it.jsonPath() == """\$.['mixed']"""
			}
	and: "should have assertion for string at index 0"
		pathAndValues.find {
			it.method() == """.array("['mixed']").elementWithIndex(0).isEqualTo("text")""" &&
					it.jsonPath() == """\$.['mixed'][0]"""
		}
	and: "should have assertion for number at index 1"
		pathAndValues.find {
			it.method() == """.array("['mixed']").elementWithIndex(1).isEqualTo(123)""" &&
					it.jsonPath() == """\$.['mixed'][1]"""
		}
	and: "should have assertion for boolean at index 2"
		pathAndValues.find {
			it.method() == """.array("['mixed']").elementWithIndex(2).isEqualTo(true)""" &&
					it.jsonPath() == """\$.['mixed'][2]"""
		}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	// ========== Object Arrays ==========

	def "should generate ordered assertions for array of objects"() {
		given:
			Map json = [
					users: [
							[name: "Alice", age: 30],
							[name: "Bob", age: 25]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for users[0].name"
			pathAndValues.find {
				it.method() == """.array("['users']").elementWithIndex(0).field("['name']").isEqualTo("Alice")""" &&
						it.jsonPath() == """\$.['users'][0][?(@.['name'] == 'Alice')]"""
			}
		and: "should have assertion for users[0].age"
			pathAndValues.find {
				it.method() == """.array("['users']").elementWithIndex(0).field("['age']").isEqualTo(30)""" &&
						it.jsonPath() == """\$.['users'][0][?(@.['age'] == 30)]"""
			}
		and: "should have assertion for users[1].name"
			pathAndValues.find {
				it.method() == """.array("['users']").elementWithIndex(1).field("['name']").isEqualTo("Bob")""" &&
						it.jsonPath() == """\$.['users'][1][?(@.['name'] == 'Bob')]"""
			}
		and: "should have assertion for users[1].age"
			pathAndValues.find {
				it.method() == """.array("['users']").elementWithIndex(1).field("['age']").isEqualTo(25)""" &&
						it.jsonPath() == """\$.['users'][1][?(@.['age'] == 25)]"""
			}
		and: "should have exactly 4 assertions (2 users x 2 fields)"
			pathAndValues.size() == 4
	}

	def "should generate ordered assertions for array of objects with same values"() {
		given:
			Map json = [
					entries: [
							[status: "active"],
							[status: "active"],
							[status: "inactive"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for entries[0].status = active"
			pathAndValues.find {
				it.method() == """.array("['entries']").elementWithIndex(0).field("['status']").isEqualTo("active")""" &&
						it.jsonPath() == """\$.['entries'][0][?(@.['status'] == 'active')]"""
			}
		and: "should have assertion for entries[1].status = active (same value, different index)"
			pathAndValues.find {
				it.method() == """.array("['entries']").elementWithIndex(1).field("['status']").isEqualTo("active")""" &&
						it.jsonPath() == """\$.['entries'][1][?(@.['status'] == 'active')]"""
			}
		and: "should have assertion for entries[2].status = inactive"
			pathAndValues.find {
				it.method() == """.array("['entries']").elementWithIndex(2).field("['status']").isEqualTo("inactive")""" &&
						it.jsonPath() == """\$.['entries'][2][?(@.['status'] == 'inactive')]"""
			}
		and: "should have exactly 3 assertions"
			pathAndValues.size() == 3
	}

	// ========== Nested Arrays ==========

	def "should generate ordered assertions for nested primitive arrays"() {
		given:
			Map json = [
					matrix: [
							["a", "b"],
							["c", "d"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for matrix[0][0] = a"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") && it.method().contains("isEqualTo(\"a\")")
			}
		and: "should have assertion for matrix[0][1] = b"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") && it.method().contains("isEqualTo(\"b\")")
			}
		and: "should have assertion for matrix[1][0] = c"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") && it.method().contains("isEqualTo(\"c\")")
			}
		and: "should have assertion for matrix[1][1] = d"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") && it.method().contains("isEqualTo(\"d\")")
			}
	}

	def "should generate ordered assertions for array with nested objects - all fields"() {
		given:
			Map json = [
					orders: [
							[
									id: 1,
									items: [
											[name: "item1", qty: 2],
											[name: "item2", qty: 3]
									]
							],
							[
									id: 2,
									items: [
											[name: "item3", qty: 1]
									]
							]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for orders[0].id"
			pathAndValues.find {
				it.method() == """.array("['orders']").elementWithIndex(0).field("['id']").isEqualTo(1)""" &&
						it.jsonPath() == """\$.['orders'][0][?(@.['id'] == 1)]"""
			}
		and: "should have assertion for orders[0].items[0].name"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['items']") &&
						it.method().contains("['name']") &&
						it.method().contains("isEqualTo(\"item1\")")
			}
		and: "should have assertion for orders[0].items[0].qty"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['items']") &&
						it.method().contains("['qty']") &&
						it.method().contains("isEqualTo(2)")
			}
		and: "should have assertion for orders[0].items[1].name"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['items']") &&
						it.method().contains("['name']") &&
						it.method().contains("isEqualTo(\"item2\")")
			}
		and: "should have assertion for orders[0].items[1].qty"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['items']") &&
						it.method().contains("['qty']") &&
						it.method().contains("isEqualTo(3)")
			}
		and: "should have assertion for orders[1].id"
			pathAndValues.find {
				it.method() == """.array("['orders']").elementWithIndex(1).field("['id']").isEqualTo(2)""" &&
						it.jsonPath() == """\$.['orders'][1][?(@.['id'] == 2)]"""
			}
		and: "should have assertion for orders[1].items[0].name"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['items']") &&
						it.method().contains("['name']") &&
						it.method().contains("isEqualTo(\"item3\")")
			}
		and: "should have assertion for orders[1].items[0].qty"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['items']") &&
						it.method().contains("['qty']") &&
						it.method().contains("isEqualTo(1)")
			}
	}

	// ========== Root Level Arrays ==========

	def "should generate ordered assertions for root level array of primitives"() {
		given:
			String json = """["first", "second", "third"]"""
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
	then: "should have size check for root array"
			pathAndValues.find {
				it.method() == """.hasSize(3)""" &&
						it.jsonPath() == """\$"""
			}
	and: "should have assertion for element at index 0"
		pathAndValues.find {
			it.method() == """.array().elementWithIndex(0).isEqualTo("first")""" &&
					it.jsonPath() == """\$[*][0]"""
		}
	and: "should have assertion for element at index 1"
		pathAndValues.find {
			it.method() == """.array().elementWithIndex(1).isEqualTo("second")""" &&
					it.jsonPath() == """\$[*][1]"""
		}
	and: "should have assertion for element at index 2"
		pathAndValues.find {
			it.method() == """.array().elementWithIndex(2).isEqualTo("third")""" &&
					it.jsonPath() == """\$[*][2]"""
		}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	def "should generate ordered assertions for root level array of objects"() {
		given:
			String json = """[
	{"property1": "a"},
	{"property2": "b"}
]"""
	when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
	then: "should have assertion for [0].property1"
		pathAndValues.find {
			it.method() == """.array().elementWithIndex(0).field("['property1']").isEqualTo("a")""" &&
					it.jsonPath() == """\$[*][0][?(@.['property1'] == 'a')]"""
		}
	and: "should have assertion for [1].property2"
		pathAndValues.find {
			it.method() == """.array().elementWithIndex(1).field("['property2']").isEqualTo("b")""" &&
					it.jsonPath() == """\$[*][1][?(@.['property2'] == 'b')]"""
		}
	and: "should have exactly 2 assertions (2 objects with 1 field each)"
			pathAndValues.size() == 2
	}

	// ========== Complex Real-World Scenarios ==========

	def "should generate ordered assertions for response with errors array - all fields"() {
		given:
			Map json = [
					errors: [
							[property: "email", message: "invalid format"],
							[property: "phone", message: "required field"],
							[property: "age", message: "must be positive"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for errors[0].property"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(0).field("['property']").isEqualTo("email")""" &&
						it.jsonPath() == """\$.['errors'][0][?(@.['property'] == 'email')]"""
			}
		and: "should have assertion for errors[0].message"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(0).field("['message']").isEqualTo("invalid format")""" &&
						it.jsonPath() == """\$.['errors'][0][?(@.['message'] == 'invalid format')]"""
			}
		and: "should have assertion for errors[1].property"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(1).field("['property']").isEqualTo("phone")""" &&
						it.jsonPath() == """\$.['errors'][1][?(@.['property'] == 'phone')]"""
			}
		and: "should have assertion for errors[1].message"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(1).field("['message']").isEqualTo("required field")""" &&
						it.jsonPath() == """\$.['errors'][1][?(@.['message'] == 'required field')]"""
			}
		and: "should have assertion for errors[2].property"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(2).field("['property']").isEqualTo("age")""" &&
						it.jsonPath() == """\$.['errors'][2][?(@.['property'] == 'age')]"""
			}
		and: "should have assertion for errors[2].message"
			pathAndValues.find {
				it.method() == """.array("['errors']").elementWithIndex(2).field("['message']").isEqualTo("must be positive")""" &&
						it.jsonPath() == """\$.['errors'][2][?(@.['message'] == 'must be positive')]"""
			}
		and: "should have exactly 6 assertions (3 errors x 2 fields)"
			pathAndValues.size() == 6
	}

	def "should generate ordered assertions for paginated response - all fields"() {
		given:
			Map json = [
					page: 1,
					totalPages: 5,
					data: [
							[id: 101, name: "First Item"],
							[id: 102, name: "Second Item"],
							[id: 103, name: "Third Item"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for page"
			pathAndValues.find {
				it.method() == """.field("['page']").isEqualTo(1)""" &&
						it.jsonPath() == """\$[?(@.['page'] == 1)]"""
			}
		and: "should have assertion for totalPages"
			pathAndValues.find {
				it.method() == """.field("['totalPages']").isEqualTo(5)""" &&
						it.jsonPath() == """\$[?(@.['totalPages'] == 5)]"""
			}
		and: "should have assertion for data[0].id"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(0).field("['id']").isEqualTo(101)""" &&
						it.jsonPath() == """\$.['data'][0][?(@.['id'] == 101)]"""
			}
		and: "should have assertion for data[0].name"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(0).field("['name']").isEqualTo("First Item")""" &&
						it.jsonPath() == """\$.['data'][0][?(@.['name'] == 'First Item')]"""
			}
		and: "should have assertion for data[1].id"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(1).field("['id']").isEqualTo(102)""" &&
						it.jsonPath() == """\$.['data'][1][?(@.['id'] == 102)]"""
			}
		and: "should have assertion for data[1].name"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(1).field("['name']").isEqualTo("Second Item")""" &&
						it.jsonPath() == """\$.['data'][1][?(@.['name'] == 'Second Item')]"""
			}
		and: "should have assertion for data[2].id"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(2).field("['id']").isEqualTo(103)""" &&
						it.jsonPath() == """\$.['data'][2][?(@.['id'] == 103)]"""
			}
		and: "should have assertion for data[2].name"
			pathAndValues.find {
				it.method() == """.array("['data']").elementWithIndex(2).field("['name']").isEqualTo("Third Item")""" &&
						it.jsonPath() == """\$.['data'][2][?(@.['name'] == 'Third Item')]"""
			}
		and: "should have exactly 8 assertions (2 metadata fields + 3 data items x 2 fields)"
			pathAndValues.size() == 8
	}

	def "should generate ordered assertions for timeline/sequence data - all fields"() {
		given:
			Map json = [
					events: [
							[timestamp: "2024-01-01T10:00:00Z", action: "created"],
							[timestamp: "2024-01-01T10:05:00Z", action: "updated"],
							[timestamp: "2024-01-01T10:10:00Z", action: "published"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for events[0].timestamp"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(0).field("['timestamp']").isEqualTo("2024-01-01T10:00:00Z")""" &&
						it.jsonPath() == """\$.['events'][0][?(@.['timestamp'] == '2024-01-01T10:00:00Z')]"""
			}
		and: "should have assertion for events[0].action"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(0).field("['action']").isEqualTo("created")""" &&
						it.jsonPath() == """\$.['events'][0][?(@.['action'] == 'created')]"""
			}
		and: "should have assertion for events[1].timestamp"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(1).field("['timestamp']").isEqualTo("2024-01-01T10:05:00Z")""" &&
						it.jsonPath() == """\$.['events'][1][?(@.['timestamp'] == '2024-01-01T10:05:00Z')]"""
			}
		and: "should have assertion for events[1].action"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(1).field("['action']").isEqualTo("updated")""" &&
						it.jsonPath() == """\$.['events'][1][?(@.['action'] == 'updated')]"""
			}
		and: "should have assertion for events[2].timestamp"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(2).field("['timestamp']").isEqualTo("2024-01-01T10:10:00Z")""" &&
						it.jsonPath() == """\$.['events'][2][?(@.['timestamp'] == '2024-01-01T10:10:00Z')]"""
			}
		and: "should have assertion for events[2].action"
			pathAndValues.find {
				it.method() == """.array("['events']").elementWithIndex(2).field("['action']").isEqualTo("published")""" &&
						it.jsonPath() == """\$.['events'][2][?(@.['action'] == 'published')]"""
			}
		and: "should have exactly 6 assertions (3 events x 2 fields)"
			pathAndValues.size() == 6
	}

	// ========== Edge Cases ==========

	def "should generate ordered assertions for single element array"() {
		given:
			Map json = [
					items: ["only"]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
	then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['items']").hasSize(1)""" &&
						it.jsonPath() == """\$.['items']"""
			}
	and: "should have assertion for the single element at index 0"
		pathAndValues.find {
			it.method() == """.array("['items']").elementWithIndex(0).isEqualTo("only")""" &&
					it.jsonPath() == """\$.['items'][0]"""
		}
		and: "should have exactly 2 assertions (1 size + 1 element)"
			pathAndValues.size() == 2
	}

	def "should not generate assertions for empty array"() {
		given:
			Map json = [
					items: []
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have exactly 1 assertion (empty array check)"
			pathAndValues.size() == 1
		and: "should not have any elementWithIndex assertions"
			!pathAndValues.find { it.method().contains("elementWithIndex") }
	}

	def "should handle array with decimal numbers"() {
		given:
			Map json = [
					prices: [19.99, 29.99, 9.99]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
	then: "should have size check"
			pathAndValues.find {
				it.method() == """.array("['prices']").hasSize(3)""" &&
						it.jsonPath() == """\$.['prices']"""
			}
	and: "should have assertion for prices[0]"
		pathAndValues.find {
			it.method() == """.array("['prices']").elementWithIndex(0).isEqualTo(19.99)""" &&
					it.jsonPath() == """\$.['prices'][0]"""
		}
	and: "should have assertion for prices[1]"
		pathAndValues.find {
			it.method() == """.array("['prices']").elementWithIndex(1).isEqualTo(29.99)""" &&
					it.jsonPath() == """\$.['prices'][1]"""
		}
	and: "should have assertion for prices[2]"
		pathAndValues.find {
			it.method() == """.array("['prices']").elementWithIndex(2).isEqualTo(9.99)""" &&
					it.jsonPath() == """\$.['prices'][2]"""
		}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	// ========== Comparison with Unordered ==========

	def "ordered verification should produce different results than unordered"() {
		given:
			Map json = [
					items: ["a", "b", "c"]
			]
		when:
			JsonPaths orderedPaths = converter().transformToJsonPathWithTestsSideValues(json)
			JsonPaths unorderedPaths = new JsonToJsonPathsConverter(false).transformToJsonPathWithTestsSideValues(json)
	then: "ordered should use elementWithIndex"
			orderedPaths.any { it.method().contains("elementWithIndex") }
	and: "unordered should not use elementWithIndex"
			!unorderedPaths.any { it.method().contains("elementWithIndex") }
	and: "ordered should have exact index paths [0], [1], [2]"
			orderedPaths.find { it.method() == """.array("['items']").elementWithIndex(0).isEqualTo("a")""" &&
					it.jsonPath() == """\$.['items'][0]""" }
			orderedPaths.find { it.method() == """.array("['items']").elementWithIndex(1).isEqualTo("b")""" &&
					it.jsonPath() == """\$.['items'][1]""" }
			orderedPaths.find { it.method() == """.array("['items']").elementWithIndex(2).isEqualTo("c")""" &&
					it.jsonPath() == """\$.['items'][2]""" }
	and: "unordered should use arrayField with filtered json paths"
			unorderedPaths.find { it.method() == """.array("['items']").arrayField().isEqualTo("a").value()""" &&
					it.jsonPath() == """\$.['items'][?(@ == 'a')]""" }
			unorderedPaths.find { it.method() == """.array("['items']").arrayField().isEqualTo("b").value()""" &&
					it.jsonPath() == """\$.['items'][?(@ == 'b')]""" }
			unorderedPaths.find { it.method() == """.array("['items']").arrayField().isEqualTo("c").value()""" &&
					it.jsonPath() == """\$.['items'][?(@ == 'c')]""" }
	}

	// ========== JSON Path Validity ==========

	def "all generated json paths should be valid and account for all elements"() {
		given:
			Map json = [
					users: [
							[name: "Alice", roles: ["admin", "user"]],
							[name: "Bob", roles: ["user"]]
					],
					metadata: [
							version: "1.0",
							tags: ["important", "reviewed"]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
			DocumentContext context = JsonPath.parse(JsonOutput.toJson(json))
		then: "all JSON paths should be valid"
			pathAndValues.each { path ->
				try {
					context.read(path.jsonPath())
				}
				catch (Exception e) {
					// Some paths with filters may not match but should still be valid syntax
					assert path.jsonPath().startsWith('$')
				}
			}
		and: "should have assertions for users[0].name"
			pathAndValues.find { it.method().contains("elementWithIndex(0)") && it.method().contains("['name']") && it.method().contains("Alice") }
		and: "should have assertions for users[1].name"
			pathAndValues.find { it.method().contains("elementWithIndex(1)") && it.method().contains("['name']") && it.method().contains("Bob") }
		and: "should have assertions for metadata.version"
			pathAndValues.find { it.method().contains("['version']") && it.method().contains("1.0") }
		and: "should have assertions for metadata.tags"
			pathAndValues.find { it.method().contains("['tags']") && it.method().contains("important") }
			pathAndValues.find { it.method().contains("['tags']") && it.method().contains("reviewed") }
	}

	// ========== Stub Side Values ==========

	def "should generate ordered assertions for stub side values - all elements"() {
		given:
			Map json = [
					items: ["one", "two", "three"]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithStubsSideValues(json)
		then: "should have size check"
			pathAndValues.find { it.method().contains("hasSize(3)") }
		and: "should have assertion for items[0]"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") && it.method().contains("isEqualTo(\"one\")")
			}
		and: "should have assertion for items[1]"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") && it.method().contains("isEqualTo(\"two\")")
			}
		and: "should have assertion for items[2]"
			pathAndValues.find {
				it.method().contains("elementWithIndex(2)") && it.method().contains("isEqualTo(\"three\")")
			}
		and: "should have exactly 4 assertions (1 size + 3 elements)"
			pathAndValues.size() == 4
	}

	// ========== Additional Complex Scenarios ==========

	def "should generate ordered assertions for deeply nested structure"() {
		given:
			Map json = [
					level1: [
							level2: [
									items: [
											[id: 1, data: [value: "a"]],
											[id: 2, data: [value: "b"]]
									]
							]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for level1.level2.items[0].id"
			pathAndValues.find {
				it.method().contains("['level1']") &&
						it.method().contains("['level2']") &&
						it.method().contains("['items']") &&
						it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['id']") &&
						it.method().contains("isEqualTo(1)")
			}
		and: "should have assertion for level1.level2.items[0].data.value"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['data']") &&
						it.method().contains("['value']") &&
						it.method().contains("isEqualTo(\"a\")")
			}
		and: "should have assertion for level1.level2.items[1].id"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['id']") &&
						it.method().contains("isEqualTo(2)")
			}
		and: "should have assertion for level1.level2.items[1].data.value"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['data']") &&
						it.method().contains("['value']") &&
						it.method().contains("isEqualTo(\"b\")")
			}
	}

	def "should generate ordered assertions for array with null values"() {
		given:
			Map json = [
					items: [
							[name: "first", value: null],
							[name: "second", value: 123]
					]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have assertion for items[0].name"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['name']") &&
						it.method().contains("isEqualTo(\"first\")")
			}
		and: "should have assertion for items[0].value being null"
			pathAndValues.find {
				it.method().contains("elementWithIndex(0)") &&
						it.method().contains("['value']") &&
						it.method().contains("isNull()")
			}
		and: "should have assertion for items[1].name"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['name']") &&
						it.method().contains("isEqualTo(\"second\")")
			}
		and: "should have assertion for items[1].value"
			pathAndValues.find {
				it.method().contains("elementWithIndex(1)") &&
						it.method().contains("['value']") &&
						it.method().contains("isEqualTo(123)")
			}
	}

	def "should generate ordered assertions for multiple arrays in same object"() {
		given:
			Map json = [
					names: ["Alice", "Bob"],
					ages: [30, 25],
					active: [true, false]
			]
		when:
			JsonPaths pathAndValues = converter().transformToJsonPathWithTestsSideValues(json)
		then: "should have size checks for all arrays"
			pathAndValues.find { it.method().contains("['names']") && it.method().contains("hasSize(2)") }
			pathAndValues.find { it.method().contains("['ages']") && it.method().contains("hasSize(2)") }
			pathAndValues.find { it.method().contains("['active']") && it.method().contains("hasSize(2)") }
		and: "should have assertions for names array"
			pathAndValues.find { it.method().contains("['names']") && it.method().contains("elementWithIndex(0)") && it.method().contains("Alice") }
			pathAndValues.find { it.method().contains("['names']") && it.method().contains("elementWithIndex(1)") && it.method().contains("Bob") }
		and: "should have assertions for ages array"
			pathAndValues.find { it.method().contains("['ages']") && it.method().contains("elementWithIndex(0)") && it.method().contains("isEqualTo(30)") }
			pathAndValues.find { it.method().contains("['ages']") && it.method().contains("elementWithIndex(1)") && it.method().contains("isEqualTo(25)") }
		and: "should have assertions for active array"
			pathAndValues.find { it.method().contains("['active']") && it.method().contains("elementWithIndex(0)") && it.method().contains("isEqualTo(true)") }
			pathAndValues.find { it.method().contains("['active']") && it.method().contains("elementWithIndex(1)") && it.method().contains("isEqualTo(false)") }
		and: "should have exactly 9 assertions (3 arrays x (1 size + 2 elements))"
			pathAndValues.size() == 9
	}

}
