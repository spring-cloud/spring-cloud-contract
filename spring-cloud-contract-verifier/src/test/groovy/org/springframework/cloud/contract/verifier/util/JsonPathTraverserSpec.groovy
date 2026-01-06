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

import java.util.function.Function

import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * Tests for {@link JsonPathTraverser}.
 *
 * @author Marcin Grzejszczak
 * @since 5.1.0
 */
class JsonPathTraverserSpec extends Specification {

	Function<String, Object> parsingFunction = { String s -> new JsonSlurper().parseText(s) }

	def 'should traverse simple object without ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"name": "John",
					"age": 30
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath() == '''$[?(@.['name'] == 'John')]''' }
			collected.any { it.jsonPath() == '''$[?(@.['age'] == 30)]''' }
	}

	def 'should traverse primitive array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"numbers": [1, 2, 3]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('2') }
			collected.any { it.jsonPath().contains('[2]') && it.jsonPath().contains('3') }
	}

	def 'should traverse primitive array without ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"numbers": [1, 2, 3]
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('2') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('3') }
			!collected.any { it.jsonPath().contains('[0]') }
			!collected.any { it.jsonPath().contains('[1]') }
			!collected.any { it.jsonPath().contains('[2]') }
	}

	def 'should traverse object array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"items": [
						{"id": 1, "name": "first"},
						{"id": 2, "name": "second"}
					]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('id') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('name') && it.jsonPath().contains('first') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('id') && it.jsonPath().contains('2') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('name') && it.jsonPath().contains('second') }
	}

	def 'should traverse object array without ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"items": [
						{"id": 1, "name": "first"},
						{"id": 2, "name": "second"}
					]
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('id') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('name') && it.jsonPath().contains('first') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('id') && it.jsonPath().contains('2') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('name') && it.jsonPath().contains('second') }
	}

	def 'should traverse nested objects'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"person": {
						"address": {
							"city": "NYC",
							"zip": "10001"
						}
					}
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('city') && it.jsonPath().contains('NYC') }
			collected.any { it.jsonPath().contains('zip') && it.jsonPath().contains('10001') }
	}

	def 'should handle empty map'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"empty": {}
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('empty') }
	}

	def 'should handle empty array'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"items": []
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('items') }
	}

	def 'should traverse string array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"tags": ["red", "green", "blue"]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('red') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('green') }
			collected.any { it.jsonPath().contains('[2]') && it.jsonPath().contains('blue') }
	}

	def 'should traverse nested array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"matrix": [[1, 2], [3, 4]]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('[0]') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('[1]') && it.jsonPath().contains('2') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('[0]') && it.jsonPath().contains('3') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('[1]') && it.jsonPath().contains('4') }
	}

	def 'should traverse root level array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				[
					{"id": 1},
					{"id": 2}
				]
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('id') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('id') && it.jsonPath().contains('2') }
	}

	def 'should traverse root level array without ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				[
					{"id": 1},
					{"id": 2}
				]
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('id') && it.jsonPath().contains('1') }
			collected.any { it.jsonPath().contains('[*]') && it.jsonPath().contains('id') && it.jsonPath().contains('2') }
	}

	def 'should add size check for primitive arrays with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"numbers": [1, 2, 3]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('hasSize') || it.method().contains('hasSize') }
	}

	def 'should not add size check without ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"numbers": [1, 2, 3]
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			!collected.any { it.method().contains('hasSize') }
	}

	def 'should traverse mixed primitive types in array with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"mixed": ["text", 42, true, 3.14]
				}
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('[0]') && it.jsonPath().contains('text') }
			collected.any { it.jsonPath().contains('[1]') && it.jsonPath().contains('42') }
			collected.any { it.jsonPath().contains('[2]') && it.jsonPath().contains('true') }
			collected.any { it.jsonPath().contains('[3]') && it.jsonPath().contains('3.14') }
	}

	def 'should traverse deeply nested structure'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"level1": {
						"level2": {
							"level3": {
								"level4": {
									"value": "deep"
								}
							}
						}
					}
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('value') && it.jsonPath().contains('deep') }
	}

	def 'should handle boolean values'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"active": true,
					"deleted": false
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('active') && it.jsonPath().contains('true') }
			collected.any { it.jsonPath().contains('deleted') && it.jsonPath().contains('false') }
	}

	def 'should traverse array of arrays at root level with ordered verification'() {
		given:
			def json = new JsonSlurper().parseText('''
				[[1, 2], [3, 4], [5, 6]]
			''')
			def traverser = new JsonPathTraverser(true, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.size() > 0
			collected.any { it.jsonPath().contains('[0]') }
	}

	def 'should handle special characters in keys'() {
		given:
			def json = new JsonSlurper().parseText('''
				{
					"special-key": "value1",
					"key.with.dots": "value2"
				}
			''')
			def traverser = new JsonPathTraverser(false, parsingFunction)
			def rootKey = createRootVerifiable(json)
			def collected = []
		when:
			traverser.traverse(json, rootKey, { collected.add(it) })
		then:
			collected.any { it.jsonPath().contains('special-key') && it.jsonPath().contains('value1') }
			collected.any { it.jsonPath().contains('key.with.dots') && it.jsonPath().contains('value2') }
	}

	private MethodBufferingJsonVerifiable createRootVerifiable(Object json) {
		return new DelegatingJsonVerifiable(
				JsonAssertion.assertThat(JsonOutput.toJson(json)).withoutThrowingException()
		)
	}

}
