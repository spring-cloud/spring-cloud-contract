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

package org.springframework.cloud.contract.verifier.builder

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.dsl.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class MockMvcMethodBodyBuilderWithMatchersSpec extends Specification implements WireMockStubVerifier {

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Issue('#185')
	def "should allow to set dynamic values via stub / test matchers for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body([
							duck: 123,
							alpha: "abc",
							number: 123,
							aBoolean: true,
							date: "2017-01-01",
							dateTime: "2017-01-01T01:23:45",
							time: "01:02:34",
							valueWithoutAMatcher: "foo",
							valueWithTypeMatch: "string"
					])
					stubMatchers {
						jsonPath('$.duck', byRegex("[0-9]{3}"))
						jsonPath('$.alpha', byRegex(onlyAlphaUnicode()))
						jsonPath('$.number', byRegex(number()))
						jsonPath('$.aBoolean', byRegex(anyBoolean()))
						jsonPath('$.date', byDate())
						jsonPath('$.dateTime', byTimestamp())
						jsonPath('$.time', byTime())
					}
					headers {
						contentType(applicationJson())
					}
				}
				response {
					status 200
					body([
							duck: 123,
							alpha: "abc",
							number: 123,
							aBoolean: true,
							date: "2017-01-01",
							dateTime: "2017-01-01T01:23:45",
							time: "01:02:34",
							valueWithoutAMatcher: "foo",
							valueWithTypeMatch: "string"
					])
					testMatchers {
						jsonPath('$.duck', byRegex("[0-9]{3}"))
						jsonPath('$.alpha', byRegex(onlyAlphaUnicode()))
						jsonPath('$.number', byRegex(number()))
						jsonPath('$.aBoolean', byRegex(anyBoolean()))
						jsonPath('$.date', byDate())
						jsonPath('$.dateTime', byTimestamp())
						jsonPath('$.time', byTime())
						jsonPath('$.valueWithTypeMatch', byType())
					}
					headers {
						contentType(applicationJson())
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThat(parsedJson.read("' + rootElement + '.duck", String.class)).matches("[0-9]{3}")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.alpha", String.class)).matches("[\\\\p{L}]*")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.number", String.class)).matches("-?\\\\d*(\\\\.\\\\d+)?")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.aBoolean", String.class)).matches("(true|false)")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.date", String.class)).matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.dateTime", String.class)).matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThat(parsedJson.read("' + rootElement + '.time", String.class)).matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThat((Object) parsedJson.read("' + rootElement + '.valueWithTypeMatch")).isExactlyInstanceOf(java.lang.String.class)')
			!test.contains('cursor')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                    | methodBuilder                                                                                                                       | rootElement
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }                                             | '\\$'
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                                                              | '$'
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | '\\$'
			"JaxRsClientJUnitMethodBodyBuilder"                  | { org.springframework.cloud.contract.spec.Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | '$'
	}

}
