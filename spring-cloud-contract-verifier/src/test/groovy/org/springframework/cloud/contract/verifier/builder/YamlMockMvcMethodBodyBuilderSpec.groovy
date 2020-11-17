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

package org.springframework.cloud.contract.verifier.builder

import org.junit.Rule
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import org.springframework.boot.test.system.OutputCaptureRule
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

/**
 * @author Jakub Kubrynski, codearte.io
 * @author Tim Ysewyn
 */
class YamlMockMvcMethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule
	OutputCaptureRule capture = new OutputCaptureRule()

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Shared
	GeneratedClassDataForMethod generatedClassDataForMethod = new GeneratedClassDataForMethod(
			new SingleTestGenerator.GeneratedClassData("foo", "bar", new File(".").toPath()), "method")

	@Shared
	SingleTestGenerator.GeneratedClassData generatedClassData =
			new SingleTestGenerator.GeneratedClassData("foo", "com.example", new File(".").toPath())

	def setup() {
		properties = new ContractVerifierConfigProperties(
				assertJsonSize: true
		)
	}

	private String singleTestGenerator(Contract contractDsl) {
		return new JavaTestGenerator() {
			@Override
			ClassBodyBuilder classBodyBuilder(BlockBuilder builder, GeneratedClassMetaData metaData, SingleMethodBuilder methodBuilder) {
				return super.classBodyBuilder(builder, metaData, methodBuilder).field(new Field() {
					@Override
					boolean accept() {
						return metaData.configProperties.testMode == TestMode.JAXRSCLIENT
					}

					@Override
					Field call() {
						builder.addLine("WebTarget webTarget")
						return this
					}
				})
			}
		}.buildClass(properties, [contractMetadata(contractDsl)], "foo", generatedClassData)
	}

	private ContractMetadata contractMetadata(Contract contractDsl) {
		return new ContractMetadata(new File(".").toPath(), false, 0, null, contractDsl)
	}

	static File textToFile(String text) {
		File temp = File.createTempFile("yaml", ".yml")
		temp.text = text
		return temp
	}

	static Contract fromYaml(String text) {
		return new YamlContractConverter().convertFrom(textToFile(text))[0]
	}

	def 'should generate assertions for simple response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "a"
    "property2": "b"
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue("#187")
	def "should generate assertions for null and boolean values with #methodBuilderName"() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "true"
    "property2": null
    "property3": false
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("true")""")
			test.contains("""assertThatJson(parsedJson).field("['property2']").isNull()""")
			test.contains("""assertThatJson(parsedJson).field("['property3']").isEqualTo(false)""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#79')
	def 'should generate assertions for simple response body constructed from map with a list with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "a"
    "property2":
      - "a" : "sth"
      - "b" : "sthElse"
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#79')
	@RestoreSystemProperties
	def 'should generate assertions for simple response body constructed from map with a list with #methodBuilderName with array size check'() {
		given:
			System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "a"
    "property2":
      - "a" : "sth"
      - "b" : "sthElse"
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").hasSize(2)""")
			test.contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#82')
	def 'should generate proper request when body constructed from map with a list #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
  body:
    items: 
      - "HOP"
response:
  status: 200
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder                                      | bodyString
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """.body('''{\"items\":[\"HOP\"]}''')"""
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                                                      | '.body("{\\"items\\":[\\"HOP\\"]}")'
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                                                      | '.body("{\\"items\\":[\\"HOP\\"]}")'
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                                                      | '.body("{\\"items\\":[\\"HOP\\"]}")'
	}

	@Issue('#88')
	def 'should generate proper request when body constructed from GString with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
  body: "property1=VAL1"
response:
  status: 200
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder                                      | bodyString
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """.body('''property1=VAL1''')"""
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }         | '.body("property1=VAL1")'
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }   | '.body("property1=VAL1")'
			"custom"          | { properties.testMode = TestMode.CUSTOM }          | '.body("property1=VAL1")'
	}

	@Issue('185')
	def 'should generate assertions for a response body containing map with integers as keys with #methodBuilderName'() {
		given:
			// YAML CAN'T HAVE INTEGER KEYS
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    property: 
      14: 0.0
      7: 0.0
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property']").field("['7']").isEqualTo(0.0)""")
			test.contains("""assertThatJson(parsedJson).field("['property']").field("['14']").isEqualTo(0.0)""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	def 'should generate assertions for array in response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body: '
  [
		{
		 "property1": "a"
		},
		{
		 "property2": "b"
		}
  ]'
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).array().contains("['property2']").isEqualTo("b")""")
			test.contains("""assertThatJson(parsedJson).array().contains("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	def 'should generate assertions for array inside response body element with #methodBuilderName'() {
		given:

			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body: '
    {
      "property1": [
        { "property2": "test1"},
        { "property3": "test2"}
      ]
    }'
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).array("['property1']").contains("['property2']").isEqualTo("test1")""")
			test.contains("""assertThatJson(parsedJson).array("['property1']").contains("['property3']").isEqualTo("test2")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	def 'should generate assertions for nested objects in response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body: '
  {
    "property1": "a",
    "property2": {"property3": "b"}
  }'
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).field("['property2']").field("['property3']").isEqualTo("b")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#1423')
	def 'should generate assertions for a response body containing an empty list with #methodBuilderName'() {
		given:
			String contract = """\
---
description: Returns an empty collection
request:
  method: GET
  urlPath: /url
response:
  status: 200
  body: []
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).isEmpty()""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"testng"          | { properties.testFramework = TestFramework.TESTNG }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
	}

	@Issue('#1423')
	def 'should generate assertions for a response body containing an empty map with #methodBuilderName'() {
		given:
			String contract = """\
---
description: Returns an empty map
request:
  method: GET
  urlPath: /url
response:
  status: 200
  body: {}
"""
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).isEmpty()""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"testng"          | { properties.testFramework = TestFramework.TESTNG }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
	}

	def 'should generate regex assertions for map objects in response body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "test"
response:
  status: 200
  body:
    property1: "a"
    property2: "123"
  matchers:
    body:
      - path: $.property2
        type: by_regex
        value: "[0-9]{3}"
        regexType: as_integer
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""\$.property2", String.class)).matches("[0-9]{3}")""")
			test.contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	def "should generate a call with an url path and query parameters with #methodBuilderName"() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  urlPath: "/users"
  queryParameters:
    'limit': "10"
    'offset': "20"
    'filter': "email"
    'sort': "name"
    'search': "55"
    'age': "99"
    'name': "Denis.Stepanov"
    'email': "bob@email.com"
  matchers:
    queryParameters:
      - key: limit
        type: equal_to
        value: "10"
      - key: offset
        type: containing
        value: "20"
      - key: sort
        type: equal_to
        value: "name"
      - key: sort
        type: not_matching
        value: "^/[0-9]{2}$"
      - key: age
        type: not_matching
        value: '^\\w*$'
      - key: name
        type: matching
        value: 'Denis.*'
      - key: hello
        type: absent
response:
  status: 200
  body:
    property1: "a"
    property2: "b"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''.queryParam("limit","10")''')
			test.contains('''.queryParam("offset","20")''')
			test.contains('''.queryParam("filter","email")''')
			test.contains('''.queryParam("sort","name")''')
			test.contains('''.queryParam("search","55")''')
			test.contains('''.queryParam("age","99")''')
			test.contains('''.queryParam("name","Denis.Stepanov")''')
			test.contains('''.queryParam("email","bob@email.com")''')
			test.contains('''.get("/users")''')
			test.contains('assertThatJson(parsedJson).field("[\'property1\']").isEqualTo("a")')
			test.contains('assertThatJson(parsedJson).field("[\'property2\']").isEqualTo("b")')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#169')
	def 'should generate a call with an url path and query parameters with url containing a pattern with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  urlPath: "/foo/123456"
  queryParameters:
    'limit': "10"
    'offset': "20"
    'filter': "email"
    'sort': "name"
    'search': "55"
    'age': "99"
    'name': "Denis.Stepanov"
    'email': "bob@email.com"
  matchers:
    url:
      regex: '/foo/[0-9]+'
    queryParameters:
      - key: limit
        type: equal_to
        value: "10"
      - key: offset
        type: containing
        value: "20"
      - key: sort
        type: equal_to
        value: "name"
      - key: sort
        type: not_matching
        value: "^/[0-9]{2}$"
      - key: age
        type: not_matching
        value: '^\\w*$'
      - key: name
        type: matching
        value: 'Denis.*'
      - key: hello
        type: absent
response:
  status: 200
  body:
    property1: "a"
    property2: "b"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''.queryParam("limit","10")''')
			test.contains('''.queryParam("offset","20")''')
			test.contains('''.queryParam("filter","email")''')
			test.contains('''.queryParam("sort","name")''')
			test.contains('''.queryParam("search","55")''')
			test.contains('''.queryParam("age","99")''')
			test.contains('''.queryParam("name","Denis.Stepanov")''')
			test.contains('''.queryParam("email","bob@email.com")''')
			test.contains('''.get("/foo/123456")''')
			test.contains('assertThatJson(parsedJson).field("[\'property1\']").isEqualTo("a")')
			test.contains('assertThatJson(parsedJson).field("[\'property2\']").isEqualTo("b")')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	def 'should generate test for empty body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "POST"
  url: "/ws/payments"
  body: ""
response:
  status: 406
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder                                      | bodyString
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | ".body('''''')"
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }         | '.body("")'
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }   | '.body("")'
			"custom"          | { properties.testMode = TestMode.CUSTOM }          | '.body("")'
	}

	def 'should generate test for String in response body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "POST"
  url: "test"
  body: ""
response:
  status: 200
  body: "test"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyDefinitionString)
			test.contains(bodyEvaluationString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | bodyDefinitionString                                   | bodyEvaluationString
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | 'String responseBody = response.body.asString()'        | "responseBody == 'test'"
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                 | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('113')
	def 'should generate regex test for String in response header with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "POST"
  url: "/partners/1000/users"
  headers:
    "Content-Type": "application/json"
  body:
    first_name: 'John'
    last_name: 'Smith'
    personal_id: '12345678901'
    phone_number: '500500500'
    invitation_token: '00fec7141bb94793bfe7ae1d0f39bda0'
    password: 'john'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
    url:
      regex: '/partners/[0-9]+/users'
response:
  status: 201
  headers:
    "Location": "http://localhost/partners/1000/users/1001"
  matchers:
    headers:
      - key: 'Location'
        regex: 'http://localhost/partners/[0-9]+/users/[0-9]+'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(headerEvaluationString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder                                      | headerEvaluationString
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | '''response.header("Location") ==~ java.util.regex.Pattern.compile('http://localhost/partners/[0-9]+/users/[0-9]+')'''
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                                                      | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                                                      | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                                                      | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
	}

	def 'should work with more complex stuff and jsonpaths with #methodBuilderName'() {
		given:
			String contract = '''\
---
priority: 10
request:
  method: "POST"
  url: "/validation/client"
  headers:
    "Content-Type": "application/json"
  body:
    bank_account_number: '0014282912345698765432161182'
    email: 'foo@bar.com'
    phone_number: '100299300'
    personal_id: 'ABC123456'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
    url:
      regex: '/partners/[0-9]+/users'
response:
  status: 201
  body:
    errors:
    - property: "bank_account_number"
      message: "incorrect_format"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("""assertThatJson(parsedJson).array("['errors']").contains("['property']").isEqualTo("bank_account_number")""")
			test.contains("""assertThatJson(parsedJson).array("['errors']").contains("['message']").isEqualTo("incorrect_format")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('72')
	def 'should make the execute method work with #methodBuilderName'() {
		given:
			String contract = '''\
---
priority: 10
request:
  method: "PUT"
  url: "/fraudcheck"
response:
  status: 201
  headers:
    "Location": null
  body:
    rejectionReason: "foo"
  matchers:
    body:
      - path: "$.rejectionReason"
        type: by_command
        value: assertThatRejectionReasonIsNull($it)
    headers:
      - key: "Location"
        command: assertThatLocationIsNull($it)
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			assertionStrings.each { String assertionString ->
				assert test.contains(assertionString)
			}
		where:
			methodBuilderName | methodBuilder | assertionStrings
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | ['''assertThatRejectionReasonIsNull(parsedJson.read("\\\$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                 | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
	}

	def "shouldn't generate unicode escape characters with #methodBuilderName"() {
		given:
			String contract = '''\
---
priority: 10
request:
  method: "PUT"
  url: "/v1/payments/e86df6f693de4b35ae648464c5b0dc09/енев"
  headers:
    "Content-Type": "application/json"
  body:
    client:
      first_name: "Пенева"
      last_name: "Пенева"
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
        regexType: as_string
    body:
      - path: $.first_name
        type: by_regex
        value: '[\\p{L}]*'
        regexType: as_string
      - path: $.last_name
        type: by_regex
        value: '[\\p{L}]*'
response:
  status: 201
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains("\\u041f")
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('180')
	def 'should generate proper test code when having multipart parameters with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "PUT"
  url: "/multipart"
  headers:
    "Content-Type": 'multipart/form-data;boundary=AaB03x'
  multipart:
    params: 
      formParameter: '"formParameterValue"'
      someBooleanParameter: 'true'
    named: 
      - paramName: "file"
        fileName: 'filename.csv'
        fileContent: 'file content'
        contentType: 'application/json'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'multipart/form-data;boundary=AaB03x.*'
    multipart:
      params:
        - key: formParameter
          regex: ".+"
        - key: someBooleanParameter
          predefined: any_boolean
      named:
        - paramName: file
          fileName:
            predefined: non_empty
          fileContent:
            predefined: non_empty
          contentType:
            predefined: non_empty
response:
  status: 201
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestStrings
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 """.param('formParameter', '''"formParameterValue"'''""",
												 """.param('someBooleanParameter', 'true')""",
												 """.multiPart('file', 'filename.csv', 'file content'.bytes, 'application/json')"""]
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.param("formParameter", "\\"formParameterValue\\"")',
												 '.param("someBooleanParameter", "true")',
												 '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json")']
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.param("formParameter", "\\"formParameterValue\\"")',
												 '.param("someBooleanParameter", "true")',
												 '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json")']

	}

	@Issue('546')
	def 'should generate test code when having multipart parameters with byte array #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "PUT"
  url: "/multipart"
  headers:
    "Content-Type": 'multipart/form-data;boundary=AaB03x'
  multipart:
    params: 
      formParameter: '"formParameterValue"'
      someBooleanParameter: 'true'
    named: 
      - paramName: "file"
        fileName: 'filename.csv'
        fileContent: "file content"
        contentType: 'application/json'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'multipart/form-data;boundary=AaB03x.*'
    multipart:
      params:
        - key: formParameter
          regex: ".+"
        - key: someBooleanParameter
          predefined: any_boolean
      named:
        - paramName: file
          fileName:
            predefined: non_empty
          fileContent:
            predefined: non_empty
          contentType:
            predefined: non_empty
response:
  status: 201
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestStrings
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 """.multiPart('file', 'filename.csv', 'file content'.bytes, 'application/json')"""]
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json")']
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json")']

	}

	@Issue('541')
	def 'should generate proper test code when having multipart parameters that use execute with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "PUT"
  url: "/multipart"
  headers:
    "Content-Type": 'multipart/form-data;boundary=AaB03x'
  multipart:
    params: 
      formParameter: '"formParameterValue"'
      someBooleanParameter: 'true'
    named: 
      - paramName: "file"
        fileNameCommand: 'toString()'
        fileContent: 'file content'
        contentType: 'application/json'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'multipart/form-data;boundary=AaB03x.*'
    multipart:
      params:
        - key: formParameter
          regex: ".+"
          regexType: as_string
        - key: someBooleanParameter
          predefined: any_boolean
          regexType: as_boolean
      named:
        - paramName: file
          fileName:
            predefined: non_empty
          fileContent:
            predefined: non_empty
          contentType:
            predefined: non_empty
response:
  status: 201
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestStrings
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 """.param('formParameter', '''"formParameterValue"'''""",
												 """.param('someBooleanParameter', 'true')""",
												 """.multiPart('file', toString(), 'file content'.bytes, 'application/json')"""]
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.param("formParameter", "\\"formParameterValue\\"")',
												 '.param("someBooleanParameter", "true")',
												 '.multiPart("file", toString(), "file content".getBytes(), "application/json")']
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
												 '.param("formParameter", "\\"formParameterValue\\"")',
												 '.param("someBooleanParameter", "true")',
												 '.multiPart("file", toString(), "file content".getBytes(), "application/json")']

	}

	@Issue('#216')
	def 'should parse JSON with arrays using Spock'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/auth/oauth/check_token"
response:
  status: 200
  body:
    authorities:
     - ROLE_ADMIN
  matchers:
    body:
      - path: $.authorities[0]
        type: by_regex
        value: '^[a-zA-Z0-9_\\- ]+$'
        regexType: as_string
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThat(parsedJson.read("\\$.authorities[0]", String.class)).matches("^[a-zA-Z0-9_\\\\- ]+\\$")''')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic("spock", test)
	}

	@Issue('#216')
	def 'should parse JSON with arrays using JUnit'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/auth/oauth/check_token"
response:
  status: 200
  body:
    authorities:
     - ROLE_ADMIN
  matchers:
    body:
      - path: $.authorities[0]
        type: by_regex
        value: '^[a-zA-Z0-9_\\- ]+$'
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThat(parsedJson.read("$.authorities[0]", String.class)).matches("^[a-zA-Z0-9_\\\\- ]+$")''')
		and:
			SyntaxChecker.tryToCompileJava("mockmvc", test)
	}

	def 'should work with execution property with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "PUT"
  url: "/fraudcheck"
response:
  status: 200
  body:
    fraudCheckStatus: "OK"
    rejectionReason: null
  matchers:
    body:
      - path: $.rejectionReason
        type: by_command
        value: "assertThatRejectionReasonIsNull($it)"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('''assertThatJson(parsedJson).field("[\'rejectionReason']").isEqualTo("assertThatRejectionReasonIsNull("''')
			test.contains('''assertThatRejectionReasonIsNull(''')
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('262')
	def "should generate proper test code with map inside list"() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/foos"
response:
  status: 200
  body:
    - id: "123"
    - id: "567"
  headers:
    "Content-Type": "application/json;charset=UTF-8"
  matchers:
    body:
      - path: $[0].id
        type: by_regex
        value: "[0-9]+"
      - path: $[1].id
        type: by_regex
        value: "[0-9]+"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(parsedJson.read("\\$[0].id", String.class)).matches("[0-9]+")')
			test.contains('assertThat(parsedJson.read("\\$[1].id", String.class)).matches("[0-9]+")')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic("spock", test)
	}

	@Issue('266')
	def "should generate proper test code with top level array using #methodBuilderName"() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/tags"
response:
  status: 200
  body: 
    - "Java"
    - "Java8"
    - "Spring"
    - "SpringBoot"
    - "Stream"
  headers:
    "Content-Type": "application/json;charset=UTF-8"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('266')
	@RestoreSystemProperties
	def 'should generate proper test code with top level array using #methodBuilderName with array size check'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/tags"
response:
  status: 200
  body: ["Java", "Java8", "Spring", "SpringBoot", "Stream"]
  headers:
    "Content-Type": "application/json;charset=UTF-8"
'''
			Contract contractDsl = fromYaml(contract)
			System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')

			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).hasSize(5)')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('266')
	def 'should generate proper test code with top level array or arrays using #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/categories"
response:
  status: 200
  body: [["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]]
  headers:
    "Content-Type": "application/json;charset=UTF-8"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Programming").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Java").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Spring").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Boot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('47')
	def 'should generate async body when async flag set in response'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "test"
response:
  async: true
  status: 200
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(bodyDefinitionString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder                                      | bodyDefinitionString
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | '.when().async()'
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }         | '.when().async()'
	}

	@Issue('372')
	def 'should generate async body after queryParams when async flag set in response and queryParams set in request'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/test"
  queryParameters:
    param: value
response:
  async: true
  status: 200
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
			def strippedTest = test.replace('\n', '').replace(' ', '').replaceAll("\t", "").stripIndent().stripMargin()
		then:
			strippedTest.contains('.queryParam("param","value").when().async().get("/test")')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
	}

	def 'should generate proper test code with array of primitives using #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/tags"
response:
  status: 200
  body:
    - partners:
        payment_methods: 
          - BANK
          - CASH
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).array().field("[\'partners\']").array("[\'payment_methods\']").arrayField().isEqualTo("BANK").value()')
			test.contains('assertThatJson(parsedJson).array().field("[\'partners\']").array("[\'payment_methods\']").arrayField().isEqualTo("CASH").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#273')
	def 'should not escape dollar in Spock regex tests'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body:
    code: 9
    message: Wrong credentials
  matchers:
    body:
      - path: $.message
        type: by_regex
        value: '^(?!\\s*$).+'
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(parsedJson.read("\\$.message", String.class)).matches("^(?!\\\\s*\\$).+")')
		and:
			SyntaxChecker.tryToCompileGroovy("spock", test,
					false)
	}

	@Issue('#85')
	def 'should execute custom method for complex structures on the response side'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body:
    fraudCheckStatus: "OK"
    rejectionReason:
      title: null
  matchers:
    body:
      - path: $.rejectionReason.title
        type: by_command
        value: "assertThatRejectionReasonIsNull($it)"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatRejectionReasonIsNull(parsedJson.read("\\\$.rejectionReason.title"))')
			SyntaxChecker.tryToCompileGroovy("spock", test)
	}

	@Issue('#85')
	def 'should execute custom method for more complex structures on the response side when using Spock'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body:
    - name: "userName 1"
    - name: "userName 2"
  matchers:
    body:
      - path: $[0].name
        type: by_command
        value: "assertThatUserNameIsNotNull($it)"
      - path: $[1].name
        type: by_command
        value: "assertThatUserNameIsNotNull($it)"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("\\$[0].name"))''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("\\$[1].name"))''')
	}

	@Issue('#85')
	def 'should execute custom method for more complex structures on the response side when using JUnit'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body:
    - name: "userName 1"
    - name: "userName 2"
  matchers:
    body:
      - path: $[0].name
        type: by_command
        value: "assertThatUserNameIsNotNull($it)"
      - path: $[1].name
        type: by_command
        value: "assertThatUserNameIsNotNull($it)"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[0].name")''')
			test.contains('''assertThatUserNameIsNotNull(parsedJson.read("$[1].name")''')
	}

	@Issue('#111')
	def 'should execute custom method for request headers'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
  headers:
    authorization: Bearer token
  matchers:
    headers:
      - key: "authorization"
        command: "getOAuthTokenHeader()"
response:
  status: 200
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('.header("authorization", getOAuthTokenHeader())')
			SyntaxChecker.tryToCompileGroovy("spock", test)
	}

	@Issue('#150')
	def 'should support body matching in response'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body: "HELLO FROM STUB"
  matchers:
    body:
      - type: by_regex
        value: ".*"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains("responseBody ==~ java.util.regex.Pattern.compile('.*')")
		and:
			SyntaxChecker.tryToCompileGroovy("spock", test)
	}

	@Issue('#150')
	def 'should support custom method execution in response'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
response:
  status: 200
  body: "HELLO FROM STUB"
  matchers:
    body:
      - type: by_command
        value: "foo($it)"
'''
			Contract contractDsl = fromYaml(contract)
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('foo(responseBody)')
			SyntaxChecker.tryToCompileGroovy("spock", test)
	}

	@Issue('#162')
	def 'should escape regex properly for content type'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/get"
  headers:
    "Content-Type": "application/vnd.fraud.v1+json"
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application.vnd.fraud.v1.json.*'
response:
  status: 200
  headers:
    "Content-Type": "application/vnd.fraud.v1+json"
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application.vnd.fraud.v1.json.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			matcher(test)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | matcher
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String s -> 'assertThat(response.header("Content-Type")).matches("application.vnd.fraud.v1.json.*")' }
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String s -> "response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application.vnd.fraud.v1.json.*')" }
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | { String s -> "response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application.vnd.fraud.v1.json.*')" }
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                 | { String s -> "response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application.vnd.fraud.v1.json.*')" }
	}

	@Issue('#172')
	def 'should resolve plain text properly via headers with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/foo"
response:
  status: 200
  body: |
    {"a":1}
    {"a":2}
  headers:
    "Content-Type": "text/plain"
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'text/plain.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('assertThatJson(parsedJson).field("[\'a\']").isEqualTo(1)')
			test.contains(expectedAssertion)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName | methodBuilder                                      | expectedAssertion
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """responseBody == '''{"a":1}\\n{"a":2}\\n'''"""
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }         | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}\\n'''
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }   | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}\\n'''
			"custom"          | { properties.testMode = TestMode.CUSTOM }          | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}\\n'''
	}

	@Issue('#443')
	def 'should resolve plain text that happens to be a valid json for [#methodBuilderName]'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/foo"
response:
  status: 200
  body: 'true'
  headers:
    "Content-Type": "application/json;charset=utf-8"
  matchers:
    body:
      - type: by_regex
        value: 'true|false'
    headers:
      - key: 'Content-Type'
        regex: 'application/json;charset=utf-8.*'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			testAssertion(test)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | testAssertion
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String testContents -> testContents.contains("""responseBody ==~ java.util.regex.Pattern.compile('true|false')""") }
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
			"mockmvc-testng"  | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testContents -> testContents.contains("""responseBody ==~ java.util.regex.Pattern.compile("true|false")""") }
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                 | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
	}

	@Issue('#169')
	def "should escape quotes properly using [#methodBuilderName]"() {
		given:
			String contract = '''\
---
request:
  method: "POST"
  url: "/foo"
  body:
    xyz: 'abc'
  headers:
    "Content-Type": "application/json;charset=utf-8"
response:
  status: 200
  body: '{ "bar": "some value \\u0022with quote\\u0022" }'
  headers:
    "Content-Type": "application/json;charset=utf-8"
  matchers:
    body:
      - path: $.bar
        type: by_regex
        value: 'some value "with quote"|bar'
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('.matches("some value \\"with quote\\"|bar")')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName | methodBuilder                                      | expectedAssertion
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | '''responseBody == "{\\"a\\":1}\\n{\\"a\\":2}"'''
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }         | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
			"custom"          | { properties.testMode = TestMode.CUSTOM }          | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
	}

	@Issue('#169')
	def "should make the execute method work in a url for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url $(c("foo"), p(execute("toString()")))
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		and:
			test.contains("toString()")
			!test.contains("\"toString()\"")
			!test.contains("'toString()'")
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"mockmvc-testng"  | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}
	}

	@Issue('#203')
	def "should create an assertion for an empty list for [#methodBuilderName]"() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/v1/xxxx"
response:
  status: 200
  body: 
    status: '200'
    list: []
    foo   : 
      - "bar"
      - "baz"
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			test.contains('assertThatJson(parsedJson).array("[\'list\']").isEmpty()')
			!test.contains('assertThatJson(parsedJson).array("[\'foo\']").isEmpty()')
		where:
			methodBuilderName | methodBuilder
			"spock"           | { properties.testFramework = TestFramework.SPOCK }
			"mockmvc"         | { properties.testMode = TestMode.MOCKMVC }
			"mockmvc-testng"  | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | { properties.testMode = TestMode.WEBTESTCLIENT }
			"custom"          | { properties.testMode = TestMode.CUSTOM }
	}

	@Issue('#226')
	def 'should work properly when body is an integer [#methodBuilderName]'() {
		given:
			String contract = '''\
---
request:
  method: "GET"
  url: "/api/v1/xxxx"
  body: 12000
response:
  status: 200
  body: 12000
'''
			Contract contractDsl = fromYaml(contract)
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			requestAssertion(test)
			responseAssertion(test)
		where:
			methodBuilderName | methodBuilder | requestAssertion                                                                             | responseAssertion
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String body -> body.contains("body('''12000''')") }                                        | { String body -> body.contains("responseBody == '12000'") }
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String body -> body.contains('body("12000")') }                                            | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
			"mockmvc-testng"  | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String body -> body.contains('body("12000")') }                                            | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String body -> body.contains(""".build("GET", entity("12000", "text/plain"))""") } | { String body -> body.contains('responseBody == "12000"') }
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String body -> body.contains(""".build("GET", entity("12000", "text/plain"))""") } | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000")') }
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | { String body -> body.contains('body("12000")') }                                            | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
			"custom"          | {
				properties.testMode = TestMode.CUSTOM
			}                                 | { String body -> body.contains('body("12000")') }                                            | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
	}
}
