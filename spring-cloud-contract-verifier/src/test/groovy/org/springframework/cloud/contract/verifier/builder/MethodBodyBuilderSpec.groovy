/*
 *  Copyright 2013-2017 the original author or authors.
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
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.InvocationTargetException

class MethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule OutputCapture capture = new OutputCapture()

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Issue('#251')
	def "should work with execute and arrays [#methodBuilderName]"() {
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
				status 200
				body ([
						myArray:[
								[
										notABugGeneratedHere: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("foo")'))),
										anotherArrayNeededForBug:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("12")')))
												]
										],
										yetAnotherArrayNeededForBug:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("22")')))
												]
										]
								],
								[
										anotherArrayNeededForBug2:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("122")')))
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
			test.eachLine { if (it.contains("assertThatJson") || it.contains("assertThat((String")) lines << it else it }
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#269')
	def "should work with execute and keys with dots [#methodBuilderName]"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/foo'
			}
			response {
				status 200
				body (
						foo: ["my.dotted.response" : $(c('foo'), p(execute('"foo".equals($it)')))]
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
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#289')
	def "should fail on nonexistent field [#methodBuilderName]"() {
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
				status 200
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
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#313')
	def "should allow to use execute in request body [#methodBuilderName]"() {
		given:
		//tag::body_execute[]
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/something'
					body(
							$(c("foo"), p(execute("hashCode()")))
					)
				}
				response {
					status 200
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
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

	@Issue('#318')
	def "should assert the response headers properly [#methodBuilderName]"() {
		given:
			def contractDsl = org.springframework.cloud.contract.spec.Contract.make {
				request {
					method 'POST'
					urlPath '/documents/app_statement_v1'
					headers {
						contentType(applicationPdf())
					}
					body([
							PESEL: "77100604360",
							CLIENT_NAME: "STANISLAW STASZIC",
							STATEMENT_NUMBER: "00200001/C4/2017/1"
					])
				}
				response {
					status 200
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
			methodBuilderName                                    | methodBuilder                                                                               | asserter
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }     | { String testBody -> testBody.contains("response.header('Content-Length') == 4") && testBody.contains("response.header('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')") }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }                      | { String testBody -> testBody.contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");') }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) } | { String testBody -> testBody.contains("response.getHeaderString('Content-Length') == 4") && testBody.contains("  response.getHeaderString('Content-Type') ==~ java.util.regex.Pattern.compile('application/pdf.*')") }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }                  | { String testBody -> testBody.contains('assertThat(response.getHeaderString("Content-Length")).isEqualTo(4);') && testBody.contains('assertThat(response.getHeaderString("Content-Type")).matches("application/pdf.*");') }
	}

	def "should put L on long values [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					url "test"
				}
				response {
					status 200
					body(
							"createdAt": 1502766000000,
							"updatedAt": 1499476115000
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
			methodBuilderName           						 | methodBuilder
			"MockMvcSpockMethodBuilder" 						 | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder" 						 | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

}
