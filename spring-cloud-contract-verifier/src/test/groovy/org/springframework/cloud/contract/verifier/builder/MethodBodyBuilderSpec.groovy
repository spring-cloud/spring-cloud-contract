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

package org.springframework.cloud.contract.verifier.builder

import org.junit.Rule
import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.dsl.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.InvocationTargetException

class MethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule
	OutputCapture capture = new OutputCapture()

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
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
											notABugGeneratedHere       : $(c('foo'), p(execute('assertThat((String)$it).isEqualTo("foo")'))),
											anotherArrayNeededForBug   : [
													[
															optionalNotEmpty: $(c('foo'), p(execute('assertThat((String)$it).isEqualTo("12")')))
													]
											],
											yetAnotherArrayNeededForBug: [
													[
															optionalNotEmpty: $(c('foo'), p(execute('assertThat((String)$it).isEqualTo("22")')))
													]
											]
									],
									[
											anotherArrayNeededForBug2: [
													[
															optionalNotEmpty: $(c('foo'), p(execute('assertThat((String)$it).isEqualTo("122")')))
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
				if (it.contains("assertThatJson") || it.contains("assertThat((String")) lines << it else it
			}
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
							foo: ["my.dotted.response": $(c('foo'), p(execute('"foo".equals($it)')))]
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
			test.eachLine { if (it.contains('"foo".equals')) lines << it else it }
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			test.eachLine { if (it.contains('assertThatJson')) lines << it else it }
			lines.addFirst(jsonSample)
			try {
				SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
			} catch (IllegalStateException e) {
				assert e.message.contains("Parsed JSON [{}] doesn't match the JSON path")
			} catch (InvocationTargetException e1) {
				assert e1.cause.message.contains("Parsed JSON [{}] doesn't match the JSON path")
			}
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			methodBuilderName                                             | methodBuilder                                                                               | asserter
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }        | { String testBody -> testBody.contains("response.header('Content-Length') == 4") && testBody.contains("response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')") }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { String testBody -> testBody.contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");') }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String testBody -> testBody.contains("response.getHeaderString('Content-Length') == 4") && testBody.contains("  response.getHeaderString('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')") }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { String testBody -> testBody.contains('assertThat(response.getHeaderString("Content-Length")).isEqualTo(4);') && testBody.contains('assertThat(response.getHeaderString("Content-Type")).matches("application/pdf.*");') }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }                | { String testBody -> testBody.contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");') }
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
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['createdAt']").isEqualTo(1502766000000L)""")
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['updatedAt']").isEqualTo(1499476115000L)""")
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			methodBuilderName                                             | methodBuilder                                                                               | responseAsserter
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }        | { String string -> string.contains('responseBody == "My name"') }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { String string -> string.contains('assertThat(responseBody).isEqualTo("My name");') }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String string -> string.contains('responseBody == "My name"') }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { String string -> string.contains('assertThat(responseBody).isEqualTo("My name");') }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }                | { String string -> string.contains('assertThat(responseBody).isEqualTo("My name");') }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
									"name"      : $(consumer(~/.+/), producer('string-1')),
									"updatedTs" : $(consumer(~/\d{13}/), producer(1531916906000L)),
									"isDisabled": $(consumer(regex(anyBoolean())), producer(true))
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
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName               | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			blockBuilder.toString().contains("""assertThatJson(parsedJson).field("['nullValue']").isNull()""")
		and:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, blockBuilder.toString())
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
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
			test
		where:
			methodBuilderName                                             | methodBuilder
			HttpSpockMethodRequestProcessingBodyBuilder.simpleName        | { Contract dsl -> new HttpSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			MockMvcJUnitMethodBodyBuilder.simpleName                      | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			JaxRsClientSpockMethodRequestProcessingBodyBuilder.simpleName | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			JaxRsClientJUnitMethodBodyBuilder.simpleName                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
			WebTestClientJUnitMethodBodyBuilder.simpleName                | { Contract dsl -> new WebTestClientJUnitMethodBodyBuilder(dsl, properties) }
	}
}
