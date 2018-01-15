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

import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingType

/**
 * @author Marcin Grzejszczak
 */
class YamlContractConverterSpec extends Specification {

	URL ymlUrl = YamlContractConverterSpec.getResource("/yml/contract.yml")
	File ymlWithRest = new File(ymlUrl.toURI())
	URL ymlMsgUrl = YamlContractConverterSpec.getResource("/yml/contract_message.yml")
	File ymlMessaging = new File(ymlMsgUrl.toURI())
	YamlContractConverter converter = new YamlContractConverter()

	def "should convert YAML with REST to DSL"() {
		given:
			assert converter.isAccepted(ymlWithRest)
		when:
			Collection<Contract> contracts = converter.convertFrom(ymlWithRest)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.name == "some name"
			contract.priority == 8
			contract.ignored == true
			contract.request.url.clientValue == "/foo"
			contract.request.url.queryParameters.parameters[0].name == "a"
			contract.request.url.queryParameters.parameters[0].serverValue == "b"
			contract.request.url.queryParameters.parameters[1].name == "b"
			contract.request.url.queryParameters.parameters[1].serverValue == "c"
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

	def "should assert request headers when converting YAML to DSL"() {
		given:
			File yml = new File(YamlContractConverterSpec.getResource("/yml/contract_broken_request_headers.yml").toURI())
		and:
			assert converter.isAccepted(yml)
		when:
			converter.convertFrom(yml)
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Broken request headers! A header with key [foo] with value [bar] is not matched by regex [barrrr]")
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
			e.message.contains("Broken response headers! A header with key [foo2] with value [bar] is not matched by regex [barrrr]")
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
