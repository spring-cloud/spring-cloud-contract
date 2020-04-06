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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.springframework.boot.test.system.OutputCaptureRule
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class XmlMethodBodyBuilderSpec extends Specification {

	@Rule
	OutputCaptureRule capture = new OutputCaptureRule()

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true, generatedTestSourcesDir: new File("."),
			generatedTestResourcesDir: new File(".")
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

	@Unroll
	def 'should generate correct verification from xml with body matchers  [#methodBuilderName]'() {
		given:
			Contract contractDsl =
					// tag::xmlgroovy[]
					Contract.make {
						request {
							method GET()
							urlPath '/get'
							headers {
								contentType(applicationXml())
							}
						}
						response {
							status(OK())
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
<key><complex>foo</complex></key>
</test>"""
							bodyMatchers {
								xPath('/test/duck/text()', byRegex("[0-9]{3}"))
								xPath('/test/duck/text()', byCommand('equals($it)'))
								xPath('/test/duck/xxx', byNull())
								xPath('/test/duck/text()', byEquality())
								xPath('/test/alpha/text()', byRegex(onlyAlphaUnicode()))
								xPath('/test/alpha/text()', byEquality())
								xPath('/test/number/text()', byRegex(number()))
								xPath('/test/date/text()', byDate())
								xPath('/test/dateTime/text()', byTimestamp())
								xPath('/test/time/text()', byTime())
								xPath('/test/*/complex/text()', byEquality())
								xPath('/test/duck/@type', byEquality())
							}
						}
					}
			// end::xmlgroovy[]
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/list/elem/text()")).isEqualTo("abc")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/list/elem[2]/text()")).isEqualTo("def")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/list/elem[3]/text()")).isEqualTo("ghi")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/aBoolean/text()")).isEqualTo("true")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/valueWithoutAMatcher/text()")).isEqualTo("foo")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/duck/text()")).matches("[0-9]{3}")')
			test.contains('equals("123")')
			test.contains('assertThat(nodeFromXPath(parsedXml, "/test/duck/xxx")).isNull()')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/duck/text()")).isEqualTo("123")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/alpha/text()")).matches("[\\\\p{L}]*")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/alpha/text()")).isEqualTo("abc")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/number/text()")).matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/date/text()")).matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/dateTime/text()")).matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/time/text()")).matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/*/complex/text()")).isEqualTo("foo")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/test/duck/@type")).isEqualTo("xtype")')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder
			"spock"           | {
				properties.testFramework = TestFramework.SPOCK
			}
			"testng"          | {
				properties.testFramework = TestFramework.TESTNG
			}
			"junit"           | {
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
	}

	def 'should throw exception for verification by type'() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method GET()
					urlPath '/get'
					headers {
						contentType(applicationXml())
					}
				}
				response {
					status(OK())
					headers {
						contentType(applicationXml())
					}
					body """
<test>
<duck type='xtype'>123</duck>
</test>"""
					bodyMatchers {
						xPath('/test/duck/text()', byType())
					}
				}
			}
			properties.testMode = TestMode.MOCKMVC
		when:
			singleTestGenerator(contractDsl)
		then:
			thrown UnsupportedOperationException
	}
}
