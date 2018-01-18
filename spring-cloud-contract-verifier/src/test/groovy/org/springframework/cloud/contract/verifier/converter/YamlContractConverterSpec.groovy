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

package org.springframework.cloud.contract.verifier.converter

import java.util.regex.Pattern

import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * @author Marcin Grzejszczak
 */
class YamlContractConverterSpec extends Specification {

	@Shared URL ymlUrl = YamlContractConverterSpec.getResource("/yml/contract.yml")
	@Shared File ymlWithRest = new File(ymlUrl.toURI())
	@Shared URL ymlUrl2 = YamlContractConverterSpec.getResource("/yml/contract_rest.yml")
	@Shared File ymlWithRest2 = new File(ymlUrl2.toURI())
	@Shared URL ymlUrl3 = YamlContractConverterSpec.getResource("/yml/contract_rest_with_path.yml")
	@Shared File ymlWithRest3 = new File(ymlUrl3.toURI())
	URL ymlMsgUrl = YamlContractConverterSpec.getResource("/yml/contract_message.yml")
	File ymlMessaging = new File(ymlMsgUrl.toURI())
	URL ymlMsgMethodUrl = YamlContractConverterSpec.getResource("/yml/contract_message_method.yml")
	File ymlMessagingMethod = new File(ymlMsgMethodUrl.toURI())
	URL ymlMsgMsgUrl = YamlContractConverterSpec.getResource("/yml/contract_message_input_message.yml")
	File ymlMessagingMsg = new File(ymlMsgMsgUrl.toURI())
	URL ymlBodyFile = YamlContractConverterSpec.getResource("/yml/contract_from_file.yml")
	File ymlBody = new File(ymlBodyFile.toURI())
	URL ymlReferenceFile = YamlContractConverterSpec.getResource("/yml/contract_reference_request.yml")
	File ymlReference = new File(ymlReferenceFile.toURI())
	URL ymlMatchersFile = YamlContractConverterSpec.getResource("/yml/contract_matchers.yml")
	File ymlMatchers = new File(ymlMatchersFile.toURI())
	URL ymlMultipleFile = YamlContractConverterSpec.getResource("/yml/multiple_contracts.yml")
	File ymlMultiple = new File(ymlMultipleFile.toURI())
	YamlContractConverter converter = new YamlContractConverter()

	def "should convert YAML with REST to DSL for [#yamlFile]"() {
		given:
			assert converter.isAccepted(yamlFile)
		when:
			Collection<Contract> contracts = converter.convertFrom(yamlFile)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.name == "some name"
			contract.priority == 8
			contract.ignored == true
			Url url = yamlFile == ymlWithRest3 ?
					contract.request.urlPath : contract.request.url
			url.clientValue == "/foo"
			url.queryParameters.parameters[0].name == "a"
			url.queryParameters.parameters[0].serverValue == "b"
			url.queryParameters.parameters[1].name == "b"
			url.queryParameters.parameters[1].serverValue == "c"
			contract.request.method.clientValue == "PUT"
			contract.request.headers.entries.find { it.name == "foo" &&
					((Pattern) it.clientValue).pattern == "bar" && it.serverValue == "bar" }
			contract.request.headers.entries.find { it.name == "fooReq" &&
					it.serverValue == "baz" }
			contract.request.body.clientValue == [foo: "bar"]
			contract.request.matchers.jsonPathRegexMatchers[0].path() == '$.foo'
			contract.request.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.request.matchers.jsonPathRegexMatchers[0].value() == 'bar'
		and:
			contract.response.status.clientValue == 200
			contract.response.headers.entries.find { it.name == "foo2" &&
					((Pattern) it.serverValue).pattern == "bar" && it.clientValue == "bar" }
			contract.response.headers.entries.find { it.name == "foo3" &&
					((ExecutionProperty) it.serverValue).insertValue('foo') == "andMeToo(foo)" }
			contract.response.headers.entries.find { it.name == "fooRes" &&
					it.clientValue == "baz" }
			contract.response.body.clientValue == [foo2: "bar", foo3: "baz"]
			contract.response.matchers.jsonPathRegexMatchers[0].path() == '$.foo2'
			contract.response.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.response.matchers.jsonPathRegexMatchers[0].value() == 'bar'
			contract.response.matchers.jsonPathRegexMatchers[1].path() == '$.foo3'
			contract.response.matchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.COMMAND
			contract.response.matchers.jsonPathRegexMatchers[1].value() == new ExecutionProperty('executeMe($it)')
		where:
			yamlFile << [ymlWithRest, ymlWithRest2, ymlWithRest3]
	}

	def "should convert YAML with REST to DSL with advanced request referencing"() {
		given:
			assert converter.isAccepted(ymlReference)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlReference)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			Url url = contract.request.url
			url.clientValue == "/api/v1/xxxx"
			url.queryParameters.parameters[0].name == "foo"
			url.queryParameters.parameters[0].serverValue == "bar"
			url.queryParameters.parameters[1].name == "foo"
			url.queryParameters.parameters[1].serverValue == "bar2"
			contract.request.method.clientValue == "GET"
			contract.request.headers.entries.findAll { it.name == "Authorization" }
					.collect { it.clientValue } == ["secret", "secret2"]
			contract.request.body.clientValue == [foo: "bar", baz: 5]
		and:
			contract.response.status.clientValue == 200
			contract.response.headers.entries
					.find { it.name == "Authorization" }.clientValue == '''foo {{{ request.headers.Authorization.0 }}} bar'''
			with(MapConverter.getTestSideValues(contract.response.body)) {
				it.url == "{{{ request.url }}}"
				it.path == "{{{ request.path }}}"
				it.pathIndex == "{{{ request.path.1 }}}"
				it.param == "{{{ request.query.foo }}}"
				it.paramIndex == "{{{ request.query.foo.1 }}}"
				it.authorization == "{{{ request.headers.Authorization.0 }}}"
				it.authorization2 == "{{{ request.headers.Authorization.1 }}"
				it.fullBody == "{{{ request.body }}}"
				it.responseFoo == '''{{{ jsonpath this '$.foo' }}}'''
				it.responseBaz == '''{{{ jsonpath this '$.baz' }}}'''
				it.responseBaz2 == '''Bla bla {{{ jsonpath this '$.foo' }}} bla bla'''
			}
	}

	def "should convert YAML with REST matchers to DSL"() {
		given:
			assert converter.isAccepted(ymlMatchers)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMatchers)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			RegexPatterns patterns = new RegexPatterns()
			contract.request.headers.entries.find { it.name == "Content-Type" &&
					((Pattern) it.clientValue).pattern == "application/json.*" && it.serverValue == "application/json" }
			contract.request.matchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.request.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.request.matchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.request.matchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.request.matchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.request.matchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.request.matchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.request.matchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode()
			contract.request.matchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.request.matchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.request.matchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.request.matchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.request.matchers.jsonPathRegexMatchers[4].value() == patterns.number()
			contract.request.matchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.request.matchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.request.matchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean()
			contract.request.matchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.request.matchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.request.matchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.request.matchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.request.matchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.request.matchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.request.matchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.request.matchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.request.matchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.request.matchers.jsonPathRegexMatchers[9].path() == "\$.['key'].['complex.key']"
			contract.request.matchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.EQUALITY
		and:
			contract.response.status.clientValue == 200
			contract.response.matchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.response.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.response.matchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.response.matchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.response.matchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.response.matchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.response.matchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.response.matchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode()
			contract.response.matchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.response.matchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.response.matchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.response.matchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.response.matchers.jsonPathRegexMatchers[4].value() == patterns.number()
			contract.response.matchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.response.matchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.response.matchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean()
			contract.response.matchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.response.matchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.response.matchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.response.matchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.response.matchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.response.matchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.response.matchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.response.matchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.response.matchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.response.matchers.jsonPathRegexMatchers[9].path() == '$.valueWithTypeMatch'
			contract.response.matchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[10].path() == '$.valueWithMin'
			contract.response.matchers.jsonPathRegexMatchers[10].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[10].minTypeOccurrence() == 1
			contract.response.matchers.jsonPathRegexMatchers[11].path() == '$.valueWithMax'
			contract.response.matchers.jsonPathRegexMatchers[11].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[11].maxTypeOccurrence() == 3
			contract.response.matchers.jsonPathRegexMatchers[12].path() == '$.valueWithMinMax'
			contract.response.matchers.jsonPathRegexMatchers[12].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[12].minTypeOccurrence() == 1
			contract.response.matchers.jsonPathRegexMatchers[12].maxTypeOccurrence() == 3
			contract.response.matchers.jsonPathRegexMatchers[13].path() == '$.valueWithMinEmpty'
			contract.response.matchers.jsonPathRegexMatchers[13].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[13].minTypeOccurrence() == 0
			contract.response.matchers.jsonPathRegexMatchers[14].path() == '$.valueWithMaxEmpty'
			contract.response.matchers.jsonPathRegexMatchers[14].matchingType() == MatchingType.TYPE
			contract.response.matchers.jsonPathRegexMatchers[14].maxTypeOccurrence() == 0
			contract.response.matchers.jsonPathRegexMatchers[15].path() == '$.duck'
			contract.response.matchers.jsonPathRegexMatchers[15].matchingType() == MatchingType.COMMAND
			contract.response.matchers.jsonPathRegexMatchers[15].value() == new ExecutionProperty('assertThatValueIsANumber($it)')
	}

	def "should convert YAML with REST with response from request"() {
		given:
			assert converter.isAccepted(ymlBody)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlBody)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.request.body.clientValue == '''{ "hello" : "request" }'''
		and:
			contract.response.body.clientValue == '''{ "hello" : "response" }'''
	}

	def "should convert YAML with messaging to DSL"() {
		given:
			assert converter.isAccepted(ymlMessaging)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMessaging)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.name == "some name"
			contract.label == "some_label"
			contract.ignored == true
			contract.input.assertThat.toString() == "bar()"
			contract.input.messageFrom.serverValue == "foo"
			contract.input.triggeredBy.toString() == "foo()"
			contract.input.messageHeaders.entries.find { it.name == "foo" &&
					((Pattern) it.clientValue).pattern == "bar" && it.serverValue == "bar" }
			contract.input.messageBody.clientValue == [foo: "bar"]
			contract.input.matchers.jsonPathRegexMatchers[0].path() == '$.bar'
			contract.input.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.input.matchers.jsonPathRegexMatchers[0].value() == 'bar'
		and:
			contract.outputMessage.assertThat.toString() == "baz()"
			contract.outputMessage.headers.entries.find { it.name == "foo2" &&
					((Pattern) it.serverValue).pattern == "bar" && it.clientValue == "bar" }
			contract.outputMessage.headers.entries.find { it.name == "foo3" &&
					((ExecutionProperty) it.serverValue).insertValue('foo') == "andMeToo(foo)" }
			contract.outputMessage.headers.entries.find { it.name == "fooRes" &&
					it.clientValue == "baz" }
			contract.outputMessage.body.clientValue == [foo2: "bar", foo3: "baz"]
			contract.outputMessage.matchers.jsonPathRegexMatchers[0].path() == '$.foo2'
			contract.outputMessage.matchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.outputMessage.matchers.jsonPathRegexMatchers[0].value() == 'bar'
			contract.outputMessage.matchers.jsonPathRegexMatchers[1].path() == '$.foo3'
			contract.outputMessage.matchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.COMMAND
			contract.outputMessage.matchers.jsonPathRegexMatchers[1].value() == new ExecutionProperty('executeMe($it)')
	}

	def "should convert YAML with messaging triggered by a method to DSL"() {
		given:
			assert converter.isAccepted(ymlMessagingMethod)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMessagingMethod)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.label == "some_label"
			contract.input.triggeredBy.toString() == "bookReturnedTriggered()"
		and:
			contract.outputMessage.sentTo.clientValue == "output"
			contract.outputMessage.headers.entries.find {
				it.name == "BOOK-NAME" && it.clientValue == "foo" }
			contract.outputMessage.body.clientValue == [bookName: "foo"]
	}

	def "should convert YAML with messaging triggered by a message to DSL"() {
		given:
			assert converter.isAccepted(ymlMessagingMsg)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMessagingMsg)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.label == "some_label"
			contract.input.messageFrom.serverValue == "input"
			contract.input.messageHeaders.entries.find { it.name == "sample" &&
					it.serverValue == "header" }
			contract.input.messageBody.clientValue == [bookName: "foo"]
		and:
			contract.outputMessage.sentTo.clientValue == "output"
			contract.outputMessage.headers.entries.find {
				it.name == "BOOK-NAME" && it.clientValue == "foo" }
			contract.outputMessage.body.clientValue == [bookName: "foo"]
	}

	def "should assert request headers when converting YAML to DSL"() {
		given:
			File yml = new File(YamlContractConverterSpec.getResource("/yml/contract_broken_request_headers.yml").toURI())
		and:
			assert converter.isAccepted(yml)
		when:
			converter.convertFrom(yml)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Broken headers! A header with key [foo] with value [bar] is not matched by regex [barrrr]")
	}

	def "should assert response headers when converting YAML to DSL"() {
		given:
			File yml = new File(YamlContractConverterSpec.getResource("/yml/contract_broken_response_headers.yml").toURI())
		and:
			assert converter.isAccepted(yml)
		when:
			converter.convertFrom(yml)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Broken headers! A header with key [foo2] with value [bar] is not matched by regex [barrrr]")
	}

	def "should parse multiple documents into a list of contracts"() {
		given:
			assert converter.isAccepted(ymlMultiple)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMultiple)
		then:
			contracts.size() == 2
		and:
			contracts.first().request.url.clientValue == "/users/1"
			contracts.last().request.url.clientValue == "/users/2"
	}

	def "should parse messaging contract for [#file]"() {
		given:
			assert converter.isAccepted(file)
		when:
			Collection<Contract> contracts = converter.convertFrom(file)
		then:
			contracts.size() == 1
		and:
			contracts.first().input != null || contracts.first().outputMessage != null
		where:
			file << [1,2,3].collect {
				new File(YamlContractConverterSpec.getResource("/yml/contract_message_scenario${it}.yml").toURI())
			}
	}

	def "should convert DSL to YAML"() {
		given:
			assert converter.isAccepted(ymlWithRest)
		and:
			List<Contract> contracts = [Contract.make {
				request {
					url("/foo")
					method("PUT")
					headers {
						header("foo", "bar")
					}
					body([foo: "bar"])
				}
				response {
					status(200)
					headers {
						header("foo2", "bar")
					}
					body([foo2: "bar"])
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.url == "/foo"
			yamlContract.request.method == "PUT"
			yamlContract.request.headers.find { it.key == "foo" && it.value == "bar" }
			yamlContract.request.body == [foo: "bar"]
			yamlContract.response.status == 200
			yamlContract.response.headers.find { it.key == "foo2" && it.value == "bar" }
			yamlContract.response.body == [foo2: "bar"]
	}
}
