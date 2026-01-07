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

import groovy.json.JsonSlurper
import spock.lang.Specification

import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.RegexProperty

/**
 * Tests for {@link JsonPathMatcherUtils}.
 *
 * @author Marcin Grzejszczak
 * @since 5.1.0
 */
class JsonPathMatcherUtilsSpec extends Specification {

	def 'should read element from JSON by path'() {
		given:
			def json = [
				person: [
					name: "John",
					age: 30
				]
			]
		when:
			def name = JsonPathMatcherUtils.readElement(json, '$.person.name')
			def age = JsonPathMatcherUtils.readElement(json, '$.person.age')
		then:
			name == "John"
			age == 30
	}

	def 'should read nested array element from JSON'() {
		given:
			def json = [
				items: [
					[id: 1, name: "first"],
					[id: 2, name: "second"]
				]
			]
		when:
			def firstId = JsonPathMatcherUtils.readElement(json, '$.items[0].id')
			def secondName = JsonPathMatcherUtils.readElement(json, '$.items[1].name')
		then:
			firstId == 1
			secondName == "second"
	}

	def 'should remove matching JSON paths from body'() {
		given:
			def json = [
				person: [
					name: "John",
					age: 30,
					email: "john@example.com"
				]
			]
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.person.email', bodyMatchers.byRegex('.*'))
		when:
			def result = JsonPathMatcherUtils.removeMatchingJsonPaths(json, bodyMatchers)
		then:
			result.person.name == "John"
			result.person.age == 30
			result.person.email == null
	}

	def 'should return original JSON when no matchers provided'() {
		given:
			def json = [name: "John"]
		when:
			def result = JsonPathMatcherUtils.removeMatchingJsonPaths(json, null)
		then:
			result.name == "John"
	}

	def 'should return original JSON when matchers have no entries'() {
		given:
			def json = [name: "John"]
			def bodyMatchers = new BodyMatchers()
		when:
			def result = JsonPathMatcherUtils.removeMatchingJsonPaths(json, bodyMatchers)
		then:
			result.name == "John"
	}

	def 'should convert JSON path with regex to filter expression'() {
		given:
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.person.name', bodyMatchers.byRegex('[A-Z][a-z]+'))
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher)
		then:
			result == '$.person[?(@.name =~ /([A-Z][a-z]+)/)]'
	}

	def 'should convert JSON path with equality to filter expression'() {
		given:
			def json = [
				person: [
					name: "John"
				]
			]
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.person.name', bodyMatchers.byEquality())
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher, json)
		then:
			result == "\$.person[?(@.name == 'John')]"
	}

	def 'should convert JSON path with numeric equality'() {
		given:
			def json = [
				person: [
					age: 30
				]
			]
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.person.age', bodyMatchers.byEquality())
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher, json)
		then:
			result == '$.person[?(@.age == 30)]'
	}

	def 'should convert JSON path with type matching and min occurrence'() {
		given:
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.items', bodyMatchers.byType { minOccurrence(2) })
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher)
		then:
			result == '$[?(@.items.size() >= 2)]'
	}

	def 'should convert JSON path with type matching and max occurrence'() {
		given:
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.items', bodyMatchers.byType { maxOccurrence(5) })
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher)
		then:
			result == '$[?(@.items.size() <= 5)]'
	}

	def 'should convert JSON path with type matching with min and max occurrence'() {
		given:
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.items', bodyMatchers.byType { minOccurrence(2); maxOccurrence(5) })
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher)
		then:
			result == '$[?(@.items.size() >= 2 && @.items.size() <= 5)]'
	}

	def 'should return original value for non-RegexProperty'() {
		given:
			def value = "test value"
		when:
			def result = JsonPathMatcherUtils.generatedValueIfNeeded(value)
		then:
			result == "test value"
	}

	def 'should return original value for numeric value'() {
		given:
			def value = 42
		when:
			def result = JsonPathMatcherUtils.generatedValueIfNeeded(value)
		then:
			result == 42
	}

	def 'should clone body correctly'() {
		given:
			def original = [name: "John", age: 30, items: [1, 2, 3]]
		when:
			def cloned = JsonPathMatcherUtils.cloneBody(original)
		then:
			cloned == original
			!cloned.is(original)
	}

	def 'should handle bracket notation in path for equality'() {
		given:
			def json = [
				person: [
					"first-name": "John"
				]
			]
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath("\$.person['first-name']", bodyMatchers.byEquality())
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher, json)
		then:
			result == "\$[?(@.person['first-name'] == 'John')]"
	}

	def 'should remove array element matching path'() {
		given:
			def json = [
				items: [
					[id: 1, name: "first"],
					[id: 2, name: "second"]
				]
			]
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.items[0].id', bodyMatchers.byRegex('\\d+'))
		when:
			def result = JsonPathMatcherUtils.removeMatchingJsonPaths(json, bodyMatchers)
		then:
			result.items[0].id == null
			result.items[0].name == "first"
			result.items[1].id == 2
			result.items[1].name == "second"
	}

	def 'should handle regex with forward slashes'() {
		given:
			def bodyMatchers = new BodyMatchers()
			bodyMatchers.jsonPath('$.url', bodyMatchers.byRegex('http://example.com/path'))
			def bodyMatcher = bodyMatchers.matchers().first()
		when:
			def result = JsonPathMatcherUtils.convertJsonPathAndRegexToAJsonPath(bodyMatcher)
		then:
			result.contains('http:\\/\\/example.com\\/path')
	}

	def 'should read root level array'() {
		given:
			def json = [
				[id: 1],
				[id: 2]
			]
		when:
			def firstId = JsonPathMatcherUtils.readElement(json, '$[0].id')
			def secondId = JsonPathMatcherUtils.readElement(json, '$[1].id')
		then:
			firstId == 1
			secondId == 2
	}

	def 'should handle deeply nested paths'() {
		given:
			def json = [
				level1: [
					level2: [
						level3: [
							value: "deep"
						]
					]
				]
			]
		when:
			def result = JsonPathMatcherUtils.readElement(json, '$.level1.level2.level3.value')
		then:
			result == "deep"
	}

}
