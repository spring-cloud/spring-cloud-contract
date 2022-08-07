/*
 * Copyright 2013-2020 the original author or authors.
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

import org.junit.Rule
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.boot.test.system.OutputCaptureRule
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

class MethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule
	OutputCaptureRule capture = new OutputCaptureRule()

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true, generatedTestSourcesDir: new File("."),
			generatedTestResourcesDir: new File(".")
	)

	@Shared
	SingleTestGenerator.GeneratedClassData generatedClassData =
			new SingleTestGenerator.GeneratedClassData("foo", "com.example", new File(MethodBodyBuilderSpec.getResource(".").toURI()).toPath())

	def setup() {
		properties = new ContractVerifierConfigProperties(
				assertJsonSize: true,
				generatedTestSourcesDir: new File(MethodBodyBuilderSpec.getResource(".").toURI()),
				generatedTestResourcesDir: new File(MethodBodyBuilderSpec.getResource(".").toURI())
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
		}.buildClass(properties, Collections.singletonList(contractMetadata(contractDsl)), "foo", generatedClassData)
	}

	private ContractMetadata contractMetadata(Contract contractDsl) {
		return new ContractMetadata(new File(".").toPath(), false, 0, null, contractDsl)
	}

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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('$.myArray.[0].anotherArrayNeededForBug.[0].optionalNotEmpty')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('d+')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
				methodBuilder()
				String test = singleTestGenerator(contractDsl)

				assert !test.contains('REGEXP>>')
				SyntaxChecker.tryToCompile(methodBuilderName, test)
			}
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('''$.foo.['my.dotted.response']''')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			!test.contains("executionCommand")
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			asserter(test)
		where:
			methodBuilderName | methodBuilder | asserter
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String testBody ->
				testBody.contains('response.header("Content-Length") == 4') && testBody.
						contains('''response.header("Content-Type") ==~ java.util.regex.Pattern.compile('application/pdf.*')''')
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String testBody ->
				testBody.
						contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");')
			}
			"mockmvc-testng"   | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String testBody ->
				testBody.
						contains('assertThat(response.header("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.header("Content-Type")).matches("application/pdf.*");')
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testBody ->
				testBody.
						contains("""response.getHeaderString("Content-Length") == 4""") && testBody.
						contains("""response.getHeaderString("Content-Type") ==~ java.util.regex.Pattern.compile("application/pdf.*")""")
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testBody ->
				testBody.
						contains('assertThat(response.getHeaderString("Content-Length")).isEqualTo(4);') && testBody.
						contains('assertThat(response.getHeaderString("Content-Type")).matches("application/pdf.*");')
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | { String testBody ->
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.
					contains("""assertThatJson(parsedJson).field("['createdAt']").isEqualTo(1502766000000L)""")
			test.
					contains("""assertThatJson(parsedJson).field("['updatedAt']").isEqualTo(1499476115000L)""")
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue(['#424','#1647'])
	def 'should not put an absent header to the request [#methodBuilderName]'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					url '/mytest'
					headers {
						header('header-before', anyNonBlankString())
						header('myheader', absent())
						header('header-after', anyNonBlankString())
					}
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('myheader')
			test.contains('header-before')
			test.contains('header-after')
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			responseAsserter(test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder | responseAsserter
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String string ->
				string.contains("responseBody == '''My name'''")
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
			"mockmvc-testng"         | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String string ->
				string.contains('responseBody == "My name"')
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String string ->
				string.contains('assertThat(responseBody).isEqualTo("My name");')
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | { String string ->
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['foo']").isEqualTo("bar")''')
			test.contains('''assertThatJson(parsedJson).field("['number']").isEqualTo(1)''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['updatedTs']").isEqualTo(1531916906000L)''')
			!test.contains('''"updatedTs":"1531916906000"''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(""".timeout(10000)""")
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('a=abc&amp;b=123')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('username=user&password=password&grant_type=password')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('&amp;')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''assertThatJson(parsedJson).field("['status']").isEqualTo("RESPONSE")''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			ClassVerifier verifier = new TestClassVerifier()
		when:
			Map<String, String> map = new LinkedHashMap<>()
			Integer number = Integer.valueOf(42)
			List<String> list = new ArrayList<>()
			Set<String> set = new HashSet<>()
		then:
			verifier.classToCheck(map) == Map.class
		and:
			verifier.classToCheck(number) == Integer.class
		and:
			verifier.classToCheck(list) == List.class
		and:
			verifier.classToCheck(set) == Set.class
	}

	class TestClassVerifier implements ClassVerifier {

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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.
					contains("""assertThatJson(parsedJson).field("['nullValue']").isNull()""")
		and:
			SyntaxChecker.
					tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains(escapedPattern)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue("#664")
	def 'should work with binary payload for [#methodBuilderName]'() {
		given:
			File root = new File("src/test/resources/body_builder/")
			Contract contractDsl = ContractVerifierDslConverter.convertAsCollection(root,
					new File(root, "worksWithPdf.groovy")).first()
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			requestMatcher(test)
			responseMatcher(test)
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder | requestMatcher | responseMatcher
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String string ->
				string.contains('.body(fileToBytes(this, "worksWithPdf_request_request.pdf"))')
			}                                                  | { String string ->
				string.contains('response.body.asByteArray() == fileToBytes(this, "worksWithPdf_response_response.pdf")')
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String string ->
				string.contains('.body(fileToBytes(this, "worksWithPdf_request_request.pdf"));')
			}                                                  | { String string ->
				string.contains('assertThat(response.getBody().asByteArray()).isEqualTo(fileToBytes(this, "worksWithPdf_response_response.pdf"));')
			}
			"mockmvc-testng"         | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String string ->
				string.contains('.body(fileToBytes(this, "worksWithPdf_request_request.pdf"));')
			}                                                  | { String string ->
				string.contains('assertThat(response.getBody().asByteArray()).isEqualTo(fileToBytes(this, "worksWithPdf_response_response.pdf"));')
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String string ->
				string.contains('entity(fileToBytes(this, "worksWithPdf_request_request.pdf")')
			}                                                  | { String string ->
				string.contains('response.readEntity(byte[]) == fileToBytes(this, "worksWithPdf_response_response.pdf")')
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String string ->
				string.contains('entity(fileToBytes(this, "worksWithPdf_request_request.pdf")')
			}                                                  | { String string ->
				string.contains('assertThat(response.readEntity(byte[].class)).isEqualTo(fileToBytes(this, "worksWithPdf_response_response.pdf"));')
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | { String string ->
				string.contains('.body(fileToBytes(this, "worksWithPdf_request_request.pdf"));')
			}                                                  | { String string ->
				string.contains('assertThat(response.getBody().asByteArray()).isEqualTo(fileToBytes(this, "worksWithPdf_response_response.pdf"));')
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''assertThatJson(parsedJson).array("['toyDetails']").field("['dimensions']").isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEqualTo("\\\\"escaped\\\\"")''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''assertThatJson(parsedJson).array().isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains(''':{}}''')
			test.contains("""assertThatJson(parsedJson).field("['value']").isEqualTo("{}")""")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
			asserter(test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder | asserter
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | { String testContent ->
				assert testContent.
						contains('''response.header("Content-Length") ==~ java.util.regex.Pattern.compile('([1-9]\\\\d*)')''') && testContent.
						contains('''response.header("Content-Type") ==~ java.util.regex.Pattern.compile('^\\\\s*\\\\S[\\\\S\\\\s]*')'''); return true
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | { String testContent ->
				assert testContent.
						contains('''assertThat(response.header("Content-Type")).matches("^\\\\s*\\\\S[\\\\S\\\\s]*")''') && testContent.
						contains('''assertThat(response.header("Content-Length")).matches("([1-9]\\\\d*)")'''); return true
			}
			"mockmvc-testng"         | {
				properties.testFramework = TestFramework.TESTNG; properties.testMode = TestMode.MOCKMVC
			}                                 | { String testContent ->
				assert testContent.
						contains('''assertThat(response.header("Content-Type")).matches("^\\\\s*\\\\S[\\\\S\\\\s]*")''') && testContent.
						contains('''assertThat(response.header("Content-Length")).matches("([1-9]\\\\d*)")'''); return true
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testContent ->
				assert testContent.
						contains('''response.getHeaderString("Content-Length") ==~ java.util.regex.Pattern.compile("([1-9]\\\\d*)")''') && testContent.
						contains('''response.getHeaderString("Content-Type") ==~ java.util.regex.Pattern.compile("^\\\\s*\\\\S[\\\\S\\\\s]*")'''); return true
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | { String testContent ->
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''[value:123]''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''(1541609556000)''')
			test.contains('''(1541609556000L)''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Ignore
	@Issue("#1414")
	def "should work with empty arrays after doing array matching [#methodBuilderName]"() {
		given:
			String yaml = '''\
name: GET sample
description: sample
request:
  method: GET
  urlPath: /sample
response:
  status: 200
  body:
    - foo: "sample1"
      bar: true
    - foo: "sample2"
      bar: false
  headers:
    Content-Type: application/json
  matchers:
    body:
      - path: $..foo
        type: by_regex
        value: .*
      - path: $..bar
        type: by_regex
        predefined: any_boolean
'''
			File tmpFile = File.createTempFile("foo", ".yml")
			tmpFile.createNewFile()
			tmpFile.text = yaml
			Contract contractDsl = new YamlContractConverter().convertFrom(tmpFile).
					first()
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''assertThatJson(parsedJson).array().isEmpty()''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue("#1441")
	def "should work with query parameters that need to be escaped [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					urlPath("/rest/something") {
						queryParameters {
							parameter 'quote': equalTo("\"")
						}
					}
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			!test.contains('''.queryParam("quote",""")''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue("#854")
	def "should call execute in queryParameters [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'POST'
					urlPath("/rest/something") {
						queryParameters {
							    parameter('someHashCode': $(
                                	consumer(regex(anInteger())),
                                	producer(execute("hashCode()")))
                        		)
						}
					}
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
			test.contains('''.queryParam("someHashCode",hashCode())''') | test.contains('''.queryParam("someHashCode", hashCode())''')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			test.contains("timeout")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			test.contains("timeout")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
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
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			test.contains($/assertThatJson(parsedJson).field("['result']").isEqualTo("Don't worry/$)
			test.contains("you're not a fraud")
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	def 'should work with an array of uuids'() {
		given:
			Contract contractDsl = Contract.make {
				description "TEST ARRAY"
				request {
					method POST()
					urlPath($(c('/TEST'), p('/TEST')))
					body([
							$(c(anyUuid()), p("00000000-0000-0000-0000-000000000002")),
							$(c(anyUuid()), p("00000000-0000-0000-0000-000000000001"))
					])
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			!test.contains('singleValue')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue("1635")
	def 'should work with anyOf that contains special chars'() {
		given:
			Contract contractDsl = Contract.make {
				name 'anyOf test'
				request {
					method 'POST'
					url ("hello")
					body(
							type: anyOf("VAL", "VAL+VAL")
					)
				}
				response {
					status OK()
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompileWithoutCompileStatic(methodBuilderName, test)
		and:
			!test.contains('singleValue')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}
	}

	@Issue("1808")
	def "should correctly process optional of DslProperty parameters"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method('GET')
					url("/api/foo")
					headers {
						header 'Content-Type': 'application/json'
						header 'Accept': 'application/json'
					}
					body([
							key1: $(client(optional(anyOf("foo", "bar"))), server("bar")),
							key2: $(client(optional(anyNonBlankString())), server("bar")),
							key3: $(client(optional(anyEmail())), server("foo@bar.com")),
							key4: $(optional(anyNumber()))
					])
				}
				response {
					status OK()
					headers {
						header 'Content-Type': 'application/json'
					}
					body([
							key1: $(client("bar"), server(optional(anyOf("foo", "bar")))),
							key2: $(client("bar"), server(optional(anyNonBlankString()))),
							key3: $(client("foo@bar.com"), server(optional(anyEmail()))),
							key4: $(optional(anyNumber()))
					])
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		then:
			test.contains('assertThatJson(parsedJson).field("[\'key1\']").matches("(^foo' + endOfLineRegexSymbol + '|^bar' + endOfLineRegexSymbol + ')?")')
			test.contains('assertThatJson(parsedJson).field("[\'key2\']").matches("(^\\\\s*\\\\S[\\\\S\\\\s]*)?")')
			test.contains('assertThatJson(parsedJson).field("[\'key3\']").matches("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6})?")')
			test.contains('assertThatJson(parsedJson).field("[\'key4\']").matches("(-?(\\\\d*\\\\.\\\\d+|\\\\d+))?")')
		and:
			stubMappingIsValidWireMockStub(contractDsl)
		where:
			methodBuilderName | methodBuilder | endOfLineRegexSymbol
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}                                 | '\\$'
			"mockmvc"         | {
				properties.testMode = TestMode.MOCKMVC
			}                                 | '\$'
			"jaxrs-spock"     | {
				properties.testFramework = TestFramework.SPOCK; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | '\\$'
			"jaxrs"           | {
				properties.testFramework = TestFramework.JUNIT; properties.testMode = TestMode.JAXRSCLIENT
			}                                 | '\$'
			"webclient"       | {
				properties.testMode = TestMode.WEBTESTCLIENT
			}                                 | '\$'
			"testNG"          | {
				properties.testFramework = TestFramework.TESTNG
			}                                 | '\$'
	}
}
