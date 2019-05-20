/*
 * Copyright 2013-2019 the original author or authors.
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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Rule
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

/**
 * @author Jakub Kubrynski, codearte.io
 * @author Tim Ysewyn
 */
class YamlMockMvcMethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule
	OutputCapture capture = new OutputCapture()

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Shared
	GeneratedClassDataForMethod generatedClassDataForMethod = new GeneratedClassDataForMethod(
			new SingleTestGenerator.GeneratedClassData("foo", "bar", new File(".").toPath()), "method")

	static File textToFile(String text) {
		File temp = File.createTempFile("yaml", ".yml")
		temp.text = text
		return temp
	}

	static Contract fromYaml(String text) {
		return new YamlContractConverter().convertFrom(textToFile(text))[0]
	}

	@Shared
	String contractDslWithCookiesValue = """\
---
request:
  methodBuilder: "GET"
  url: "/foo"
  headers:
    'Accept': 'application/json'
  cookies:
    'cookie-key': 'cookie-value'
response:
  status: 200
  headers:
    'Content-Type': 'application/json'
  cookies:
    'cookie-key': 'new-cookie-value'
  body:
    status: 'OK'
"""

	@Shared
	String contractDslWithCookiesPattern = """\
---
request:
  methodBuilder: "GET"
  url: "/foo"
  headers:
    'Accept': 'application/json'
  cookies:
    'cookie-key': 'cookie-value'
  matchers:
    cookies:
      - key: 'cookie-key'
        regex: '[A-Za-z]+'
response:
  status: 200
  headers:
    'Content-Type': 'application/json'
  cookies:
    'cookie-key': 'new-cookie-value'
  body:
    status: 'OK'
  matchers:
    cookies:
      - key: 'cookie-key'
        regex: '[A-Za-z]+'
"""

	// TODO: Add absent
	@Shared
	String contractDslWithAbsentCookies = """\
---
request:
  methodBuilder: "GET"
  url: "/foo"
  headers:
    'Accept': 'application/json'
  cookies:
    'cookie-key': 'cookie-value'
  matchers:
    cookies:
      - key: 'cookie-key'
        regex: '[A-Za-z]+'
response:
  status: 200
  headers:
    'Content-Type': 'application/json'
  cookies:
    'cookie-key': 'new-cookie-value'
  body:
    status: 'OK'
  matchers:
    cookies:
      - key: 'cookie-key'
        absent
"""

	@Shared
	// tag::contract_with_regex[]
	String dslWithOptionalsInString = """\
---
priority: 1
request:
  methodBuilder: "POST"
  url: "/users/password"
  headers:
    'Content-Type': 'application/json'
  body:
    email: 'abc@abc.com'
    callback_url: 'http://partners.com'
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
    body:
      - path: \$.email
        predefined: email
      - path: \$.callback_url
        predefined: hostname
response:
  status: 404
  headers:
    'Content-Type': 'application/json'
  body:
    code: "123123"
    message: "User not found by email = [not.existing@user.com]"
  matchers:
    headers:
      - key: 'Content-Type'
        regex: 'application/json.*'
    body:
      - path: \$.code
        type: by_regex
        value: "[123123]?"
      - path: \$.message
        type: by_regex
        value: "User not found by email = [[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}]"
"""
	// end::contract_with_regex[]

	def 'should generate assertions for simple response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "a"
    "property2": "b"
"""
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property2']").isEqualTo("b")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue("#187")
	def "should generate assertions for null and boolean values with #methodBuilderName"() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
  url: "test"
response:
  status: 200
  body:
    "property1": "true"
    "property2": null
    "property3": false
"""
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("true")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property2']").isNull()""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property3']").isEqualTo(false)""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#79')
	def 'should generate assertions for simple response body constructed from map with a list with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#79')
	@RestoreSystemProperties
	def 'should generate assertions for simple response body constructed from map with a list with #methodBuilderName with array size check'() {
		given:
			System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
			String contract = """\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property2']").contains("['a']").isEqualTo("sth")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property2']").hasSize(2)""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property2']").contains("['b']").isEqualTo("sthElse")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#82')
	def 'should generate proper request when body constructed from map with a list #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
  url: "test"
  body:
    items: 
      - "HOP"
response:
  status: 200
"""
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | bodyString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | """.body('''{\"items\":[\"HOP\"]}''')"""
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '.body("{\\"items\\":[\\"HOP\\"]}")'
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | '.body("{\\"items\\":[\\"HOP\\"]}")'
	}

	@Issue('#88')
	def 'should generate proper request when body constructed from GString with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
  url: "test"
  body: "property1=VAL1"
response:
  status: 200
"""
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(bodyString)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | bodyString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | """.body('''property1=VAL1''')"""
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '.body("property1=VAL1")'
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | '.body("property1=VAL1")'
	}

	@Issue('185')
	def 'should generate assertions for a response body containing map with integers as keys with #methodBuilderName'() {
		given:
			// YAML CAN'T HAVE INTEGER KEYS
			String contract = """\
---
request:
  methodBuilder: "GET"
  url: "test"
response:
  status: 200
  body:
    property: 
      14: 0.0
      7: 0.0
"""
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property']").field("['7']").isEqualTo(0.0)""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property']").field("['14']").isEqualTo(0.0)""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate assertions for array in response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("['property2']").isEqualTo("b")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array().contains("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate assertions for array inside response body element with #methodBuilderName'() {
		given:

			String contract = """\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property1']").contains("['property2']").isEqualTo("test1")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).array("['property1']").contains("['property3']").isEqualTo("test2")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate assertions for nested objects in response body with #methodBuilderName'() {
		given:
			String contract = """\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property2']").field("['property3']").isEqualTo("b")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate regex assertions for map objects in response body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("""\$.property2", String.class)).matches("[0-9]{3}")""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['property1']").isEqualTo("a")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def "should generate a call with an url path and query parameters with #methodBuilderName"() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
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
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#169')
	def 'should generate a call with an url path and query parameters with url containing a pattern with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
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
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate test for empty body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "POST"
  url: "/ws/payments"
  body: ""
response:
  status: 406
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains(bodyString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | bodyString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ".body('''''')"
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '.body("")'
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | '.body("")'
	}

	def 'should generate test for String in response body with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "POST"
  url: "test"
  body: ""
response:
  status: 200
  body: "test"
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains(bodyDefinitionString)
			test.contains(bodyEvaluationString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | bodyDefinitionString                                   | bodyEvaluationString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | 'def responseBody = (response.body.asString())'        | 'responseBody == "test"'
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | 'String responseBody = response.getBody().asString();' | 'assertThat(responseBody).isEqualTo("test");'
	}

	@Issue('113')
	def 'should generate regex test for String in response header with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "POST"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains(headerEvaluationString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | headerEvaluationString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '''response.header('Location') ==~ java.util.regex.Pattern.compile('http://localhost/partners/[0-9]+/users/[0-9]+')'''
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | 'assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");'
	}

	def 'should work with more complex stuff and jsonpaths with #methodBuilderName'() {
		given:
			String contract = '''\
---
priority: 10
request:
  methodBuilder: "POST"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("""assertThatJson(parsedJson).array("['errors']").contains("['property']").isEqualTo("bank_account_number")""")
			test.contains("""assertThatJson(parsedJson).array("['errors']").contains("['message']").isEqualTo("incorrect_format")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('72')
	def 'should make the execute method work with #methodBuilderName'() {
		given:
			String contract = '''\
---
priority: 10
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		then:
			assertionStrings.each { String assertionString ->
				assert test.contains(assertionString)
			}
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | assertionStrings
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ['''assertThatRejectionReasonIsNull(parsedJson.read("\\\$.rejectionReason"))''', '''assertThatLocationIsNull(response.header('Location'))''']
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | ['''assertThatRejectionReasonIsNull(parsedJson.read("$.rejectionReason"))''', '''assertThatLocationIsNull(response.header("Location"))''']
	}

	def "shouldn't generate unicode escape characters with #methodBuilderName"() {
		given:
			String contract = '''\
---
priority: 10
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains("\\u041f")
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('180')
	def 'should generate proper test code when having multipart parameters with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | requestStrings
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  """.param('formParameter', '"formParameterValue"'""",
																																														  """.param('someBooleanParameter', 'true')""",
																																														  """.multiPart('file', 'filename.csv', 'file content'.bytes, 'application/json')"""]
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.param("formParameter", "\\"formParameterValue\\"")',
																																														  '.param("someBooleanParameter", "true")',
																																														  '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json");']
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.param("formParameter", "\\"formParameterValue\\"")',
																																														  '.param("someBooleanParameter", "true")',
																																														  '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json");']

	}

	@Issue('546')
	def 'should generate test code when having multipart parameters with byte array #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | requestStrings
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  """.multiPart('file', 'filename.csv', 'file content'.bytes, 'application/json')"""]
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json");']
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.multiPart("file", "filename.csv", "file content".getBytes(), "application/json");']

	}

	@Issue('541')
	def 'should generate proper test code when having multipart parameters that use execute with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			for (String requestString : requestStrings) {
				assert test.contains(requestString)
			}
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | requestStrings
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  """.param('formParameter', '"formParameterValue"'""",
																																														  """.param('someBooleanParameter', 'true')""",
																																														  """.multiPart('file', toString(), 'file content'.bytes, 'application/json')"""]
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.param("formParameter", "\\"formParameterValue\\"")',
																																														  '.param("someBooleanParameter", "true")',
																																														  '.multiPart("file", toString(), "file content".getBytes(), "application/json");']
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | ['"Content-Type", "multipart/form-data;boundary=AaB03x"',
																																														  '.param("formParameter", "\\"formParameterValue\\"")',
																																														  '.param("someBooleanParameter", "true")',
																																														  '.multiPart("file", toString(), "file content".getBytes(), "application/json");']

	}

	@Issue('#216')
	def 'should parse JSON with arrays using Spock'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThat(parsedJson.read("\\$.authorities[0]", String.class)).matches("^[a-zA-Z0-9_\\\\- ]+\\$")''')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic("spock", blockBuilder.toString())
	}

	@Issue('#216')
	def 'should parse JSON with arrays using JUnit'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('''assertThat(parsedJson.read("$.authorities[0]", String.class)).matches("^[a-zA-Z0-9_\\\\- ]+$")''')
		and:
			SyntaxChecker.tryToCompileJava(MockMvcJUnitMethodBodyBuilder.simpleName, blockBuilder.toString())
	}

	def 'should work with execution property with #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "PUT"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('''assertThatJson(parsedJson).field("[\'rejectionReason']").isEqualTo("assertThatRejectionReasonIsNull("''')
			test.contains('''assertThatRejectionReasonIsNull(''')
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('262')
	def "should generate proper test code with map inside list"() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThat(parsedJson.read("\\$[0].id", String.class)).matches("[0-9]+")')
			test.contains('assertThat(parsedJson.read("\\$[1].id", String.class)).matches("[0-9]+")')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic("spock", blockBuilder.toString())
	}

	@Issue('266')
	def "should generate proper test code with top level array using #methodBuilderName"() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('266')
	@RestoreSystemProperties
	def 'should generate proper test code with top level array using #methodBuilderName with array size check'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
  url: "/api/tags"
response:
  status: 200
  body: ["Java", "Java8", "Spring", "SpringBoot", "Stream"]
  headers:
    "Content-Type": "application/json;charset=UTF-8"
'''
			Contract contractDsl = fromYaml(contract)
			System.setProperty('spring.cloud.contract.verifier.assert.size', 'true')
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).hasSize(5)')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java8").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Spring").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Java").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("Stream").value()')
			test.contains('assertThatJson(parsedJson).arrayField().contains("SpringBoot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('266')
	def 'should generate proper test code with top level array or arrays using #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
  url: "/api/categories"
response:
  status: 200
  body: [["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]]
  headers:
    "Content-Type": "application/json;charset=UTF-8"
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Programming").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Java").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Spring").value()')
			test.contains('assertThatJson(parsedJson).array().array().arrayField().isEqualTo("Boot").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('47')
	def 'should generate async body when async flag set in response'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
  url: "test"
response:
  async: true
  status: 200
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains(bodyDefinitionString)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | bodyDefinitionString
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '.when().async()'
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '.when().async()'
	}

	@Issue('372')
	def 'should generate async body after queryParams when async flag set in response and queryParams set in request'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
  url: "/test"
  queryParameters:
    param: value
response:
  async: true
  status: 200
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
			def strippedTest = test.replace('\n', '').replace(' ', '').stripIndent().stripMargin()
		then:
			strippedTest.contains('.queryParam("param","value").when().async().get("/test")')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	def 'should generate proper test code with array of primitives using #methodBuilderName'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).array().field("[\'partners\']").array("[\'payment_methods\']").arrayField().isEqualTo("BANK").value()')
			test.contains('assertThatJson(parsedJson).array().field("[\'partners\']").array("[\'payment_methods\']").arrayField().isEqualTo("CASH").value()')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#273')
	def 'should not escape dollar in Spock regex tests'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThat(parsedJson.read("\\$.message", String.class)).matches("^(?!\\\\s*\\$).+")')
		and:
			SyntaxChecker.tryToCompileGroovy(HttpSpockMethodRequestProcessingBodyBuilder.simpleName, blockBuilder.toString(),
					false)
	}

	@Issue('#85')
	def 'should execute custom method for complex structures on the response side'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatRejectionReasonIsNull(parsedJson.read("\\\$.rejectionReason.title"))')
		when:
			SyntaxChecker.tryToCompileGroovy(HttpSpockMethodRequestProcessingBodyBuilder.simpleName, blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains('Cannot find matching methodBuilder Script1#assertThatRejectionReasonIsNull')
	}

	@Issue('#85')
	def 'should execute custom method for more complex structures on the response side when using Spock'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
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
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.then(blockBuilder)
			def test = blockBuilder.toString()
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
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.given(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('.header("authorization", getOAuthTokenHeader())')
		when:
			SyntaxChecker.tryToCompileGroovy(HttpSpockMethodRequestProcessingBodyBuilder.simpleName, blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains('Cannot find matching methodBuilder Script1#getOAuthTokenHeader')
	}

	@Issue('#150')
	def 'should support body matching in response'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains("responseBody ==~ java.util.regex.Pattern.compile('.*')")
		and:
			SyntaxChecker.tryToCompileGroovy(HttpSpockMethodRequestProcessingBodyBuilder.simpleName, blockBuilder.toString())
	}

	@Issue('#150')
	def 'should support custom method execution in response'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = new HttpSpockMethodRequestProcessingBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('foo(responseBody)')
		when:
			SyntaxChecker.tryToCompileGroovy(HttpSpockMethodRequestProcessingBodyBuilder.simpleName, blockBuilder.toString())
		then:
			def e = thrown(MultipleCompilationErrorsException)
			e.message.contains('Cannot find matching methodBuilder Script1#foo')
	}

	@Issue('#162')
	def 'should escape regex properly for content type'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			matcher(test)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                      | methodBuilder                                                                                                     | matcher
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | { String s -> 'assertThat(response.header("Content-Type")).matches("application.vnd.fraud.v1.json.*")' }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | { String s -> "response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application.vnd.fraud.v1.json.*')" }
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | { String s -> "response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application.vnd.fraud.v1.json.*')" }
	}

	@Issue('#172')
	def 'should resolve plain text properly via headers'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('assertThatJson(parsedJson).field("[\'a\']").isEqualTo(1)')
			test.contains(expectedAssertion)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName                                      | methodBuilder                                                                                                     | expectedAssertion
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '''responseBody == "{\\"a\\":1}\\n{\\"a\\":2}'''
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
			WebTestClientJUnitMethodBodyBuilder.simpleName         | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }         | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
	}

	@Issue('#443')
	def 'should resolve plain text that happens to be a valid json for [#methodBuilderName]'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			testAssertion(test)
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                             | methodBuilder                                                                                                            | testAssertion
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }        | { String testContents -> testContents.contains("""responseBody ==~ java.util.regex.Pattern.compile('true|false')""") }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                      | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | { String testContents -> testContents.contains("""responseBody ==~ java.util.regex.Pattern.compile('true|false')""") }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                  | { String testContents -> testContents.contains("""assertThat(responseBody).matches("true|false");""") }
	}

	@Issue('#169')
	def "should escape quotes properly using [#methodBuilderName]"() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "POST"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('.matches("some value \\"with quote\\"|bar")')
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
		where:
			//order is inverted cause Intellij didn't parse this properly
			methodBuilderName                                      | methodBuilder                                                                                                     | expectedAssertion
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '''responseBody == "{\\"a\\":1}\\n{\\"a\\":2}"'''
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }               | '''assertThat(responseBody).isEqualTo("{\\"a\\":1}\\n{\\"a\\":2}'''
	}

	@Issue('#169')
	def "should make the execute method work in a url for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url $(c("foo"), p(execute("executedMethod()")))
				}
				response {
					status OK()
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			def e = thrown(Throwable)
			missingMethodAssertion(e, capture)
		and:
			test.contains("executedMethod()")
			!test.contains("\"executedMethod()\"")
			!test.contains("'executedMethod()'")
		where:
			methodBuilderName                                             | methodBuilder                                                                                                            | missingMethodAssertion
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }        | { Throwable t, OutputCapture capture -> t.message.contains("Cannot find matching methodBuilder Script1#executedMethod") }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                      | { Throwable t, OutputCapture capture -> t.message.contains("Truncated class file") && capture.toString().contains("post(executedMethod())") }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | { Throwable t, OutputCapture capture -> t.message.contains("Cannot find matching methodBuilder Script1#executedMethod") }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                  | { Throwable t, OutputCapture capture -> t.message.contains("Truncated class file") && capture.toString().contains("path(executedMethod())") }
	}

	@Issue('#203')
	def "should create an assertion for an empty list for [#methodBuilderName]"() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			test.contains('assertThatJson(parsedJson).array("[\'list\']").isEmpty()')
			!test.contains('assertThatJson(parsedJson).array("[\'foo\']").isEmpty()')
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }
	}

	@Issue('#226')
	def 'should work properly when body is an integer [#methodBuilderName]'() {
		given:
			String contract = '''\
---
request:
  methodBuilder: "GET"
  url: "/api/v1/xxxx"
  body: 12000
response:
  status: 200
  body: 12000
'''
			Contract contractDsl = fromYaml(contract)
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		and:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		when:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			requestAssertion(test)
			responseAssertion(test)
		where:
			methodBuilderName                                             | methodBuilder                                                                                                            | requestAssertion                                                                      | responseAssertion
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) }        | { String body -> body.contains("body('''12000''')") }                                 | { String body -> body.contains('responseBody == "12000"') }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                      | { String body -> body.contains('body("12000")') }                                     | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, generatedClassDataForMethod) } | { String body -> body.contains(""".methodBuilder('GET', entity('12000', 'text/plain'))""") } | { String body -> body.contains('responseBody == "12000"') }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                  | { String body -> body.contains(""".methodBuilder("GET", entity("12000", "text/plain"))""") } | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000")') }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) }                | { String body -> body.contains('body("12000")') }                                     | { String body -> body.contains('assertThat(responseBody).isEqualTo("12000");') }
	}
}
