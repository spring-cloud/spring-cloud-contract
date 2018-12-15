/*
 *  Copyright 2013-2018 the original author or authors.
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

import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
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
	URL ymlMessagingMatchersFile = YamlContractConverterSpec.getResource("/yml/contract_message_matchers.yml")
	File ymlMessagingMatchers = new File(ymlMessagingMatchersFile.toURI())
	URL ymlCookiesUrl = YamlContractConverterSpec.getResource("/yml/contract_cookies.yml")
	File ymlCookies = new File(ymlCookiesUrl.toURI())
	URL ymlBytesUrl = YamlContractConverterSpec.getResource("/yml/contract_pdf.yml")
	File ymlBytes = new File(ymlBytesUrl.toURI())
	URL ymlMessagingBytesUrl = YamlContractConverterSpec.getResource("/yml/contract_messaging_pdf.yml")
	File ymlMessagingBytes = new File(ymlMessagingBytesUrl.toURI())
	YamlContractConverter converter = new YamlContractConverter()

	def "should convert YAML with Cookies to DSL"() {
		given:
			assert converter.isAccepted(ymlCookies)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlCookies)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Contract with cookies"
			contract.name == "cookies-contract"
			contract.priority == 1
			contract.ignored == true
			contract.request.method.clientValue == "PUT"
			contract.request.url.clientValue == "/foo"
			contract.request.cookies.entries.find { it.key == "foo" && it.serverValue == "bar" }
			contract.request.cookies.entries.find { it.key == "fooRegex" && ((Pattern) it.clientValue).pattern == "reg" && it.serverValue == "reg" }
			contract.request.cookies.entries.find { it.key == "fooPredefinedRegex" && ((Pattern) it.clientValue).pattern == "(true|false)" && it.serverValue == true }
		and:
			contract.response.status.clientValue == 200
			contract.response.cookies.entries.find { it.key == "foo" && it.clientValue == "baz" }
			contract.response.cookies.entries.find { it.key == "fooRegex" && ((Pattern) it.serverValue).pattern == "[0-9]+" && it.clientValue == 123 }
			contract.response.cookies.entries.find { it.key == "source" && ((Pattern) it.serverValue).pattern == "ip_address" && it.clientValue == "ip_address" }
			contract.response.cookies.entries.find { it.key == "fooPredefinedRegex" && ((Pattern) it.serverValue).pattern == "(true|false)" && it.clientValue == true }
			contract.response.body.clientValue == ["status": "OK"]
	}

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
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.foo'
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].value() == 'bar'
		and:
			contract.response.status.clientValue == 200
			if (yamlFile == ymlWithRest) contract.response.delay.clientValue == 1000 else !contract.response.delay
			contract.response.headers.entries.find { it.name == "foo2" &&
					((Pattern) it.serverValue).pattern == "bar" && it.clientValue == "bar" }
			contract.response.headers.entries.find { it.name == "foo3" &&
					((ExecutionProperty) it.serverValue).insertValue('foo') == "andMeToo(foo)" }
			contract.response.headers.entries.find { it.name == "fooRes" &&
					it.clientValue == "baz" }
			contract.response.body.clientValue == [foo2: "bar", foo3: "baz", nullValue: null]
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.foo2'
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].value() == 'bar'
			contract.response.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.foo3'
			contract.response.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.COMMAND
			contract.response.bodyMatchers.jsonPathRegexMatchers[1].value() == new ExecutionProperty('executeMe($it)')
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].path() == '$.nullValue'
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.NULL
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].value() == null
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
					.collect { it.clientValue }.flatten() == ["secret", "secret2"]
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
			((Pattern) contract.request.urlPath.clientValue).pattern() == "/get/[0-9]"
			contract.request.urlPath.serverValue == "/get/1"
			contract.request.urlPath.queryParameters.parameters.size() == 8
			QueryParameters queryParameters = contract.request.urlPath.queryParameters
			assertQueryParam(queryParameters, "limit", 10,
					MatchingStrategy.Type.EQUAL_TO, 20)
			assertQueryParam(queryParameters, "offset", 20,
					MatchingStrategy.Type.CONTAINS, 20)
			assertQueryParam(queryParameters, "sort", "name",
					MatchingStrategy.Type.EQUAL_TO, "name")
			assertQueryParam(queryParameters, "search", 55,
					MatchingStrategy.Type.NOT_MATCHING, (~/^[0-9]{2}$/).pattern())
			assertQueryParam(queryParameters, "age", 99,
					MatchingStrategy.Type.NOT_MATCHING, "^\\\\w*\$")
			assertQueryParam(queryParameters, "name", "John.Doe",
					MatchingStrategy.Type.MATCHING, "John.*")
			assertQueryParam(queryParameters, "hello", true,
					MatchingStrategy.Type.ABSENT, null)
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.request.bodyMatchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.request.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.request.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.request.bodyMatchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.request.bodyMatchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.request.bodyMatchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode().pattern()
			contract.request.bodyMatchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.request.bodyMatchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.request.bodyMatchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.request.bodyMatchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.request.bodyMatchers.jsonPathRegexMatchers[4].value() == patterns.number().pattern()
			contract.request.bodyMatchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.request.bodyMatchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.request.bodyMatchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean().pattern()
			contract.request.bodyMatchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.request.bodyMatchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.request.bodyMatchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.request.bodyMatchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.request.bodyMatchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.request.bodyMatchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.request.bodyMatchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.request.bodyMatchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.request.bodyMatchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.request.bodyMatchers.jsonPathRegexMatchers[9].path() == "\$.['key'].['complex.key']"
			contract.request.bodyMatchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.EQUALITY
			contract.request.cookies.entries.find { it.key == "foo" }.clientValue instanceof Pattern
			contract.request.cookies.entries.find { it.key == "bar" }.serverValue == new ExecutionProperty('equals($it)')
		and:
			contract.response.status.clientValue == 200
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.response.bodyMatchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.response.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.response.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.response.bodyMatchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode().pattern()
			contract.response.bodyMatchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.response.bodyMatchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.response.bodyMatchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.response.bodyMatchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.response.bodyMatchers.jsonPathRegexMatchers[4].value() == patterns.number().pattern()
			contract.response.bodyMatchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.response.bodyMatchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.response.bodyMatchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean().pattern()
			contract.response.bodyMatchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.response.bodyMatchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.response.bodyMatchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.response.bodyMatchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.response.bodyMatchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.response.bodyMatchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.response.bodyMatchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.response.bodyMatchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.response.bodyMatchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.response.bodyMatchers.jsonPathRegexMatchers[9].path() == '$.valueWithTypeMatch'
			contract.response.bodyMatchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[10].path() == '$.valueWithMin'
			contract.response.bodyMatchers.jsonPathRegexMatchers[10].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[10].minTypeOccurrence() == 1
			contract.response.bodyMatchers.jsonPathRegexMatchers[11].path() == '$.valueWithMax'
			contract.response.bodyMatchers.jsonPathRegexMatchers[11].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[11].maxTypeOccurrence() == 3
			contract.response.bodyMatchers.jsonPathRegexMatchers[12].path() == '$.valueWithMinMax'
			contract.response.bodyMatchers.jsonPathRegexMatchers[12].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[12].minTypeOccurrence() == 1
			contract.response.bodyMatchers.jsonPathRegexMatchers[12].maxTypeOccurrence() == 3
			contract.response.bodyMatchers.jsonPathRegexMatchers[13].path() == '$.valueWithMinEmpty'
			contract.response.bodyMatchers.jsonPathRegexMatchers[13].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[13].minTypeOccurrence() == 0
			contract.response.bodyMatchers.jsonPathRegexMatchers[14].path() == '$.valueWithMaxEmpty'
			contract.response.bodyMatchers.jsonPathRegexMatchers[14].matchingType() == MatchingType.TYPE
			contract.response.bodyMatchers.jsonPathRegexMatchers[14].maxTypeOccurrence() == 0
			contract.response.bodyMatchers.jsonPathRegexMatchers[15].path() == '$.duck'
			contract.response.bodyMatchers.jsonPathRegexMatchers[15].matchingType() == MatchingType.COMMAND
			contract.response.bodyMatchers.jsonPathRegexMatchers[15].value() == new ExecutionProperty('assertThatValueIsANumber($it)')
	}

	protected Object assertQueryParam(QueryParameters queryParameters, String queryParamName, Object serverValue,
									  MatchingStrategy.Type clientType, Object clientValue) {
		if (clientType == MatchingStrategy.Type.ABSENT) {
			return ! queryParameters.parameters.find { it.name == queryParamName}
		}
		return queryParameters.parameters.find { it.name == queryParamName &&
				it.serverValue == serverValue &&
				((MatchingStrategy) it.clientValue).type == clientType &&
				((MatchingStrategy) it.clientValue).clientValue == clientValue  }
	}

	@Issue("#604")
	def "should convert YAML with Message matchers to DSL"() {
		given:
			assert converter.isAccepted(ymlMessagingMatchers)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMessagingMatchers)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			RegexPatterns patterns = new RegexPatterns()
			contract.input.messageHeaders.entries.find { it.name == "contentType" &&
					((Pattern) it.clientValue).pattern == "application/json.*" && it.serverValue == "application/json" }
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.input.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.input.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.input.bodyMatchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.input.bodyMatchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.input.bodyMatchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode().pattern()
			contract.input.bodyMatchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.input.bodyMatchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.input.bodyMatchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.input.bodyMatchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.input.bodyMatchers.jsonPathRegexMatchers[4].value() == patterns.number().pattern()
			contract.input.bodyMatchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.input.bodyMatchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.input.bodyMatchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean().pattern()
			contract.input.bodyMatchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.input.bodyMatchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.input.bodyMatchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.input.bodyMatchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.input.bodyMatchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.input.bodyMatchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.input.bodyMatchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.input.bodyMatchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.input.bodyMatchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.input.bodyMatchers.jsonPathRegexMatchers[9].path() == "\$.['key'].['complex.key']"
			contract.input.bodyMatchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.EQUALITY
		and:
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.duck'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].value() == '[0-9]{3}'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.duck'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.EQUALITY
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[2].path() == '$.alpha'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[2].matchingType() == MatchingType.REGEX
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[2].value() == patterns.onlyAlphaUnicode().pattern()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[3].path() == '$.alpha'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[3].matchingType() == MatchingType.EQUALITY
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[4].path() == '$.number'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[4].matchingType() == MatchingType.REGEX
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[4].value() == patterns.number().pattern()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[5].path() == '$.aBoolean'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[5].matchingType() == MatchingType.REGEX
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[5].value() == patterns.anyBoolean().pattern()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[6].path() == '$.date'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[6].matchingType() == MatchingType.DATE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[6].value() == patterns.isoDate()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[7].path() == '$.dateTime'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[7].matchingType() == MatchingType.TIMESTAMP
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[7].value() == patterns.isoDateTime()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[8].path() == '$.time'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[8].matchingType() == MatchingType.TIME
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[8].value() == patterns.isoTime()
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[9].path() == '$.valueWithTypeMatch'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[9].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[10].path() == '$.valueWithMin'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[10].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[10].minTypeOccurrence() == 1
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[11].path() == '$.valueWithMax'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[11].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[11].maxTypeOccurrence() == 3
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[12].path() == '$.valueWithMinMax'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[12].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[12].minTypeOccurrence() == 1
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[12].maxTypeOccurrence() == 3
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[13].path() == '$.valueWithMinEmpty'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[13].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[13].minTypeOccurrence() == 0
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[14].path() == '$.valueWithMaxEmpty'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[14].matchingType() == MatchingType.TYPE
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[14].maxTypeOccurrence() == 0
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[15].path() == '$.duck'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[15].matchingType() == MatchingType.COMMAND
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[15].value() == new ExecutionProperty('assertThatValueIsANumber($it)')
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

	def "should convert YAML with REST with multipart"() {
		given:
			URL ymlUrl = YamlContractConverterSpec.getResource("/yml/contract_multipart.yml")
			File yml = new File(ymlUrl.toURI())
		and:
			assert converter.isAccepted(yml)
		when:
			Collection<Contract> contracts = converter.convertFrom(yml)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			RegexPatterns patterns = new RegexPatterns()
			def stubSide = MapConverter.getStubSideValues(contract.request.multipart)
			stubSide.formParameter.pattern() == ".+"
			stubSide.someBooleanParameter.pattern() == patterns.anyBoolean().pattern()
			def testSide = MapConverter.getTestSideValues(contract.request.multipart)
			testSide.formParameter == '"formParameterValue"'
			testSide.someBooleanParameter == "true"
			((NamedProperty) testSide.file).name.serverValue == "filename.csv"
			((NamedProperty) testSide.file).name.clientValue.pattern() == patterns.nonEmpty().pattern()
			((NamedProperty) testSide.file).value.serverValue == "file content"
			((NamedProperty) testSide.file).value.clientValue.pattern() == patterns.nonEmpty().pattern()
		and:
			contract.response.status.serverValue == 200
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
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.bar'
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.input.bodyMatchers.jsonPathRegexMatchers[0].value() == 'bar'
		and:
			contract.outputMessage.assertThat.toString() == "baz()"
			contract.outputMessage.headers.entries.find { it.name == "foo2" &&
					((Pattern) it.serverValue).pattern == "bar" && it.clientValue == "bar" }
			contract.outputMessage.headers.entries.find { it.name == "foo3" &&
					((ExecutionProperty) it.serverValue).insertValue('foo') == "andMeToo(foo)" }
			contract.outputMessage.headers.entries.find { it.name == "fooRes" &&
					it.clientValue == "baz" }
			contract.outputMessage.body.clientValue == [foo2: "bar", foo3: "baz"]
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].path() == '$.foo2'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].matchingType() == MatchingType.REGEX
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[0].value() == 'bar'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[1].path() == '$.foo3'
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[1].matchingType() == MatchingType.COMMAND
			contract.outputMessage.bodyMatchers.jsonPathRegexMatchers[1].value() == new ExecutionProperty('executeMe($it)')
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

	def "should convert YAML with HTTP binary body to DSL"() {
		given:
			assert converter.isAccepted(ymlBytes)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlBytes)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.request.body.clientValue instanceof FromFileProperty
			((FromFileProperty) contract.request.body.clientValue).type == byte[]
		and:
			contract.response.body.clientValue instanceof FromFileProperty
			((FromFileProperty) contract.response.body.clientValue).type == byte[]
	}

	def "should convert YAML with messaging binary body to DSL"() {
		given:
			assert converter.isAccepted(ymlMessagingBytes)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlMessagingBytes)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.input.messageBody.clientValue instanceof FromFileProperty
			((FromFileProperty) contract.input.messageBody.clientValue).type == byte[]
		and:
			contract.outputMessage.body.clientValue instanceof FromFileProperty
			((FromFileProperty) contract.outputMessage.body.clientValue).type == byte[]
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

	def "should dump yml as string"() {
		given:
			String expectedYaml1 = '''\
---
request:
  method: "POST"
  url: "/users/1"
  urlPath: null
  queryParameters: {}
  headers: {}
  cookies: {}
  body: null
  bodyFromFile: null
  bodyFromFileAsBytes: null
  matchers:
    url: null
    body: []
    headers: []
    queryParameters: []
    cookies: []
    multipart: null
  multipart: null
response:
  status: 200
  headers: {}
  cookies: {}
  body: null
  bodyFromFile: null
  bodyFromFileAsBytes: null
  matchers:
    body: []
    headers: []
    cookies: []
  async: null
  fixedDelayMilliseconds: null
input: null
outputMessage: null
description: null
label: null
name: "post1"
priority: null
ignored: false
'''
			String expectedYaml2 = '''\
---
request:
  method: "POST"
  url: "/users/2"
  urlPath: null
  queryParameters: {}
  headers: {}
  cookies: {}
  body: null
  bodyFromFile: null
  bodyFromFileAsBytes: null
  matchers:
    url: null
    body: []
    headers: []
    queryParameters: []
    cookies: []
    multipart: null
  multipart: null
response:
  status: 200
  headers: {}
  cookies: {}
  body: null
  bodyFromFile: null
  bodyFromFileAsBytes: null
  matchers:
    body: []
    headers: []
    cookies: []
  async: null
  fixedDelayMilliseconds: null
input: null
outputMessage: null
description: null
label: null
name: "post2"
priority: null
ignored: false
'''
		when:
			Map<String, byte[]> strings = converter.store([
			        new YamlContract(
							name: "post1",
							request: new YamlContract.Request(method: "POST", url: "/users/1"),
							response: new YamlContract.Response(status: 200)
					),new YamlContract(
							name: "post2",
							request: new YamlContract.Request(method: "POST", url: "/users/2"),
							response: new YamlContract.Response(status: 200)
					),
			])
		then:
			strings.size() == 2
			new String(strings["post1.yml"]).trim() == expectedYaml1.trim()
			new String(strings["post2.yml"]).trim() == expectedYaml2.trim()
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

	def "should convert HTTP DSL to YAML"() {
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
					cookies {
						cookie(foo: value(c("client"), p("server")))
						cookie("bar", value(c("client"), p("server")))
					}
					body([foo: "bar"])
				}
				response {
					fixedDelayMilliseconds 1000
					status(200)
					headers {
						header("foo2", "bar")
					}
					cookies {
						cookie(foo: value(c("client"), p("server")))
						cookie("bar", value(c("client"), p("server")))
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
			yamlContract.request.cookies.find { it.key == "bar" && it.value == "server" }
			yamlContract.request.cookies.find { it.key == "foo" && it.value == "server" }
			yamlContract.request.body == [foo: "bar"]
			yamlContract.response.status == 200
			yamlContract.response.headers.find { it.key == "foo2" && it.value == "bar" }
			yamlContract.response.body == [foo2: "bar"]
			yamlContract.response.cookies.find { it.key == "foo" && it.value == "client" }
			yamlContract.response.cookies.find { it.key == "bar" && it.value == "client" }
			yamlContract.response.fixedDelayMilliseconds == 1000
	}

	def "should convert multiple messaging DSLs to YAML"() {
		given:
			assert converter.isAccepted(ymlMessagingMethod)
		and:
			List<Contract> contracts = [Contract.make {
				input {
					description("Some description")
					label("some_label")
					triggeredBy("bookReturnedTriggered()")
				}
				outputMessage {
					sentTo("output")
					body([bookName: "foo"])
					headers {
						header("BOOK-NAME", "foo")
					}
				}
			},Contract.make {
				input {
					description("Some description2")
					label("some_label2")
					triggeredBy("bookReturnedTriggered()2")
				}
				outputMessage {
					sentTo("output2")
					body([bookName2: "foo"])
					headers {
						header("BOOK-NAME2", "foo")
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 2
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.description == "Some description"
			yamlContract.label == "some_label"
			yamlContract.input.triggeredBy == "bookReturnedTriggered()"
			yamlContract.outputMessage.sentTo == "output"
			yamlContract.outputMessage.body == [bookName: "foo"]
			yamlContract.outputMessage.headers == ["BOOK-NAME": "foo"]
		and:
			YamlContract yamlContract2 = yamlContracts.last()
			yamlContract2.description == "Some description2"
			yamlContract2.label == "some_label2"
			yamlContract2.input.triggeredBy == "bookReturnedTriggered()2"
			yamlContract2.outputMessage.sentTo == "output2"
			yamlContract2.outputMessage.body == [bookName2: "foo"]
			yamlContract2.outputMessage.headers == ["BOOK-NAME2": "foo"]
	}

	def "should convert Messaging DSL with input triggered by method to YAML"() {
		given:
			assert converter.isAccepted(ymlMessagingMethod)
		and:
			List<Contract> contracts = [Contract.make {
				input {
					description("Some description")
					label("some_label")
					triggeredBy("bookReturnedTriggered()")
				}
				outputMessage {
					sentTo("output")
					body([bookName: "foo"])
					headers {
						header("BOOK-NAME", "foo")
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.description == "Some description"
			yamlContract.label == "some_label"
			yamlContract.input.triggeredBy == "bookReturnedTriggered()"
			yamlContract.outputMessage.sentTo == "output"
			yamlContract.outputMessage.body == [bookName: "foo"]
			yamlContract.outputMessage.headers == ["BOOK-NAME": "foo"]
	}

	def "should convert Messaging DSL with input and output message to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				input {
					messageFrom("jms:input")
					messageBody([bookName: 'foo'])
					messageHeaders {
						header("sample", "header")
					}
				}
				outputMessage {
					sentTo("output")
					body([bookName: "foo"])
					headers {
						header("BOOK-NAME", "foo")
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.input.messageFrom == "jms:input"
			yamlContract.input.messageBody == [bookName: 'foo']
			yamlContract.input.messageHeaders == ["sample": "header"]
			yamlContract.outputMessage.sentTo == "output"
			yamlContract.outputMessage.body == [bookName: "foo"]
			yamlContract.outputMessage.headers == ["BOOK-NAME": "foo"]
	}

	def "should convert Messaging DSL with only input message to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				input {
					messageFrom("jms:input")
					messageBody([bookName: 'foo'])
					messageHeaders {
						header("sample", "header")
					}
					assertThat("bookWasDeleted()")
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.input.messageFrom == "jms:input"
			yamlContract.input.messageBody == [bookName: 'foo']
			yamlContract.input.messageHeaders == ["sample": "header"]
			yamlContract.input.assertThat == "bookWasDeleted()"
	}

	def "should convert Messaging with a message DSL to YAML"() {
		given:
			assert converter.isAccepted(ymlMessagingMatchers)
		and:
			List<Contract> contracts = [Contract.make {
				name("fooo")
				label("card_rejected")
				ignored()
				input {
					messageFrom("input")
					messageBody([
							duck: 123,
							alpha: "abc",
							number: 123,
							aBoolean: true,
							date: "2017-01-01",
							dateTime: "2017-01-01T01:23:45",
							time: "01:02:34",
							valueWithoutAMatcher: "foo",
							valueWithTypeMatch: "string",
							key: ["complex.key": 'foo']
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
					messageHeaders {
						header("sample", $(c(regex("foo.*")), p("foo")))
						messagingContentType(applicationJson())
					}
				}
				outputMessage {
					sentTo("channel")
					body([duck: 123,
						  alpha: "abc",
						  number: 123,
						  aBoolean: true,
						  date: "2017-01-01",
						  dateTime: "2017-01-01T01:23:45",
						  time: "01:02:34",
						  valueWithoutAMatcher: "foo",
						  valueWithTypeMatch: "string",
						  valueWithMin: [1, 2, 3],
						  valueWithMax: [1, 2, 3],
						  valueWithMinMax: [1, 2, 3],
						  valueWithMinEmpty: [],
						  valueWithMaxEmpty: [],
						  key: ['complex.key' : 'foo'],
						  nullValue: null
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
						jsonPath('$.positiveInteger', byRegex(positiveInt()))
						jsonPath('$.integer', byRegex(anInteger()))
						jsonPath('$.double', byRegex(aDouble()))
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
					headers {
						messagingContentType(applicationJson())
						header('Some-Header', $(c('someValue'), p(regex('[a-zA-Z]{9}'))))
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.name == "fooo"
			yamlContract.ignored == true
			yamlContract.label == "card_rejected"
			yamlContract.input.messageFrom == "input"
			yamlContract.input.messageBody == [
					duck: 123,
					alpha: "abc",
					number: 123,
					aBoolean: true,
					date: "2017-01-01",
					dateTime: "2017-01-01T01:23:45",
					time: "01:02:34",
					valueWithoutAMatcher: "foo",
					valueWithTypeMatch: "string",
					key: ["complex.key": 'foo']
			]
			yamlContract.input.messageHeaders == [
					sample: 'foo',
					contentType: "application/json"
			]
			yamlContract.input.matchers.headers == [
			        new YamlContract.KeyValueMatcher(
							key: "sample", regex: "foo.*")
			]
			yamlContract.input.matchers.body == [
					new YamlContract.BodyStubMatcher(
							path: '$.duck',
							type: YamlContract.StubMatcherType.by_regex,
							value: "[0-9]{3}"),
					new YamlContract.BodyStubMatcher(
							path: '$.duck',
							type: YamlContract.StubMatcherType.by_equality),
					new YamlContract.BodyStubMatcher(
							path: '$.alpha',
							type: YamlContract.StubMatcherType.by_regex,
							predefined: YamlContract.PredefinedRegex.only_alpha_unicode),
					new YamlContract.BodyStubMatcher(
							path: '$.alpha',
							type: YamlContract.StubMatcherType.by_equality),
					new YamlContract.BodyStubMatcher(
							path: '$.number',
							type: YamlContract.StubMatcherType.by_regex,
							predefined: YamlContract.PredefinedRegex.number),
					new YamlContract.BodyStubMatcher(
							path: '$.aBoolean',
							type: YamlContract.StubMatcherType.by_regex,
							predefined: YamlContract.PredefinedRegex.any_boolean),
					new YamlContract.BodyStubMatcher(
							path: '$.date',
							type: YamlContract.StubMatcherType.by_date),
					new YamlContract.BodyStubMatcher(
							path: '$.dateTime',
							type: YamlContract.StubMatcherType.by_timestamp),
					new YamlContract.BodyStubMatcher(
							path: '$.time',
							type: YamlContract.StubMatcherType.by_time),
					new YamlContract.BodyStubMatcher(
							path: "\$.['key'].['complex.key']",
							type: YamlContract.StubMatcherType.by_equality),
			]
			yamlContract.outputMessage.sentTo == "channel"
			yamlContract.outputMessage.body == [duck: 123,
												alpha: "abc",
												number: 123,
												aBoolean: true,
												date: "2017-01-01",
												dateTime: "2017-01-01T01:23:45",
												time: "01:02:34",
												valueWithoutAMatcher: "foo",
												valueWithTypeMatch: "string",
												valueWithMin: [1, 2, 3],
												valueWithMax: [1, 2, 3],
												valueWithMinMax: [1, 2, 3],
												valueWithMinEmpty: [],
												valueWithMaxEmpty: [],
												key: ['complex.key' : 'foo'],
												nullValue: null
												]
			yamlContract.outputMessage.headers == [
					"contentType": "application/json",
					"Some-Header": "someValue"
			]
			yamlContract.outputMessage.matchers.headers == [
					new YamlContract.TestHeaderMatcher(
							key: "Content-Type", regex: "application/json.*")
			]
			yamlContract.outputMessage.matchers.body == [
					new YamlContract.BodyTestMatcher(
							path: '$.duck',
							type: YamlContract.TestMatcherType.by_regex,
							value: "[0-9]{3}"),
					new YamlContract.BodyTestMatcher(
							path: '$.duck',
							type: YamlContract.TestMatcherType.by_equality),
					new YamlContract.BodyTestMatcher(
							path: '$.alpha',
							type: YamlContract.TestMatcherType.by_regex,
							value: '[\\p{L}]*'),
					new YamlContract.BodyTestMatcher(
							path: '$.alpha',
							type: YamlContract.TestMatcherType.by_equality),
					new YamlContract.BodyTestMatcher(
							path: '$.number',
							type: YamlContract.TestMatcherType.by_regex,
							value: '-?(\\d*\\.\\d+|\\d+)'),
					new YamlContract.BodyTestMatcher(
							path: '$.positiveInteger',
							type: YamlContract.TestMatcherType.by_regex,
							value: '([1-9]\\d*)'),
					new YamlContract.BodyTestMatcher(
							path: '$.integer',
							type: YamlContract.TestMatcherType.by_regex,
							value: '-?(\\d+)'),
					new YamlContract.BodyTestMatcher(
							path: '$.double',
							type: YamlContract.TestMatcherType.by_regex,
							value: '-?(\\d*\\.\\d+)'),
					new YamlContract.BodyTestMatcher(
							path: '$.aBoolean',
							type: YamlContract.TestMatcherType.by_regex,
							value: '(true|false)'),
					new YamlContract.BodyTestMatcher(
							path: '$.date',
							type: YamlContract.TestMatcherType.by_regex,
							value: '(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])'),
					new YamlContract.BodyTestMatcher(
							path: '$.dateTime',
							type: YamlContract.TestMatcherType.by_regex,
							value: '([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])'),
					new YamlContract.BodyTestMatcher(
							path: '$.time',
							type: YamlContract.TestMatcherType.by_regex,
							value: '(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])'),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithTypeMatch',
							type: YamlContract.TestMatcherType.by_type),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithMin',
							type: YamlContract.TestMatcherType.by_type,
							minOccurrence: 1),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithMax',
							type: YamlContract.TestMatcherType.by_type,
							maxOccurrence: 3),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithMinMax',
							type: YamlContract.TestMatcherType.by_type,
							minOccurrence: 1,
							maxOccurrence: 3),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithMinEmpty',
							type: YamlContract.TestMatcherType.by_type,
							minOccurrence: 0),
					new YamlContract.BodyTestMatcher(
							path: '$.valueWithMaxEmpty',
							type: YamlContract.TestMatcherType.by_type,
							maxOccurrence: 0),
					new YamlContract.BodyTestMatcher(
							path: '$.duck',
							type: YamlContract.TestMatcherType.by_command,
							value: 'assertThatValueIsANumber($it)'),
					new YamlContract.BodyTestMatcher(
							path: "\$.['key'].['complex.key']",
							type: YamlContract.TestMatcherType.by_equality),
					new YamlContract.BodyTestMatcher(
							path: '$.nullValue',
							type: YamlContract.TestMatcherType.by_null),
			]
	}
}