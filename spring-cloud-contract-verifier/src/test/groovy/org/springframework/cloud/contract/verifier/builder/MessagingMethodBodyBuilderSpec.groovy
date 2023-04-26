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
					'''\
// tag::trigger_method_test[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			bookReturnedTriggered()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("activemq:output",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("BOOK-NAME") != null
			response.getHeader("BOOK-NAME").toString() == 'foo'
			response.getHeader("contentType") != null
			response.getHeader("contentType").toString() == 'application/json'

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
	}

}
// end::trigger_method_test[]
'''
			test.trim() == messageWithoutTags(expectedMessage, "trigger_method_test")
	}


	String messageWithoutTags(String message) {
		return messageWithoutTags(message, "invalidTag")
	}

	String messageWithoutTags(String message, String tagName) {
		return message.trim()
					   .replace("  ", "	")
					   .replace("\\	", "	")
					   .replace("// tag::${tagName}[]\n", "")
					   .replace("\n// end::${tagName}[]", "")
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
					'''\
// tag::trigger_method_junit_test[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			bookReturnedTriggered();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("activemq:output",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("BOOK-NAME")).isNotNull();
			assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");
			assertThat(response.getHeader("contentType")).isNotNull();
			assertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

}
// end::trigger_method_junit_test[]
'''
			test.trim() == messageWithoutTags(expectedMessage, "trigger_method_junit_test")
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
					"""\
// tag::trigger_message_spock[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					'''{"bookName":"foo"}'''
						, headers()
							.header("sample", "header")
			)

		when:
			contractVerifierMessaging.send(inputMessage, "jms:input",
					contract(this, "foo.yml"))

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("BOOK-NAME") != null
			response.getHeader("BOOK-NAME").toString() == 'foo'

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
	}

}
// end::trigger_message_spock[]
"""
			test.trim() == messageWithoutTags(expectedMessage, "trigger_message_spock")
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
					'''\
// tag::trigger_message_junit[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\\"bookName\\":\\"foo\\"}"
						, headers()
							.header("sample", "header")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "jms:input",
					contract(this, "foo.yml"));

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("BOOK-NAME")).isNotNull();
			assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

}
// end::trigger_message_junit[]
'''
			test.trim() == messageWithoutTags(expectedMessage, "trigger_message_junit")
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
			String expectedMessage =
					"""\
// tag::trigger_no_output_spock[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					'''{"bookName":"foo"}'''
						, headers()
							.header("sample", "header")
			)

		when:
			contractVerifierMessaging.send(inputMessage, "jms:delete",
					contract(this, "foo.yml"))
			bookWasDeleted()

		then:
			noExceptionThrown()
	}

}
// end::trigger_no_output_spock[]
"""
			test.trim() == messageWithoutTags(expectedMessage, "trigger_no_output_spock")
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
			String expectedMessage =
					"""\
// tag::trigger_no_output_junit[]
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\\"bookName\\":\\"foo\\"}"
						, headers()
							.header("sample", "header")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "jms:delete",
					contract(this, "foo.yml"));
			bookWasDeleted();

	}

}
// end::trigger_no_output_junit[]
"""
			test.trim() == messageWithoutTags(expectedMessage, "trigger_no_output_junit")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\\"bookName\\":\\"foo\\"}"
						, headers()
							.header("sample", "header")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "jms:input",
					contract(this, "foo.yml"));

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

}
"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					'''{"bookName":"foo"}'''
						, headers()
							.header("sample", "header")
			)

		when:
			contractVerifierMessaging.send(inputMessage, "jms:input",
					contract(this, "foo.yml"))

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "foo.yml"))
			response != null

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo")
	}

}

"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					"{\\"bookName\\":\\"foo\\"}"
						, headers()
							.header("sample", "header")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "jms:input",
					contract(this, "foo.yml"));

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['bookName']").isEqualTo("foo");
	}

}
'''
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			requestIsCalled();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("processId")).isNotNull();
			assertThat(response.getHeader("processId").toString()).matches("[0-9]+");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
	}

}

"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			test.contains('assertThatJson(parsedJson).field("[\'iso8601WithOffset\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.\\\\d+)?(Z|[+-][01]\\\\d:[0-5]\\\\d)")')
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
			test.contains('ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote"')
			test.contains('contract(this, "foo.yml"))')
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			requestIsCalled()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("processId") != null
			response.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[0-9]+')

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
	}

}
"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			requestIsCalled()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("processId") != null
			response.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[\\\\S\\\\s]+')

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['eventId']").matches("[\\\\S\\\\s]+")
	}

}
"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			requestIsCalled()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive(toString(),
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("processId") != null
			response.getHeader("processId").toString() ==~ java.util.regex.Pattern.compile('[0-9]+')

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
	}

}
"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
			String expectedMessage =
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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			requestIsCalled();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive(toString(),
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("processId")).isNotNull();
			assertThat(response.getHeader("processId").toString()).matches("[0-9]+");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
	}

}
"""
			test.trim() == messageWithoutTags(expectedMessage, "expectedMsg")
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			foo()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("Authorization") != null
			response.getHeader("Authorization").toString() ==~ java.util.regex.Pattern.compile('Bearer [A-Za-z0-9\\\\-\\\\._~\\\\+\\\\/]+=*')

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['field']").isEqualTo("value")
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			foo();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("Authorization")).isNotNull();
			assertThat(response.getHeader("Authorization").toString()).matches("Bearer [A-Za-z0-9\\\\-\\\\._~\\\\+\\\\/]+=*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['field']").isEqualTo("value");
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					fileToBytes(this, "foo_request_request.pdf")
						, headers()
							.header("contentType", "application/octet-stream")
			)

		when:
			contractVerifierMessaging.send(inputMessage, "foo",
					contract(this, "foo.yml"))

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("contentType") != null
			response.getHeader("contentType").toString() == 'application/octet-stream'

		and:
			response.getPayloadAsByteArray() == fileToBytes(this, "foo_response_response.pdf")
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// given:
			ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
					fileToBytes(this, "foo_request_request.pdf")
						, headers()
							.header("contentType", "application/octet-stream")
			);

		// when:
			contractVerifierMessaging.send(inputMessage, "foo",
					contract(this, "foo.yml"));

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("contentType")).isNotNull();
			assertThat(response.getHeader("contentType").toString()).isEqualTo("application/octet-stream");

		// and:
			assertThat(response.getPayloadAsByteArray()).isEqualTo(fileToBytes(this, "foo_response_response.pdf"));
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			createNewPerson()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("personEventsTopic",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("contentType") != null
			response.getHeader("contentType").toString() == 'application/json'
			response.getHeader("type") != null
			response.getHeader("type").toString() == 'person'
			response.getHeader("eventType") != null
			response.getHeader("eventType").toString() == 'PersonChangedEvent'
			response.getHeader("customerId") != null
			response.getHeader("customerId").toString() ==~ java.util.regex.Pattern.compile('[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')

		and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()))
			assertThatJson(parsedJson).field("['type']").isEqualTo("CREATED")
			assertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
			assertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?")
			assertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+")
			assertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?")
			assertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+")
			assertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")
			assertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			createNewPerson();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("personEventsTopic",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("contentType")).isNotNull();
			assertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");
			assertThat(response.getHeader("type")).isNotNull();
			assertThat(response.getHeader("type").toString()).isEqualTo("person");
			assertThat(response.getHeader("eventType")).isNotNull();
			assertThat(response.getHeader("eventType").toString()).isEqualTo("PersonChangedEvent");
			assertThat(response.getHeader("customerId")).isNotNull();
			assertThat(response.getHeader("customerId").toString()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['type']").isEqualTo("CREATED");
			assertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
			assertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?");
			assertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+");
			assertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?");
			assertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+");
			assertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)");
			assertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
	}

}
"""
	}

	@Issue('#1701')
	def "should generate output for message whose body is json and contains string that looks like json [#methodBuilderName]"() {
		given:
			Contract contractDsl = org.springframework.cloud.contract.spec.Contract.make {
				name "foo"
				description 'issue #650'
				label 'trigger'
				input {
					triggeredBy('toString()')
				}
				outputMessage {
					sentTo("foo")
					headers {
						messagingContentType(applicationJson())
					}
					body(fileAsBytes('messageResponse.json'))
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
	@Inject ContractVerifierMessaging contractVerifierMessaging
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper

	def validate_foo() throws Exception {
		when:
			toString()

		then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("foo",
					contract(this, "foo.yml"))
			response != null

		and:
			response.getHeader("contentType") != null
			response.getHeader("contentType").toString() == 'application/json'

		and:
			response.getPayloadAsByteArray() == fileToBytes(this, "foo_response_messageResponse.json")
	}

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
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_foo() throws Exception {
		// when:
			toString();

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("foo",
					contract(this, "foo.yml"));
			assertThat(response).isNotNull();

		// and:
			assertThat(response.getHeader("contentType")).isNotNull();
			assertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");

		// and:
			assertThat(response.getPayloadAsByteArray()).isEqualTo(fileToBytes(this, "foo_response_messageResponse.json"));
	}

}
"""
	}
}
