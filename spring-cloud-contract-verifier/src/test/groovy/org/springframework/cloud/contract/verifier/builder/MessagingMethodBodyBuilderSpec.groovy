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

import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
class MessagingMethodBodyBuilderSpec extends Specification {

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Shared
	SingleTestGenerator.GeneratedClassData generatedClassData =
			new SingleTestGenerator.GeneratedClassData("foo", "com.example", new File(MessagingMethodBodyBuilderSpec.getResource(".").toURI()).toPath())

	def setup() {
		properties = new ContractVerifierConfigProperties(
				assertJsonSize: true,
				generatedTestSourcesDir: new File(MessagingMethodBodyBuilderSpec.getResource(".").toURI()),
				generatedTestResourcesDir: new File(MessagingMethodBodyBuilderSpec.getResource(".").toURI())
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

	def "should work for triggered based messaging with Spock"() {
		given:
// tag::trigger_method_dsl[]
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					triggeredBy('bookReturnedTriggered()')
				}
				outputMessage {
					sentTo('activemq:output')
					body('''{ "bookName" : "foo" }''')
					headers {
						header('BOOK-NAME', 'foo')
						messagingContentType(applicationJson())
					}
				}
			}
// end::trigger_method_dsl[]
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMessage =
// tag::trigger_method_test[]
					'''\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\tbookReturnedTriggered()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("activemq:output")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("BOOK-NAME") != null
\t\t\tresponse.getHeader("BOOK-NAME").toString() == 'foo'
\t\t\tresponse.getHeader("contentType") != null
\t\t\tresponse.getHeader("contentType").toString() == 'application/json'

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
\t}

}

'''
// end::trigger_method_test[]
			test.trim() == expectedMessage.trim()
	}

	def "should work for triggered based messaging with JUnit"() {
		given:
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					triggeredBy('bookReturnedTriggered()')
				}
				outputMessage {
					sentTo('activemq:output')
					body('''{ "bookName" : "foo" }''')
					headers {
						header('BOOK-NAME', 'foo')
						messagingContentType(applicationJson())
					}
				}
			}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMessage =
// tag::trigger_method_junit_test[]
					'''\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// when:
\t\t\tbookReturnedTriggered();

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("activemq:output");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("BOOK-NAME")).isNotNull();
\t\t\tassertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");
\t\t\tassertThat(response.getHeader("contentType")).isNotNull();
\t\t\tassertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
\t}

}

'''
// end::trigger_method_junit_test[]
			test.trim() == expectedMessage.trim()
	}

	def "should generate tests triggered by a message for Spock"() {
		given:
			// tag::trigger_message_dsl[]
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:input')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('jms:output')
					body([
							bookName: 'foo'
					])
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			// end::trigger_message_dsl[]
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMessage =
// tag::trigger_message_spock[]
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\tgiven:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t'''{"bookName":"foo"}'''
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t)

\t\twhen:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:input")

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("jms:output")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("BOOK-NAME") != null
\t\t\tresponse.getHeader("BOOK-NAME").toString() == 'foo'

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
\t}

}

"""
// end::trigger_message_spock[]
			test.trim() == expectedMessage.trim()
	}

	def "should generate tests triggered by a message for JUnit"() {
		given:
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:input')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('jms:output')
					body([
							bookName: 'foo'
					])
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMessage =
// tag::trigger_message_junit[]
					'''\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// given:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t"{\\"bookName\\":\\"foo\\"}"
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t);

\t\t// when:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:input");

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("BOOK-NAME")).isNotNull();
\t\t\tassertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
\t}

}

'''
// end::trigger_message_junit[]
			test.trim() == expectedMessage.trim()
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
			// tag::trigger_no_output_dsl[]
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:delete')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
					assertThat('bookWasDeleted()')
				}
			}
			// end::trigger_no_output_dsl[]
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
// tag::trigger_no_output_spock[]
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\tgiven:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t'''{"bookName":"foo"}'''
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t)

\t\twhen:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:delete")
\t\t\tbookWasDeleted()

\t\tthen:
\t\t\tnoExceptionThrown()
\t}

}
"""
// end::trigger_no_output_spock[]
			test.trim() == expectedMsg.trim()
	}

	def "should generate tests without destination, triggered by a message for JUnit"() {
		given:
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:delete')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
					assertThat('bookWasDeleted()')
				}
			}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
// tag::trigger_no_output_junit[]
					"""\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// given:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t"{\\"bookName\\":\\"foo\\"}"
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t);

\t\t// when:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:delete");
\t\t\tbookWasDeleted();

\t}

}

"""
// end::trigger_no_output_junit[]
			test.trim() == expectedMsg.trim()
	}

	def "should generate tests without headers for JUnit"() {
		given:
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:input')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('jms:output')
					body([
							bookName: 'foo'
					])
				}
			}
			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// given:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t"{\\"bookName\\":\\"foo\\"}"
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t);

\t\t// when:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:input");

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
\t}

}
"""
			test.trim() == expectedMsg.trim()
	}

	def "should generate tests without headers for Spock"() {
		given:
			def contractDsl = Contract.make {
				name "foo"
				label 'some_label'
				input {
					messageFrom('jms:input')
					messageBody([
							bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('jms:output')
					body([
							bookName: 'foo'
					])
				}
			}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\tgiven:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t'''{"bookName":"foo"}'''
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t)

\t\twhen:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:input")

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("jms:output")
\t\t\tresponse != null

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
\t}

}

"""
			test.trim() == expectedMsg.trim()
	}

	private String stripped(String text) {
		return text.stripIndent().stripMargin().replace('  ', '').replace('\n', '').replace('\t', '').replaceAll("\\W", "")
	}

	def "should generate tests without headers for JUnit with consumer / producer notation"() {
		given:
			def contractDsl =
					// tag::consumer_producer[]
					Contract.make {
				name "foo"
						label 'some_label'
						input {
							messageFrom value(consumer('jms:output'), producer('jms:input'))
							messageBody([
									bookName: 'foo'
							])
							messageHeaders {
								header('sample', 'header')
							}
						}
						outputMessage {
							sentTo $(consumer('jms:input'), producer('jms:output'))
							body([
									bookName: 'foo'
							])
						}
					}
			// end::consumer_producer[]

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					'''
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// given:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\t"{\\"bookName\\":\\"foo\\"}"
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("sample", "header")
\t\t\t);

\t\t// when:
\t\t\tcontractVerifierMessaging.send(inputMessage, "jms:input");

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
\t}

}
'''
			test.trim() == expectedMsg.trim()
	}

	@Issue("336")
	def "should generate tests with message headers containing regular expression for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo 'topic.rateablequote'
							headers {
								header('processId', value(producer(regex('[0-9]+')), consumer('123')))
							}
							body([
									eventId: value(producer(regex('[0-9]+')), consumer('1'))
							])
						}
					}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// when:
\t\t\trequestIsCalled();

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("processId")).isNotNull();
\t\t\tassertThat(response.getHeader("processId").toString()).matches("[0-9]+");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
\t}

}

"""
			test.trim() == expectedMsg.trim()
	}

	@Issue("567")
	def "should generate tests with message headers containing regular expression with backslashes for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo 'topic.rateablequote'
							headers {
								header('processId', value(producer(regex('\\d+')), consumer('123')))
							}
							body([
									eventId: value(producer(regex('\\d+')), consumer('1'))
							])
						}
					}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThat(response.getHeader("processId").toString()).matches("\\\\d+");')
	}


	@Issue('#587')
	def "should allow easier way of providing dynamic values for [#methodBuilderName]"() {
		given:
			//tag::regex_creating_props[]
			Contract contractDsl = Contract.make {
				name "foo"
				label 'trigger_event'
				input {
					triggeredBy('toString()')
				}
				outputMessage {
					sentTo 'topic.rateablequote'
					body([
							alpha            : $(anyAlphaUnicode()),
							number           : $(anyNumber()),
							anInteger        : $(anyInteger()),
							positiveInt      : $(anyPositiveInt()),
							aDouble          : $(anyDouble()),
							aBoolean         : $(aBoolean()),
							ip               : $(anyIpAddress()),
							hostname         : $(anyHostname()),
							email            : $(anyEmail()),
							url              : $(anyUrl()),
							httpsUrl         : $(anyHttpsUrl()),
							uuid             : $(anyUuid()),
							date             : $(anyDate()),
							dateTime         : $(anyDateTime()),
							time             : $(anyTime()),
							iso8601WithOffset: $(anyIso8601WithOffset()),
							nonBlankString   : $(anyNonBlankString()),
							nonEmptyString   : $(anyNonEmptyString()),
							anyOf            : $(anyOf('foo', 'bar'))
					])
				}
			}
			//end::regex_creating_props[]
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('assertThatJson(parsedJson).field("[\'aBoolean\']").matches("(true|false)")')
			test.contains('assertThatJson(parsedJson).field("[\'alpha\']").matches("[\\\\p{L}]*")')
			test.contains('assertThatJson(parsedJson).field("[\'hostname\']").matches("((http[s]?|ftp):/)/?([^:/\\\\s]+)(:[0-9]{1,5})?")')
			test.contains('assertThatJson(parsedJson).field("[\'url\']").matches("^(?:(?:[A-Za-z][+-.\\\\w^_]*:/{2})?(?:\\\\S+(?::\\\\S*)?@)?(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,}))|(?:localhost))(?::\\\\d{2,5})?(?:/\\\\S*)?)')
			test.contains('assertThatJson(parsedJson).field("[\'number\']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'anInteger\']").matches("-?(\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'positiveInt\']").matches("([1-9]\\\\d*)")')
			test.contains('assertThatJson(parsedJson).field("[\'aDouble\']").matches("-?(\\\\d*\\\\.\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'email\']").matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6}")')
			test.contains('assertThatJson(parsedJson).field("[\'ip\']").matches("([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])")')
			test.contains('assertThatJson(parsedJson).field("[\'httpsUrl\']").matches("^(?:https:/{2}(?:\\\\S+(?::\\\\S*)?@)?(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,}))|(?:localhost))(?::\\\\d{2,5})?(?:/\\\\S*)?)')
			test.contains('assertThatJson(parsedJson).field("[\'uuid\']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")')
			test.contains('assertThatJson(parsedJson).field("[\'date\']").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])')
			test.contains('assertThatJson(parsedJson).field("[\'dateTime\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])')
			test.contains('assertThatJson(parsedJson).field("[\'time\']").matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("[\'iso8601WithOffset\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.\\\\d{1,6})?(Z|[+-][01]\\\\d:[0-5]\\\\d)")')
			test.contains('assertThatJson(parsedJson).field("[\'nonBlankString\']").matches("^\\\\s*\\\\S[\\\\S\\\\s]*")')
			test.contains('assertThatJson(parsedJson).field("[\'nonEmptyString\']").matches("[\\\\S\\\\s]+")')
			test.contains('assertThatJson(parsedJson).field("[\'anyOf\']").matches("^foo' + endOfLineRegExSymbol + '|^bar' + endOfLineRegExSymbol + '")')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, test)
		where:
			methodBuilderName | methodBuilder                                      | endOfLineRegExSymbol
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | '\\$'
			"junit"           | { properties.testFramework = TestFramework.JUNIT } | '$'
	}

	@Issue("587")
	def "should generate tests with message headers containing regular expression with escapes for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo 'topic.rateablequote'
							headers {
								header('processId', value(producer(regex(nonEmpty())), consumer('123')))
							}
							body([
									eventId: value(producer(regex(nonEmpty())), consumer('1'))
							])
						}
					}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			test.contains('ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote")')
			test.contains('assertThat(response).isNotNull()')
			test.contains('assertThat(response.getHeader("processId")).isNotNull()')
			test.contains('assertThat(response.getHeader("processId").toString()).matches("[\\\\S\\\\s]+")')
			test.contains('DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))')
			test.contains('assertThatJson(parsedJson).field("[\'eventId\']").matches("[\\\\S\\\\s]+")')
	}

	@Issue("336")
	def "should generate tests with message headers containing regular expression for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo 'topic.rateablequote'
							headers {
								header('processId', value(producer(regex('[0-9]+')), consumer('123')))
							}
							body([
									eventId: value(producer(regex('[0-9]+')), consumer('1'))
							])
						}
					}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\trequestIsCalled()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("processId") != null
\t\t\tresponse.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[0-9]+')

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
\t}

}
"""
			test.trim() == expectedMsg.trim()
	}

	@Issue("587")
	def "should generate tests with message headers containing regular expression with escapes for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo 'topic.rateablequote'
							headers {
								header('processId', value(producer(regex(nonEmpty())), consumer('123')))
							}
							body([
									eventId: value(producer(regex(nonEmpty())), consumer('1'))
							])
						}
					}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\trequestIsCalled()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("processId") != null
\t\t\tresponse.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[\\\\S\\\\s]+')

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['eventId']").matches("[\\\\S\\\\s]+")
\t}

}
"""
			test.trim() == expectedMsg.trim()
	}

	@Issue("440")
	def "should generate tests with sentTo having a method execution for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo $(producer(execute("toString()")), consumer('topic.rateablequote'))
							headers {
								header('processId', value(producer(regex('[0-9]+')), consumer('123')))
							}
							body([
									eventId: value(producer(regex('[0-9]+')), consumer('1'))
							])
						}
					}
			properties.testFramework = TestFramework.SPOCK
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\trequestIsCalled()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive(toString())
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("processId") != null
\t\t\tresponse.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[0-9]+')

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
\t}

}
"""
			test.trim() == expectedMsg.trim()
	}

	@Issue("440")
	def "should generate tests with sentTo having a method execution for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
						label 'trigger_event'

						input {
							triggeredBy('requestIsCalled()')
						}

						outputMessage {
							sentTo $(producer(execute("toString()")), consumer('topic.rateablequote'))
							headers {
								header('processId', value(producer(regex('[0-9]+')), consumer('123')))
							}
							body([
									eventId: value(producer(regex('[0-9]+')), consumer('1'))
							])
						}
					}

			properties.testFramework = TestFramework.JUNIT
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			String expectedMsg =
					"""\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// when:
\t\t\trequestIsCalled();

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive(toString());
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("processId")).isNotNull();
\t\t\tassertThat(response.getHeader("processId").toString()).matches("[0-9]+");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
\t}

}
"""
			test.trim() == expectedMsg.trim()
	}

	@Issue('#620')
	def "should generate tests with message headers containing regular expression which compile for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				name "foo"
				label 'shouldPublishMessage'
				// input to the contract
				input {
					// the contract will be triggered by a methodBuilder
					triggeredBy('foo()')
				}
				// output message of the contract
				outputMessage {
					// destination to which the output message will be sent
					sentTo('messageExchange')
					// the body of the output message
					body([
							"field": "value"
					])
					headers {
						header('Authorization', value(regex('Bearer [A-Za-z0-9\\-\\._~\\+\\/]+=*')))
					}
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test.trim() == expectedTest.trim()
		where:
			methodBuilderName | methodBuilder                                      | expectedTest
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\tfoo()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("Authorization") != null
\t\t\tresponse.getHeader("Authorization").toString() ==~ java.util.regex.Pattern.compile('Bearer [A-Za-z0-9\\\\-\\\\._~\\\\+\\\\/]+=*')

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['field']").isEqualTo("value")
\t}

}
"""
			"junit"           | { properties.testFramework = TestFramework.JUNIT } | """\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// when:
\t\t\tfoo();

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("Authorization")).isNotNull();
\t\t\tassertThat(response.getHeader("Authorization").toString()).matches("Bearer [A-Za-z0-9\\\\-\\\\._~\\\\+\\\\/]+=*");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['field']").isEqualTo("value");
\t}

}
"""
	}

	@Issue('#664')
	def "should generate tests for messages having binary payloads [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				name "foo"
				label 'shouldPublishMessage'
				input {
					messageFrom("foo")
					messageBody(fileAsBytes("body_builder/request.pdf"))
					messageHeaders {
						messagingContentType(applicationOctetStream())
					}
				}
				outputMessage {
					sentTo('messageExchange')
					body(fileAsBytes("body_builder/response.pdf"))
					headers {
						messagingContentType(applicationOctetStream())
					}
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test.trim() == expectedTest.trim()
		where:
			methodBuilderName | methodBuilder                                      | expectedTest
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """\
package com.example

import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\tgiven:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\tfileToBytes(this, "foo_request_request.pdf")
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("contentType", "application/octet-stream")
\t\t\t)

\t\twhen:
\t\t\tcontractVerifierMessaging.send(inputMessage, "foo")

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("contentType") != null
\t\t\tresponse.getHeader("contentType").toString() == 'application/octet-stream'

\t\tand:
\t\t\tresponse.getPayloadAsByteArray() == fileToBytes(this, "foo_response_response.pdf")
\t}

}
"""
			"junit"           | { properties.testFramework = TestFramework.JUNIT } | """\
package com.example;

import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// given:
\t\t\tContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\t\tfileToBytes(this, "foo_request_request.pdf")
\t\t\t\t\t\t, headers()
\t\t\t\t\t\t\t.header("contentType", "application/octet-stream")
\t\t\t);

\t\t// when:
\t\t\tcontractVerifierMessaging.send(inputMessage, "foo");

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("contentType")).isNotNull();
\t\t\tassertThat(response.getHeader("contentType").toString()).isEqualTo("application/octet-stream");

\t\t// and:
\t\t\tassertThat(response.getPayloadAsByteArray()).isEqualTo(fileToBytes(this, "foo_response_response.pdf"));
\t}

}
"""
	}

	@Issue('#650')
	def "should generate output for message [#methodBuilderName]"() {
		given:
			Contract contractDsl = org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
				description 'issue #650'
				label 'trigger'
				input {
					triggeredBy('createNewPerson()')
				}
				outputMessage {
					sentTo 'personEventsTopic'
					headers {
						[
								header('contentType': 'application/json'),
								header('type': 'person'),
								header('eventType': 'PersonChangedEvent'),
								header('customerId': $(producer(regex(uuid()))))
						]
					}
					body([
							"type"      : 'CREATED',
							"personId"  : $(producer(regex(uuid())), consumer('0fd552ba-8043-42da-ab97-4fc77e1057c9')),
							"userId"    : $(producer(optional(regex(uuid()))), consumer('f043ccf1-0b72-423b-ad32-4ef123718897')),
							"firstName" : $(regex(nonEmpty())),
							"middleName": $(optional(regex(nonEmpty()))),
							"lastName"  : $(regex(nonEmpty())),
							"version"   : $(producer(regex(number())), consumer(0l)),
							"uid"       : $(producer(regex(uuid())))
					])
				}
			}
			methodBuilder()
		when:
			String test = singleTestGenerator(contractDsl)
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test.trim() == expectedTest.trim()
		where:
			methodBuilderName | methodBuilder                                      | expectedTest
			"spock"           | { properties.testFramework = TestFramework.SPOCK } | """\
package com.example

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification
import javax.inject.Inject
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes

@SuppressWarnings("rawtypes")
class FooSpec extends Specification {
\t@Inject ContractVerifierMessaging contractVerifierMessaging
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

\tdef validate_foo() throws Exception {
\t\twhen:
\t\t\tcreateNewPerson()

\t\tthen:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("personEventsTopic")
\t\t\tresponse != null

\t\tand:
\t\t\tresponse.getHeader("contentType") != null
\t\t\tresponse.getHeader("contentType").toString() == 'application/json'
\t\t\tresponse.getHeader("type") != null
\t\t\tresponse.getHeader("type").toString() == 'person'
\t\t\tresponse.getHeader("eventType") != null
\t\t\tresponse.getHeader("eventType").toString() == 'PersonChangedEvent'
\t\t\tresponse.getHeader("customerId") != null
\t\t\tresponse.getHeader("customerId").toString() ==~ java.util.regex.Pattern.compile('[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')

\t\tand:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
\t\t\tassertThatJson(parsedJson).field("['type']").isEqualTo("CREATED")
\t\t\tassertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
\t\t\tassertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?")
\t\t\tassertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+")
\t\t\tassertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?")
\t\t\tassertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+")
\t\t\tassertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")
\t\t\tassertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
\t}

}
"""
			"junit"           | { properties.testFramework = TestFramework.JUNIT } | """\
package com.example;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.Rule;
import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class FooTest {
\t@Inject ContractVerifierMessaging contractVerifierMessaging;
\t@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

\t@Test
\tpublic void validate_foo() throws Exception {
\t\t// when:
\t\t\tcreateNewPerson();

\t\t// then:
\t\t\tContractVerifierMessage response = contractVerifierMessaging.receive("personEventsTopic");
\t\t\tassertThat(response).isNotNull();

\t\t// and:
\t\t\tassertThat(response.getHeader("contentType")).isNotNull();
\t\t\tassertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");
\t\t\tassertThat(response.getHeader("type")).isNotNull();
\t\t\tassertThat(response.getHeader("type").toString()).isEqualTo("person");
\t\t\tassertThat(response.getHeader("eventType")).isNotNull();
\t\t\tassertThat(response.getHeader("eventType").toString()).isEqualTo("PersonChangedEvent");
\t\t\tassertThat(response.getHeader("customerId")).isNotNull();
\t\t\tassertThat(response.getHeader("customerId").toString()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

\t\t// and:
\t\t\tDocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
\t\t\tassertThatJson(parsedJson).field("['type']").isEqualTo("CREATED");
\t\t\tassertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
\t\t\tassertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?");
\t\t\tassertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+");
\t\t\tassertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?");
\t\t\tassertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+");
\t\t\tassertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)");
\t\t\tassertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
\t}

}
"""
	}
}
