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
 * @author Chris Bono
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

	@Unroll
	def 'should generate correct verification from xml with namespace [#methodBuilderName]'() {
		given:
			Contract contractDsl =
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
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <ns2:Res xmlns:ns2="http://*******/****/****/******/schema">
                <ns2:ID>1</ns2:ID>
            </ns2:Res>
        </soap:Body>
    </soap:Envelope>"""
						}
					}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(valueFromXPath(parsedXml, "/soap:Envelope/soap:Body/ns2:Res/ns2:ID/text()")).isEqualTo("1")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/soap:Envelope/namespace::soap")).isEqualTo("http://schemas.xmlsoap.org/soap/envelope/")')
			test.contains('valueFromXPath(parsedXml, "/soap:Envelope/soap:Body/ns2:Res/namespace::ns2")).isEqualTo("http://*******/****/****/******/schema"')
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

	@Unroll
	def 'should generate correct verification from named xml with body matchers  [#methodBuilderName]'() {
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
<ns1:test xmlns:ns1="http://demo.com/testns">
 <ns1:header>
    <duck-bucket type='bigbucket'>
      <duck>duck5150</duck>
    </duck-bucket>
</ns1:header>
</ns1:test>
"""
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
								xPath('/test/duck/@type', byEquality())
							}
						}
					}
			// end::xmlgroovy[]
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:test/ns1:header/duck-bucket/duck/text()")).isEqualTo("duck5150")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:test/namespace::ns1")).isEqualTo("http://demo.com/testns")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:test/ns1:header/duck-bucket/@type")).isEqualTo("bigbucket")')
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

	@Unroll
	def 'should generate correct verification from complex named xml with body matchers  [#methodBuilderName]'() {
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
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header>
      <RsHeader xmlns="http://schemas.xmlsoap.org/soap/custom">
         <MsgSeqId>1234</MsgSeqId>
      </RsHeader>
   </SOAP-ENV:Header>
</SOAP-ENV:Envelope>
"""
							bodyMatchers {
								xPath('//*[local-name()=\'RsHeader\' and namespace-uri()=\'http://schemas.xmlsoap.org/soap/custom\']/*[local-name()=\'MsgSeqId\']/text()', byEquality())
							}
						}
					}
			// end::xmlgroovy[]
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(valueFromXPath(parsedXml, "//*[local-name()=\'RsHeader\' and namespace-uri()=\'http://schemas.xmlsoap.org/soap/custom\']/*[local-name()=\'MsgSeqId\']/text()")).isEqualTo("1234")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/SOAP-ENV:Envelope/namespace::SOAP-ENV")).isEqualTo("http://schemas.xmlsoap.org/soap/envelope/")')
			!test.contains('assertThat(valueFromXPath(parsedXml, "/SOAP-ENV:Envelope/SOAP-ENV:Header/*[local-name()=\'RsHeader\' and namespace-uri()=\'http://schemas.xmlsoap.org/soap/custom\']/@xmlns")).isEqualTo"')
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

	@Unroll
	def 'should generate correct verification from very complex named xml without body matchers  [#methodBuilderName]'() {
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
<ns1:customer xmlns:ns1="http://demo.com/customer" xmlns:addr="http://demo.com/address">
	<email>customer@test.com</email>
	<contact-info xmlns="http://demo.com/contact-info">
		<name>Krombopulous</name>
		<address>
			<addr:gps>
				<lat>51</lat>
				<addr:lon>50</addr:lon>
			</addr:gps>
		</address>
	</contact-info>
</ns1:customer>
"""
						}
					}
			// end::xmlgroovy[]
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/namespace::ns1")).isEqualTo("http://demo.com/customer")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/namespace::addr")).isEqualTo("http://demo.com/address")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/email/text()")).isEqualTo("customer@test.com")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/*[local-name()=\'contact-info\' and namespace-uri()=\'http://demo.com/contact-info\']/*[local-name()=\'name\']/text()")).isEqualTo("Krombopulous")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/*[local-name()=\'contact-info\' and namespace-uri()=\'http://demo.com/contact-info\']/*[local-name()=\'address\']/addr:gps/*[local-name()=\'lat\']/text()")).isEqualTo("51")')
			test.contains('assertThat(valueFromXPath(parsedXml, "/ns1:customer/*[local-name()=\'contact-info\' and namespace-uri()=\'http://demo.com/contact-info\']/*[local-name()=\'address\']/addr:gps/addr:lon/text()")).isEqualTo("50")')
			!test.contains('assertThat(valueFromXPath(parsedXml,"/ns1:customer/*[local-name()=\'contact-info\' and namespace-uri()=\'http://demo.com/contact-info\']/@xmlns")).isEqualTo"')
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
