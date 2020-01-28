/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import groovy.json.JsonSlurper
import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata
import org.springframework.cloud.contract.verifier.util.ContentType

class WireMockResponseStubStrategySpec extends Specification {

	def "should not quote floating point numbers"() {
		given:
			def irrelevantStatus = 200
			def contract = Contract.make {
				request {
					method GET()
					url "/foo"
				}
				response {
					status irrelevantStatus
					body([
							value: 1.5
					])
				}
			}
		when:
			SingleContractMetadata metadata = Stub()
			metadata.evaluatedOutputStubContentType >> ContentType.JSON
			def subject = new WireMockResponseStubStrategy(contract, metadata)
			def content = subject.buildClientResponseContent()
		then:
			'{"value":1.5}' == content.body
	}

	@Issue("#468")
	def "should not quote generated numbers"() {
		given:
			def irrelevantStatus = 200
			def contract = Contract.make {
				request {
					method GET()
					url "/foo"
				}
				response {
					status irrelevantStatus
					body([
							number     : anyNumber(),
							integer    : anyInteger(),
							positiveInt: anyPositiveInt(),
							double     : anyDouble(),
					])
				}
			}
		when:
			SingleContractMetadata metadata = Stub()
			metadata.evaluatedOutputStubContentType >> ContentType.JSON
			def subject = new WireMockResponseStubStrategy(contract, metadata)
			def content = subject.buildClientResponseContent()
		then:
			Map body = new JsonSlurper().parseText(content.body) as Map
			assert body.get("number") instanceof Number
			assert body.get("integer") instanceof Integer
			assert body.get("positiveInt") instanceof Integer
			assert body.get("double") instanceof BigDecimal
	}

	def "should convert patterns to proper value"() {
		given:
			def contract = Contract.make {
				request {
					method(GET())
					urlPath(value(regex("/info/[0-9]"))) {
						queryParameters {
							parameter 'limit': $(consumer(equalTo('20')), producer(equalTo('10')))
							parameter 'offset': $(consumer(containing("20")), producer(equalTo('20')))
							parameter 'filter': 'email'
							parameter 'sort': equalTo("name")
							parameter 'age': $(consumer(notMatching("^\\w*\$")), producer('99'))
							parameter 'name': $(consumer(matching('John.*')), producer('John.Doe'))
							parameter 'email': 'bob@email.com'
							parameter 'hello': $(consumer(matching('John.*')), producer(absent()))
							parameter 'hello2': absent()
						}
					}
					headers {
						contentType(applicationJson())
						header([
								second: "value", third: $(anyAlphaNumeric())
						])
					}
					body([
							foo1                : $(c(regex("[0-9]")), p(1)),
							foo2                : $(c(regex("[0-9]"))),
							foo3                : $(anyAlphaNumeric()),
							foo4                : value(regex(aDouble())),
							foo5                : value(anyDouble()),
							foo6                : "concrete",
							duck                : 123,
							alpha               : 'abc',
							number              : 123,
							aBoolean            : true,
							date                : '2017-01-01',
							dateTime            : '2017-01-01T01:23:45',
							time                : '01:02:34',
							valueWithoutAMatcher: 'foo',
							valueWithTypeMatch  : 'string',
							key                 : [
									'complex.key': 'foo'
							]
					])
					bodyMatchers {
						jsonPath('$.duck', byRegex("[0-9]{3}"))
						jsonPath('$.duck', byEquality())
						jsonPath('$.alpha', byRegex(onlyAlphaUnicode()))
						jsonPath('$.alpha', byEquality())
						jsonPath('$.number', byRegex(number()))
						jsonPath('$.aBoolean', byRegex(anyBoolean()))
						jsonPath('$.date', byDate())
						jsonPath('$.dateTime', byTimestamp())
						jsonPath('$.time', byTime())
						jsonPath("\$.['key'].['complex.key']", byEquality())
					}
				}
				response {
					status(OK())
					headers {
						contentType(applicationJson())
						header([
								second: "value", third: $(anyAlphaNumeric())
						])
					}
					body([
							foo1                 : $(p(regex("[0-9]")), c(1)),
							foo2                 : $(p(regex("[0-9]"))),
							foo3                 : $(anyAlphaNumeric()),
							foo4                 : value(regex(aDouble())),
							foo5                 : value(anyDouble()),
							foo6                 : "concrete",
							duck                 : 123,
							alpha                : 'abc',
							number               : 123,
							positiveInteger      : 1234567890,
							negativeInteger      : -1234567890,
							positiveDecimalNumber: 123.4567890,
							negativeDecimalNumber: -123.4567890,
							aBoolean             : true,
							date                 : '2017-01-01',
							dateTime             : '2017-01-01T01:23:45',
							time                 : "01:02:34",
							valueWithoutAMatcher : 'foo',
							valueWithTypeMatch   : 'string',
							valueWithMin         : [
									1, 2, 3
							],
							valueWithMax         : [
									1, 2, 3
							],
							valueWithMinMax      : [
									1, 2, 3
							],
							valueWithMinEmpty    : [],
							valueWithMaxEmpty    : [],
							key                  : [
									'complex.key': 'foo'
							],
							nullValue            : null
					])
					bodyMatchers {
						// asserts the jsonpath value against manual regex
						jsonPath('$.duck', byRegex("[0-9]{3}"))
						// asserts the jsonpath value against the provided value
						jsonPath('$.duck', byEquality())
						// asserts the jsonpath value against some default regex
						jsonPath('$.alpha', byRegex(onlyAlphaUnicode()))
						jsonPath('$.alpha', byEquality())
						jsonPath('$.number', byRegex(number()))
						jsonPath('$.positiveInteger', byRegex(anInteger()))
						jsonPath('$.negativeInteger', byRegex(anInteger()))
						jsonPath('$.positiveDecimalNumber', byRegex(aDouble()))
						jsonPath('$.negativeDecimalNumber', byRegex(aDouble()))
						jsonPath('$.aBoolean', byRegex(anyBoolean()))
						// asserts vs inbuilt time related regex
						jsonPath('$.date', byDate())
						jsonPath('$.dateTime', byTimestamp())
						jsonPath('$.time', byTime())
						// asserts that the resulting type is the same as in response body
						jsonPath('$.valueWithTypeMatch', byType())
						jsonPath('$.valueWithMin', byType {
							// results in verification of size of array (min 1)
							minOccurrence(1)
						})
						jsonPath('$.valueWithMax', byType {
							// results in verification of size of array (max 3)
							maxOccurrence(3)
						})
						jsonPath('$.valueWithMinMax', byType {
							// results in verification of size of array (min 1 & max 3)
							minOccurrence(1)
							maxOccurrence(3)
						})
						jsonPath('$.valueWithMinEmpty', byType {
							// results in verification of size of array (min 0)
							minOccurrence(0)
						})
						jsonPath('$.valueWithMaxEmpty', byType {
							// results in verification of size of array (max 0)
							maxOccurrence(0)
						})
						// will execute a method `assertThatValueIsANumber`
						jsonPath('$.duck', byCommand('assertThatValueIsANumber($it)'))
						jsonPath("\$.['key'].['complex.key']", byEquality())
						jsonPath('$.nullValue', byNull())
					}
				}
			}
		when:
			def subject = new WireMockRequestStubStrategy(contract, null) {
				@Override
				protected ContentType contentType(SingleContractMetadata singleContractMetadata) {
					return ContentType.JSON
				}
			}
			subject.buildClientRequestContent()
		then:
			noExceptionThrown()
		when:
			def response = new WireMockResponseStubStrategy(contract, null) {
				@Override
				protected ContentType contentType(SingleContractMetadata singleContractMetadata) {
					return ContentType.JSON
				}
			}
			response.buildClientResponseContent()
		then:
			noExceptionThrown()
	}
}
