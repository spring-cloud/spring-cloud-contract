/*
 *  Copyright 2013-2016 the original author or authors.
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

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class MessagingMethodBodyBuilderSpec extends Specification {

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(assertJsonSize: true)
	
	def "should work for triggered based messaging with Spock"() {
		given:
// tag::trigger_method_dsl[]
def contractDsl = Contract.make {
	label 'some_label'
	input {
		triggeredBy('bookReturnedTriggered()')
	}
	outputMessage {
		sentTo('activemq:output')
		body('''{ "bookName" : "foo" }''')
		headers {
			header('BOOK-NAME', 'foo')
		}
	}
}
// end::trigger_method_dsl[]
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMessage =
// tag::trigger_method_test[]
'''
 when:
  bookReturnedTriggered()

 then:
  def response = contractVerifierMessaging.receiveMessage('activemq:output')
  assert response != null
  response.getHeader('BOOK-NAME')  == 'foo'
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("bookName").isEqualTo("foo")

'''
// end::trigger_method_test[]
		stripped(test) == stripped(expectedMessage)
	}

	def "should work for triggered based messaging with JUnit"() {
		given:
			def contractDsl = Contract.make {
				label 'some_label'
				input {
					triggeredBy('bookReturnedTriggered()')
				}
				outputMessage {
					sentTo('activemq:output')
					body('''{ "bookName" : "foo" }''')
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMessage =
// tag::trigger_method_junit_test[]
'''
 // when:
  bookReturnedTriggered();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receiveMessage("activemq:output");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("BOOK-NAME")).isEqualTo("foo");
 // and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
  assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
// end::trigger_method_junit_test[]
			stripped(test) == stripped(expectedMessage)
	}

	def "should generate tests triggered by a message for Spock"() {
		given:
		// tag::trigger_message_dsl[]
def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMessage =
// tag::trigger_message_spock[]
"""\
given:
   def inputMessage = contractVerifierMessaging.create(
    '''{"bookName":"foo"}''',
    ['sample': 'header']
  )

when:
   contractVerifierMessaging.send(inputMessage, 'jms:input')

then:
   def response = contractVerifierMessaging.receiveMessage('jms:output')
   assert response !- null
   response.getHeader('BOOK-NAME')  == 'foo'
and:
   DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
   assertThatJson(parsedJson).field("bookName").isEqualTo("foo")
"""
// end::trigger_message_spock[]
			stripped(test) == stripped(expectedMessage)
	}

	def "should generate tests triggered by a message for JUnit"() {
		given:
			def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMessage =
// tag::trigger_message_junit[]
'''
// given:
 ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
  "{\\"bookName\\":\\"foo\\"}"
, headers()
  .header("sample", "header"));

// when:
 contractVerifierMessaging.send(inputMessage, "jms:input");

// then:
 ContractVerifierMessage response = contractVerifierMessaging.receiveMessage("jms:output");
 assertThat(response).isNotNull();
 assertThat(response.getHeader("BOOK-NAME")).isEqualTo("foo");
// and:
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
// end::trigger_message_junit[]
			stripped(test) == stripped(expectedMessage)
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
		// tag::trigger_no_output_dsl[]
def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMsg =
// tag::trigger_no_output_spock[]
'''
given:
	 def inputMessage = contractVerifierMessaging.create(
		\'\'\'{"bookName":"foo"}\'\'\',
		['sample': 'header']
	)

when:
	 contractVerifierMessaging.send(inputMessage, 'jms:delete')

then:
	 noExceptionThrown()
	 bookWasDeleted()
'''
// end::trigger_no_output_spock[]
			stripped(test) == stripped(expectedMsg)
	}

	def "should generate tests without destination, triggered by a message for JUnit"() {
		given:
			def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMsg =
// tag::trigger_no_output_junit[]
'''
// given:
 ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
	"{\\"bookName\\":\\"foo\\"}"
, headers()
	.header("sample", "header"));

// when:
 contractVerifierMessaging.send(inputMessage, "jms:delete");

// then:
 bookWasDeleted();
'''
// end::trigger_no_output_junit[]
			stripped(test) == stripped(expectedMsg)
	}

	def "should generate tests without headers for JUnit"() {
		given:
			def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMsg =
'''
 // given:
  ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
      "{\\"bookName\\":\\"foo\\"}"
    , headers()
      .header("sample", "header"));

 // when:
  contractVerifierMessaging.send(inputMessage, "jms:input");

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receiveMessage("jms:output");
  assertThat(response).isNotNull();
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
			stripped(test) == stripped(expectedMsg)
	}

	def "should generate tests without headers for Spock"() {
		given:
			def contractDsl = Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
		String expectedMsg =
"""
 given:
  def inputMessage = contractVerifierMessaging.create('''{"bookName":"foo"}'''
		,[
			'sample': 'header'
		])

 when:
  contractVerifierMessaging.send(inputMessage, 'jms:input')

 then:
  def response = contractVerifierMessaging.receiveMessage('jms:output')
  assert response != null
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo")
"""
			stripped(test) == stripped(expectedMsg)
	}

	private String stripped(String text) {
		return text.stripIndent().stripMargin().replace('  ', '').replace('\n', '').replace('\t', '').replaceAll("\\W", "")
	}

	def "should generate tests without headers for JUnit with consumer / producer notation"() {
		given:
		def contractDsl =
		// tag::consumer_producer[]
Contract.make {
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
		MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
		BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
		builder.appendTo(blockBuilder)
		def test = blockBuilder.toString()
		then:
		String expectedMsg =
				'''
 // given:
  ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
      "{\\"bookName\\":\\"foo\\"}"
    , headers()
      .header("sample", "header"));

 // when:
  contractVerifierMessaging.send(inputMessage, "jms:input");

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receiveMessage("jms:output");
  assertThat(response).isNotNull();
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
		stripped(test) == stripped(expectedMsg)
	}

}
