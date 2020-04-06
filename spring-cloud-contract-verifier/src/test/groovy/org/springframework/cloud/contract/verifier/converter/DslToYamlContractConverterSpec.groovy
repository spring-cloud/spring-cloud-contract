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

package org.springframework.cloud.contract.verifier.converter


import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @author Olga Maciaszek-Sharma
 */
class DslToYamlContractConverterSpec extends Specification {

	String xmlContractBody = '''
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<list>
<elem>abc</elem>
<elem>def</elem>
<elem>ghi</elem>
</list>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
<key><complex>foo</complex></key>
</test>
'''

	YamlContractConverter converter = new YamlContractConverter()

	def "should convert rest DSL to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				request {
					method 'GET'
					urlPath '/get'
					body([
							duck                : 123,
							alpha               : "abc",
							number              : 123,
							aBoolean            : true,
							date                : "2017-01-01",
							dateTime            : "2017-01-01T01:23:45",
							time                : "01:02:34",
							valueWithoutAMatcher: "foo",
							valueWithTypeMatch  : "string",
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
					headers {
						header("sample", $(c(regex('foo.*')), p('foo')))
						header("Content-Type", applicationJson())
					}
				}
				response {
					status OK()
					body([
							duck                 : 123,
							alpha                : "abc",
							number               : 123,
							positiveInteger      : 1234567890,
							negativeInteger      : -1234567890,
							positiveDecimalNumber: 123.4567890,
							negativeDecimalNumber: -123.4567890,
							aBoolean             : true,
							date                 : "2017-01-01",
							dateTime             : "2017-01-01T01:23:45",
							time                 : "01:02:34",
							valueWithoutAMatcher : "foo",
							valueWithTypeMatch   : "string",
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
						contentType(applicationJson())
						header('Some-Header', $(c('someValue'), p(regex('[a-zA-Z]{9}'))))
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.method == "GET"
			yamlContract.request.urlPath == "/get"
			yamlContract.request.body == [
					duck                : 123,
					alpha               : "abc",
					number              : 123,
					aBoolean            : true,
					date                : "2017-01-01",
					dateTime            : "2017-01-01T01:23:45",
					time                : "01:02:34",
					valueWithoutAMatcher: "foo",
					valueWithTypeMatch  : "string",
					key                 : ["complex.key": 'foo']
			]
			yamlContract.request.headers == [
					sample        : 'foo',
					"Content-Type": "application/json"
			]
			yamlContract.request.matchers.headers == [
					new YamlContract.KeyValueMatcher(
							key: "sample", regex: "foo.*", regexType: YamlContract.RegexType.as_string)
			]
			yamlContract.request.matchers.body == [
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
							value: "[\\p{L}]*"),
					new YamlContract.BodyStubMatcher(
							path: '$.alpha',
							type: YamlContract.StubMatcherType.by_equality),
					new YamlContract.BodyStubMatcher(
							path: '$.number',
							type: YamlContract.StubMatcherType.by_regex,
							value: "-?(\\d*\\.\\d+|\\d+)"),
					new YamlContract.BodyStubMatcher(
							path: '$.aBoolean',
							type: YamlContract.StubMatcherType.by_regex,
							value: "(true|false)"),
					new YamlContract.BodyStubMatcher(
							path: '$.date',
							type: YamlContract.StubMatcherType.by_date,
							value: "(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])"),
					new YamlContract.BodyStubMatcher(
							path: '$.dateTime',
							type: YamlContract.StubMatcherType.by_timestamp,
							value: "([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])"),
					new YamlContract.BodyStubMatcher(
							path: '$.time',
							type: YamlContract.StubMatcherType.by_time,
							value: "(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])"),
					new YamlContract.BodyStubMatcher(
							path: "\$.['key'].['complex.key']",
							type: YamlContract.StubMatcherType.by_equality),
			]
			yamlContract.response.status == 200
			yamlContract.response.body == [duck                 : 123,
										   alpha                : "abc",
										   number               : 123,
										   aBoolean             : true,
										   date                 : "2017-01-01",
										   dateTime             : "2017-01-01T01:23:45",
										   time                 : "01:02:34",
										   positiveInteger      : 1234567890,
										   negativeInteger      : -1234567890,
										   positiveDecimalNumber: 123.4567890,
										   negativeDecimalNumber: -123.4567890,
										   valueWithoutAMatcher : "foo",
										   valueWithTypeMatch   : "string",
										   valueWithMin         : [1, 2, 3],
										   valueWithMax         : [1, 2, 3],
										   valueWithMinMax      : [1, 2, 3],
										   valueWithMinEmpty    : [],
										   valueWithMaxEmpty    : [],
										   key                  : ['complex.key': 'foo'],
										   nullValue            : null
			]
			yamlContract.response.headers == [
					"Content-Type": "application/json",
					"Some-Header" : "someValue"
			]
			yamlContract.response.matchers.headers == [
					new YamlContract.TestHeaderMatcher(
							key: "Content-Type", regex: "application/json.*", regexType: YamlContract.RegexType.as_string),
					new YamlContract.TestHeaderMatcher(
							key: "Some-Header", regex: "[a-zA-Z]{9}", regexType: YamlContract.RegexType.as_string)
			]
			yamlContract.response.matchers.body == [
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
							type: YamlContract.TestMatcherType.by_date,
							value: '(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])'),
					new YamlContract.BodyTestMatcher(
							path: '$.dateTime',
							type: YamlContract.TestMatcherType.by_timestamp,
							value: '([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])'),
					new YamlContract.BodyTestMatcher(
							path: '$.time',
							type: YamlContract.TestMatcherType.by_time,
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

	def "should convert rest DSL with dynamic entries to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				request { // (1)
					method 'PUT' // (2)
					urlPath('/fraudcheck') {
						queryParameters {
							parameter("foo", "bar")
							parameter("foo2", $(c(equalToJson('''{"foo":"bar"}''')), p("foo3")))
						}
					}
					body([ // (4)
						   "client.id": $(regex('[0-9]{10}')),
						   loanAmount : 99999
					])
					headers { // (5)
						contentType('application/json')
						header(authorization(), $(c('Bearer SOMETOKEN'), p(execute('authToken()'))))
					}
				}
				response { // (6)
					status OK() // (7)
					body([ // (8)
						   fraudCheckStatus  : "${value(regex("FRAUD"))}",
						   "rejection.reason": "Amount too high"
					])
					headers { // (9)
						contentType('application/json')
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.method == "PUT"
			yamlContract.request.urlPath == "/fraudcheck"
			yamlContract.request.queryParameters == [
					foo2: "foo3",
					foo : "bar"
			]
			yamlContract.request.body["client.id"] =~ /[0-9]{10}/
			yamlContract.request.body["loanAmount"] == 99999
			yamlContract.request.headers == [
					"Content-Type" : "application/json",
					"Authorization": 'Bearer SOMETOKEN'
			]
			yamlContract.request.matchers.headers == [
					new YamlContract.KeyValueMatcher(
							key: "Content-Type", regex: "application/json.*", regexType: YamlContract.RegexType.as_string)
			]
			yamlContract.request.matchers.body == [
					new YamlContract.BodyStubMatcher(
							path: "\$.['client.id']",
							type: YamlContract.StubMatcherType.by_regex,
							value: "[0-9]{10}",
							regexType: YamlContract.RegexType.as_string),
			]
			yamlContract.request.matchers.queryParameters == [
					new YamlContract.QueryParameterMatcher(key: "foo2", type: YamlContract.MatchingType.equal_to_json, value: '''{"foo":"bar"}'''),
			]
			yamlContract.response.status == 200
			yamlContract.response.body == [fraudCheckStatus  : "FRAUD",
										   "rejection.reason": "Amount too high"]
			yamlContract.response.headers == [
					"Content-Type": "application/json"
			]
			yamlContract.response.matchers.headers == [
					new YamlContract.TestHeaderMatcher(
							key: "Content-Type", regex: "application/json.*", regexType: YamlContract.RegexType.as_string)
			]
			yamlContract.response.matchers.body == [
					new YamlContract.BodyTestMatcher(
							path: "\$.['fraudCheckStatus']",
							type: YamlContract.TestMatcherType.by_regex,
							value: "FRAUD",
							regexType: YamlContract.RegexType.as_string),
			]
	}

	def "should convert rest DSL with multipart entries to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				request {
					method "PUT"
					url "/multipart"
					headers {
						contentType('multipart/form-data;boundary=AaB03x')
					}
					multipart(
							// key (parameter name), value (parameter value) pair
							formParameter: $(c(regex('".+"')), p('"formParameterValue"')),
							someBooleanParameter: $(c(regex(anyBoolean())), p('true')),
							// a named parameter (e.g. with `file` name) that represents file with
							// `name` and `content`. You can also call `named("fileName", "fileContent")`
							file: named(
									// name of the file
									name: $(c(regex(nonEmpty())), p('filename.csv')),
									// content of the file
									content: $(c(regex(nonEmpty())), p('file content')),
									// content type for the part
									contentType: $(c(regex(nonEmpty())), p('application/json')))
					)
				}
				response {
					status OK()
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.method == "PUT"
			yamlContract.request.url == "/multipart"
			yamlContract.request.multipart == new YamlContract.Multipart(
					params: [
							formParameter       : '"formParameterValue"',
							someBooleanParameter: 'true',
					],
					named: [new YamlContract.Named(paramName: "file",
							fileName: 'filename.csv',
							fileContent: 'file content',
							contentType: 'application/json')]
			)
			yamlContract.request.headers == [
					"Content-Type": "multipart/form-data;boundary=AaB03x"
			]
			yamlContract.request.matchers.multipart.params == [
					new YamlContract.KeyValueMatcher(
							key: "formParameter",
							regex: '".+"',
							regexType: YamlContract.RegexType.as_string
					),
					new YamlContract.KeyValueMatcher(
							key: "someBooleanParameter",
							regex: '(true|false)',
							regexType: YamlContract.RegexType.as_string
					)
			]
			yamlContract.request.matchers.multipart.named == [
					new YamlContract.MultipartNamedStubMatcher(
							paramName: "file",
							fileName: new YamlContract.ValueMatcher(
									regex: "[\\S\\s]+"
							),
							fileContent: new YamlContract.ValueMatcher(
									regex: "[\\S\\s]+"
							),
							contentType: new YamlContract.ValueMatcher(
									regex: "[\\S\\s]+"
							)
					)
			]
			yamlContract.response.status == 200
	}

	def "should convert REST XML DSL to YAML"() {
		given:
			List<Contract> contracts = [Contract.make {
				request {
					method 'GET'
					url '/get'
					headers {
						contentType(applicationXml())
					}
					body """
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<list>
<elem>abc</elem>
<elem>def</elem>
<elem>ghi</elem>
</list>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
<key><complex>foo</complex></key>
</test>"""
					bodyMatchers {
						xPath('/test/duck/text()', byRegex("[0-9]{3}"))
					}
				}
				response {
					status(OK())
					body """
<test>
<duck type='xtype'>123</duck>
<alpha>abc</alpha>
<list>
<elem>abc</elem>
<elem>def</elem>
<elem>ghi</elem>
</list>
<number>123</number>
<aBoolean>true</aBoolean>
<date>2017-01-01</date>
<dateTime>2017-01-01T01:23:45</dateTime>
<time>01:02:34</time>
<valueWithoutAMatcher>foo</valueWithoutAMatcher>
<valueWithTypeMatch>string</valueWithTypeMatch>
<key><complex>foo</complex></key>
</test>"""
					bodyMatchers {
						xPath('/test/duck/xxx', byNull())
					}
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.method == 'GET'
			yamlContract.request.url == '/get'
			yamlContract.request.body.replaceAll("\n", "")
									 .replaceAll(' ', '') == xmlContractBody.replaceAll("\n", "")
																			.replaceAll(' ', '')
			yamlContract.request.headers == [
					"Content-Type": "application/xml"
			]
			yamlContract.request.matchers.body == [
					new YamlContract.BodyStubMatcher(
							path: '/test/duck/text()',
							type: YamlContract.StubMatcherType.by_regex,
							value: '[0-9]{3}'),
			]
			yamlContract.response.status == 200
			yamlContract.response.body.replaceAll("\n", "")
									  .replaceAll(' ', '') == xmlContractBody.replaceAll("\n", "")
																			 .replaceAll(' ', '')
			yamlContract.response.matchers.body == [
					new YamlContract.BodyTestMatcher(
							path: '/test/duck/xxx',
							type: YamlContract.TestMatcherType.by_null)
			]
	}
}
