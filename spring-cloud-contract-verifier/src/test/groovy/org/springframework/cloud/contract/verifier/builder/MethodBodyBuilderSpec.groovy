/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.verifier.builder

import java.lang.reflect.InvocationTargetException

import org.junit.Rule
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

class MethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule
	OutputCapture capture = new OutputCapture()

	@Shared
	GeneratedClassDataForMethod classDataForMethod = new GeneratedClassDataForMethod(
			new SingleTestGenerator.GeneratedClassData("ClassName", "com.example",
					new File("target/test.java").toPath()),
			"some_method"
	)

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true, generatedTestSourcesDir: new File("."),
			generatedTestResourcesDir: new File(".")
	)

	@Issue('#251')
	def 'should work with execute and arrays [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/foo'
					headers {
						accept(applicationJson())
						contentType(applicationJson())
					}
				}
				response {
					status OK()
					body([
							myArray: [
									[
											notABugGeneratedHere       : $(c('foo'),
													p(execute('assertThat((String)$it).isEqualTo("foo")'))),
											anotherArrayNeededForBug   : [
													[
															optionalNotEmpty: $(c('foo'),
																	p(execute('assertThat((String)$it).isEqualTo("12")')))
													]
											],
											yetAnotherArrayNeededForBug: [
													[
															optionalNotEmpty: $(c('foo'),
																	p(execute('assertThat((String)$it).isEqualTo("22")')))
													]
											]
									],
									[
											anotherArrayNeededForBug2: [
													[
															optionalNotEmpty: $(c('foo'),
																	p(execute('assertThat((String)$it).isEqualTo("122")')))
													]
											]
									],
							]
					])
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
			test.contains('$.myArray.[0].anotherArrayNeededForBug.[0].optionalNotEmpty')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			String jsonSample = '''\
String json = "{\\"myArray\\":[{\\"notABugGeneratedHere\\":\\"foo\\",\\"anotherArrayNeededForBug\\":[{\\"optionalNotEmpty\\":\\"12\\"}],\\"yetAnotherArrayNeededForBug\\":[{\\"optionalNotEmpty\\":\\"22\\"}]},{\\"anotherArrayNeededForBug2\\":[{\\"optionalNotEmpty\\":\\"122\\"}]}]}";
DocumentContext parsedJson = JsonPath.parse(json);
'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine {
				if (it.contains("assertThatJson") || it.contains("assertThat((String")) {
					lines << it
				}
				else {
					it
				}
			}
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#588')
	def 'should work patterns in GString [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url("/${regex('\\d+')}")
				}
				response {
					status 200
					body([
							ok: true
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('d+')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#521')
	def 'should always escape generated chars [#methodBuilderName]'() {
		expect:
			[1..200].each {
				Contract contractDsl = Contract.make {
					request {
						method GET()
						urlPath('/v1/users') {
							queryParameters {
								parameter 'userId': value(regex(nonBlank()))
							}
						}
					}
					response {
						status 200
						body([
								ok: value(regex(nonBlank()))
						])
					}
				}
				MethodBodyBuilder builder = methodBuilder(contractDsl)
				BlockBuilder blockBuilder = new BlockBuilder(" ")

				builder.appendTo(blockBuilder)
				def test = blockBuilder.toString()

				assert !test.contains('REGEXP>>')
				SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
			}
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#269')
	def 'should work with execute and keys with dots [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/foo'
				}
				response {
					status OK()
					body(
							foo: ["my.dotted.response":
										  $(c('foo'), p(execute('"foo".equals($it)')))]
					)
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
			test.contains('''$.foo.['my.dotted.response']''')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			String jsonSample = '''\
String json = "{\\"foo\\":{\\"my.dotted.response\\":\\"foo\\"}}";
DocumentContext parsedJson = JsonPath.parse(json);
'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine {
				if (it.contains('"foo".equals')) {
					lines << it
				}
				else {
					it
				}
			}
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#289')
	def 'should fail on nonexistent field [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/something'
					headers {
						contentType(applicationJson())
					}
				}
				response {
					status OK()
					headers {
						contentType(applicationJson())
					}
					body([
							doesNotExist: $(p(anyAlphaUnicode()), c("123"))
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			String jsonSample = '''\
String json = "{}";
DocumentContext parsedJson = JsonPath.parse(json);
'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine {
				if (it.contains('assertThatJson')) {
					lines << it
				}
				else {
					it
				}
			}
			lines.addFirst(jsonSample)
			try {
				SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
			}
			catch (IllegalStateException e) {
				assert e.message.contains("Parsed JSON [{}] doesn't match the JSON path")
			}
			catch (InvocationTargetException e1) {
				assert e1.cause.message.
						contains("Parsed JSON [{}] doesn't match the JSON path")
			}
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#313')
	def 'should allow to use execute in request body [#methodBuilderName]'() {
		given:
			//tag::body_execute[]
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/something'
					body(
							$(c('foo'), p(execute('hashCode()')))
					)
				}
				response {
					status OK()
				}
			}
			//end::body_execute[]
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
			!test.contains("executionCommand")
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#318')
	def 'should assert the response headers properly [#methodBuilderName]'() {
		given:
			def contractDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'POST'
					urlPath '/documents/app_statement_v1'
					headers {
						contentType(applicationPdf())
					}
					body([
							PESEL           : '77100604360',
							CLIENT_NAME     : 'STANISLAW STASZIC',
							STATEMENT_NUMBER: '00200001/C4/2017/1'
					])
				}
				response {
					status OK()
					headers {
						contentType(applicationPdf())
						header('Content-Length': 4)
					}

				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			asserter(test)
		where:
			methodBuilderName                                             | methodBuilder                                                                                                   | asserter
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }        | { String testBody ->
				testBody.contains("response.header('Content-Length') == 4") && testBody.
						contains("response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')")
			}
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                      | { String testBody ->
				testBody.
						contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");')
			}
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) } | { String testBody ->
				testBody.
						contains("response.getHeaderString('Content-Length') == 4") && testBody.
						contains("  response.getHeaderString('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')")
			}
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                  | { String testBody ->
				testBody.
						contains('assertThat(response.getHeaderString("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.getHeaderString("Content-Type")).matches("application/pdf.*");')
			}
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                | { String testBody ->
				testBody.
						contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");')
			}
	}

	def 'should put L on long values [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url 'test'
				}
				response {
					status OK()
					body(
							'createdAt': 1502766000000,
							'updatedAt': 1499476115000
					)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().
					contains("""assertThatJson(parsedJson).field("['createdAt']").isEqualTo(1502766000000L)""")
			blockBuilder.toString().
					contains("""assertThatJson(parsedJson).field("['updatedAt']").isEqualTo(1499476115000L)""")
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.
							toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#424')
	def 'should not put an absent header to the request [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/mytest'
					headers {
						header('myheader', absent())
					}
				}
				response {
					status OK()
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
		then:
			!blockBuilder.toString().contains('myheader')
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.
							toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#458')
	def 'should reference request from body when body is a string [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/mytest'
					body("""{ "name": "My name" }""")
				}
				response {
					status OK()
					body fromRequest().body('$.name')
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			responseAsserter(test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder                                                                                                   | responseAsserter
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }        | { String string ->
				string.contains('responseBody == "My name"')
			}
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                      | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) } | { String string ->
				string.contains('responseBody == "My name"')
			}
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                  | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
	}

	@Issue('#559')
	def 'should reference request from body without escaping of non-string [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url('/mytest') {
						queryParameters {
							parameter('foo', 'bar')
							parameter('number', 1)
						}
					}
					body("""{ "name": "My name" }""")
				}
				response {
					status OK()
					body(
							foo: fromRequest().query('foo'),
							number: fromRequest().query('number')
					)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['foo']").isEqualTo("bar")''')
			test.contains('''assertThatJson(parsedJson).field("['number']").isEqualTo(1)''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#702')
	def 'should generate proper type for large numbers [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'PUT'
					urlPath '/example/create'
					headers {
						contentType applicationJson()
					}
					body(
							[
									"name"      :
											$(consumer(~/.+/), producer('string-1')),
									"updatedTs" :
											$(consumer(regex(~/1531916906000/).asLong())),
									"isDisabled": $(consumer(regex(anyBoolean())),
											producer(true))
							]
					)
				}

				response {
					status 200
					headers {
						contentType applicationJsonUtf8()
					}
					body(
							[
									"id"        : $(consumer(2222L), producer(~/\d+/)),
									"name"      : fromRequest().body('name'),
									"updatedTs" : fromRequest().body('updatedTs'),
									"isDisabled": fromRequest().body('isDisabled')
							]
					)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['updatedTs']").isEqualTo(1531916906000L)''')
			!test.contains('''"updatedTs":"1531916906000"''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#465')
	def "should work for '/' url for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				description("""
				Represents a request to the shouldReturnName service
				
				given:
					a request to the shouldReturnName service
				when:
					it is a GET
				then:
					return Ryan
				""")
				request {
					method 'GET'
					url '/'
				}
				response {
					status OK()
					body('Ryan')
					headers {
						contentType(textHtml())
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			String test = blockBuilder.toString()
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	def 'should use fixed delay milliseconds in the generated test [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url 'test'
				}
				response {
					status OK()
					async()
					fixedDelayMilliseconds(10000)
					body(a: 'foo')
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(""".timeout(10000)""")
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                      | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#493')
	def 'should not escape a form URL encoded request body [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url '/api/form-endpoint'
					headers {
						header("Content-Type": 'application/x-www-form-urlencoded')
					}
					body('a=abc&b=123')
				}
				response {
					status OK()
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('a=abc&amp;b=123')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#578')
	def 'should work for form parameters [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method POST()
					urlPath('/oauth/token')
					headers {
						header(authorization(), anyNonBlankString())
						header(contentType(), applicationFormUrlencoded())
						header(accept(), applicationJson())
					}
					body([
							username  : 'user',
							password  : 'password',
							grant_type: 'password'
					])
				}
				response {
					status 200
					headers {
						header(contentType(), applicationJson())
					}
					body([
							refresh_token: 'RANDOM_REFRESH_TOKEN',
							access_token : 'RANDOM_ACCESS_TOKEN',
							token_type   : 'bearer',
							expires_in   : 3600,
							scope        : ['task'],
							user         : [
									id      : 1,
									username: 'user',
									name    : 'User'
							]
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('username=user&password=password&grant_type=password')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue('#493')
	def 'should not escape a form URL encoded request body another try [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method POST()
					urlPath('/oauth/token')
					headers {
						header(authorization(), anyNonBlankString())
						header(contentType(), 'application/x-www-form-urlencoded; charset=UTF-8')
						header(accept(), anyNonBlankString())
					}
					body('username=user&password=password&grant_type=password')
				}
				response {
					status 200
					headers {
						header(contentType(), applicationJsonUtf8())
					}
					body([
							refresh_token: 'RANDOM_REFRESH_TOKEN',
							access_token : 'RANDOM_ACCESS_TOKEN',
							token_type   : 'bearer',
							expires_in   : 3600,
							scope        : ['task'],
							user         : [
									id      : 1,
									username: 'user',
									name    : 'User'
							]
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('&amp;')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	def 'should work with files that have new lines [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method('PUT')
					headers {
						contentType(applicationJson())
					}
					body(file("classpath/request.json"))
					url("/1")
				}
				response {
					status OK()
					body(file("classpath/response.json"))
					headers {
						contentType(applicationJson())
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(' ')
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['status']").isEqualTo("RESPONSE")''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}


	@Issue('#509')
	def 'classToCheck() should return class of object'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					url '/api/users'
				}
				response {
					status OK()
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
		when:
			Map<String, String> map = new LinkedHashMap<>()
			Integer number = Integer.valueOf(42)
			List<String> list = new ArrayList<>()
			Set<String> set = new HashSet<>()
		then:
			builder.classToCheck(map) == Map.class
		and:
			builder.classToCheck(number) == Integer.class
		and:
			builder.classToCheck(list) == List.class
		and:
			builder.classToCheck(set) == Set.class
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	def 'should assert null values without matchers [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url 'test'
				}
				response {
					status OK()
					body([
							nullValue: null
					])
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().
					contains("""assertThatJson(parsedJson).field("['nullValue']").isNull()""")
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.
							toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	def 'should not escape a regex pattern when matching raw body value [#methodBuilderName]'() {
		def pattern = "\\d+\\w?"
		def escapedPattern = "\\\\d+\\\\w?"

		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/api/arbitrary-url'
				}
				response {
					status OK()
					body(value(stub("1"), test(regex(pattern))))
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			test.contains(escapedPattern)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#664")
	def 'should work with binary payload for [#methodBuilderName]'() {
		given:
			File root = new File("src/test/resources/body_builder/")
			Contract contract = ContractVerifierDslConverter.convertAsCollection(root,
					new File(root, "worksWithPdf.groovy")).first()
			MethodBodyBuilder builder = methodBuilder(contract)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			requestMatcher(test)
			responseMatcher(test)
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName                                             | methodBuilder                                                                                                   | requestMatcher | responseMatcher
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }        | { String string ->
				string.contains('.body(fileToBytes(this, "some_method_request_request.pdf"))')
			}                                                                                                                                                                                                | { String string ->
				string.contains('response.body.asByteArray() == fileToBytes(this, "some_method_response_response.pdf")')
			}
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                      | { String string ->
				string.contains('.body(fileToBytes(this, "some_method_request_request.pdf"));')
			}                                                                                                                                                                                                | { String string ->
				string.contains('assertThat(response.getBody().asByteArray()).isEqualTo(fileToBytes(this, "some_method_response_response.pdf"));')
			}
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) } | { String string ->
				string.contains('entity(fileToBytes(this, "some_method_request_request.pdf")')
			}                                                                                                                                                                                                | { String string ->
				string.contains('response.readEntity(byte[]) == fileToBytes(this, "some_method_response_response.pdf")')
			}
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                  | { String string ->
				string.contains('entity(fileToBytes(this, "some_method_request_request.pdf")')
			}                                                                                                                                                                                                | { String string ->
				string.contains('assertThat(response.readEntity(byte[].class)).isEqualTo(fileToBytes(this, "some_method_response_response.pdf"));')
			}
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                | { String string ->
				string.contains('.body(fileToBytes(this, "some_method_request_request.pdf"));')
			}                                                                                                                                                                                                | { String string ->
				string.contains('assertThat(response.getBody().asByteArray()).isEqualTo(fileToBytes(this, "some_method_response_response.pdf"));')
			}
	}

	@Issue("#797")
	def "should not create an unnecessary empty collection check [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				name("get_new_toy_specs")
				request {
					description("""
            Given: A new toy request is submitted
            When: I receive the response
            Then: I would receive the toy specs
        """)
					method 'GET'
					urlPath('/toys') {
						queryParameters {
							parameter 'uuid': 'd4d724c4-e36e-4fd2-9baa-af7f5df17399'
						}
					}
				}
				response {
					status 200
					body([
							toyUuid       : "d4d724c4-e36e-4fd2-9baa-af7f5df17399",
							toyDescription: [
									name        : "Super Whiz Bang Toy",
									stockNum    : 1234,
									manufacturer: "Toy Comp",
							],
							toyDetails    : [
									[
											inventory  : 42,
											description: "Toy of the year!!",
											dimensions : [
													height: 45.8,
													weight: 12.3,
													width : 8.6,
													length: 9.3
											]
									]
							]
					])
					bodyMatchers {
						//toyDescription checks
						jsonPath('$.toyDetails[*].dimensions.height', byRegex(nonBlank()))
						jsonPath('$.toyDetails[*].dimensions.weight', byRegex(nonBlank()))
						jsonPath('$.toyDetails[*].dimensions.width', byRegex(nonBlank()))
						jsonPath('$.toyDetails[*].dimensions.length', byRegex(nonBlank()))
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
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''assertThatJson(parsedJson).array("['toyDetails']").field("['dimensions']").isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#852")
	def "should work with escaped quotes [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url('/test')
					headers {
						accept(applicationJson())
						header("X-Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJObyI6IjEyMzQ1In0.VdYumw6QkfxaBgFUZNyza1VfNKiZ2WW4JaxIKe-G8HA")
					}
				}
				response {
					status OK()
					body([
							"test": "\"escaped\""
					])
					headers {
						contentType(applicationJson())
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEqualTo("\\\\"escaped\\\\"")''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#844")
	def "should not leave unnecessary isEmpty when using matchers [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url('/test')
					headers {
						accept(applicationJson())
						header("X-Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJObyI6IjEyMzQ1In0.VdYumw6QkfxaBgFUZNyza1VfNKiZ2WW4JaxIKe-G8HA")
					}
				}
				response {
					status OK()
					body([
							[test: 'testJson'],
							[test: 'testJson']
					])
					headers {
						contentType(applicationJson())
					}
					bodyMatchers {
						jsonPath('$', byType {
							minOccurrence(2)
							maxOccurrence(2)
						})
						jsonPath('$[*].test', byType {
							minOccurrence(2)
							maxOccurrence(2)
						})
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''assertThatJson(parsedJson).array().isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#652")
	def "should not parse json in a json [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				name("insertSomething_ShouldReturnHttp200")
				description("POST should do sth")
				request {
					method 'POST'
					url "/foo"
					body(
							value: "{}"
					)
					headers {
						contentType(applicationJson())
					}
				}
				response {
					status 200
					headers { contentType(applicationJson()) }
					body(
							value: "{}"
					)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains(''':{}}''')
			test.contains("""assertThatJson(parsedJson).field("['value']").isEqualTo("{}")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#727")
	def "should not leave empty arrays [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/list'
				}
				response {
					status 200
					body(
							[
									content: [
											one  : "two",
											two  : "two",
											three: [
													six: "seven"
											]
									]
							]
					)
					bodyMatchers {
						jsonPath('$.content.three.six', byRegex(".*seven.*"))
						jsonPath('$.content.one', byRegex(".*two.*"))
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#727")
	def "should not leave empty arrays in a simple structure [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/list'
				}
				response {
					status 200
					body(
							[
									content: [
											three: [
													six: "seven"
											]
									]
							]
					)
					bodyMatchers {
						jsonPath('$.content.three.six', byRegex(".*seven.*"))
						jsonPath('$.content.one', byRegex(".*two.*"))
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#892")
	def "should not unnecessarily escape non json body [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url $(consumer(regex("/api/v1/files/" + uuid())),
							producer("/api/v1/files/b0683f29-741a-4178-b5c6-6e62202e3cf1"))
				}
				response {
					status OK()
					body($(consumer("some-content"), producer(regex(nonBlank()))))
					headers {
						header(contentLength(),
								$(consumer(2647691), producer(regex(positiveInt()))))
						header(contentType(), $(consumer(applicationOctetStream()),
								producer(regex(nonBlank()))))
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			asserter(test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder                                                                                                   | asserter
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }        | { String testContent ->
				assert testContent.
						contains('''response.header('Content-Length') ==~ java.util.regex.Pattern.compile('([1-9]\\\\d*)')''') && testContent.
						contains('''response.header('Content-Type') ==~ java.util.regex.Pattern.compile('^\\\\s*\\\\S[\\\\S\\\\s]*')'''); return true
			}
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                      | { String testContent ->
				assert testContent.
						contains('''assertThat(response.header("Content-Type")).matches("^\\\\s*\\\\S[\\\\S\\\\s]*")''') && testContent.
						contains('''assertThat(response.header("Content-Length")).matches("([1-9]\\\\d*)")'''); return true
			}
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) } | { String testContent ->
				assert testContent.
						contains('''response.getHeaderString('Content-Length') ==~ java.util.regex.Pattern.compile('([1-9]\\\\d*)')''') && testContent.
						contains('''response.getHeaderString('Content-Type') ==~ java.util.regex.Pattern.compile('^\\\\s*\\\\S[\\\\S\\\\s]*')'''); return true
			}
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }                  | { String testContent ->
				assert testContent.
						contains('''assertThat(response.getHeaderString("Content-Type")).matches("^\\\\s*\\\\S[\\\\S\\\\s]*")''') && testContent.
						contains('''assertThat(response.getHeaderString("Content-Length")).matches("([1-9]\\\\d*)")'''); return true
			}
	}

	@Issue("#1034")
	def "should not escape headers as jsons [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				name 'my name'
				request {
					method POST()
					urlPath '/my-url'
					headers {
						contentType(applicationJson())
						accept(applicationJson())
						header('my-json-header', ''' { "value": "123" } ''')
					}
				}
				response {
					status OK()
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''[value:123]''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#1052")
	def "should work with large numbers [#methodBuilderName]"() {
		given:
			String yaml = '''\
request:
  url: /numbers
  queryParameters:
    page: 0
    size: 2
  method: GET
  headers:
    Content-Type: application/json

response:
  status: 200
  headers:
    Content-Type: application/json;charset=UTF-8
  body:
    - number: 1541609556000
    - number: 1541609316000
  matchers:
    body:
      - path: $.[0].number
        type: by_equality
      - path: $.[1].number
        type: by_equality
'''
			File tmpFile = File.createTempFile("foo", ".yml")
			tmpFile.createNewFile()
			tmpFile.text = yaml
			Contract contractDsl = new YamlContractConverter().convertFrom(tmpFile).
					first()
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''(1541609556000)''')
			test.contains('''(1541609556000L)''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#1262")
	def "should work with the timeout flag for groovy [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method(GET())
					url("/hello")
				}
				response {
					status(200)
					fixedDelayMilliseconds(5000)
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''timeout''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#1262")
	def "should work with the timeout flag for yaml [#methodBuilderName]"() {
		given:
			String yaml = '''\
request:
  method: GET
  url: /hello
  queryParameters:
    name: LuLu
response:
  status: 200
  fixedDelayMilliseconds: 5000
  body: "Hello LuLu"
  async: true
'''
			File tmpFile = File.createTempFile("foo", ".yml")
			tmpFile.createNewFile()
			tmpFile.text = yaml
			Contract contractDsl = new YamlContractConverter().convertFrom(tmpFile).
					first()
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''timeout''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	@Issue("#1049")
	def "should work with body having new lines [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url "/foo"
				}
				response {
					status OK()
					headers {
						contentType('application/x-research-info-systems;charset=UTF-8')
					}
					body("1\n2\n3\n")
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
	}

	def 'should resolve headers from request correctly'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method PUT()
					url '/frauds/name'
					body([
							name: $(anyAlphaUnicode())
					])
					headers {
						contentType("application/json")
					}
				}
				response {
					status OK()
					body([
							result: "Don't worry ${fromRequest().body('$.name')} you're not a fraud"
					])
					headers {
						header(contentType(), "${fromRequest().header(contentType())};charset=UTF-8")
					}
				}
			}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			String test = blockBuilder.toString()
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			test.contains($/assertThatJson(parsedJson).field("['result']").isEqualTo("Don't worry/$)
			test.contains("you're not a fraud")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties, classDataForMethod) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties, classDataForMethod) }

	}

}
