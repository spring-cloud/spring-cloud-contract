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

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
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
			messagingContentType(applicationJson())
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
  ContractVerifierMessage response = contractVerifierMessaging.receive('activemq:output')
  assert response != null
  response.getHeader('BOOK-NAME')?.toString()  == 'foo'
  response.getHeader('contentType')?.toString()  == 'application/json'
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
						messagingContentType(applicationJson())
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
  ContractVerifierMessage response = contractVerifierMessaging.receive("activemq:output");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("BOOK-NAME")).isNotNull();
  assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");
  assertThat(response.getHeader("contentType")).isNotNull();
  assertThat(response.getHeader("contentType").toString()).isEqualTo("application/json");
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
   ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
    '''{"bookName":"foo"}''',
    ['sample': 'header']
  )

when:
   contractVerifierMessaging.send(inputMessage, 'jms:input')

then:
   ContractVerifierMessage response = contractVerifierMessaging.receive('jms:output')
   assert response !- null
   response.getHeader('BOOK-NAME')?.toString()  == 'foo'
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
 ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
 assertThat(response).isNotNull();
 assertThat(response.getHeader("BOOK-NAME")).isNotNull();
 assertThat(response.getHeader("BOOK-NAME").toString()).isEqualTo("foo");
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
	 ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
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
  ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
  assertThat(response).isNotNull();
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
			stripped(test) == stripped(expectedMsg)
		and: 'indents are maintained'
			test.contains("  ContractVerifierMessage response")
			test.contains("  DocumentContext parsedJson")
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
  ContractVerifierMessage inputMessage = contractVerifierMessaging.create('''{"bookName":"foo"}'''
		,[
			'sample': 'header'
		])

 when:
  contractVerifierMessaging.send(inputMessage, 'jms:input')

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('jms:output')
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
  ContractVerifierMessage response = contractVerifierMessaging.receive("jms:output");
  assertThat(response).isNotNull();
 DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("336")
	def "should generate tests with message headers containing regular expression for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
				'''
  // when:
  requestIsCalled();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("processId")).isNotNull();
  assertThat(response.getHeader("processId").toString()).matches("[0-9]+");
 // and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
  assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("567")
	def "should generate tests with message headers containing regular expression with backslashes for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
    		test.contains('assertThat(response.getHeader("processId").toString()).matches("\\d+");')
	}


	@Issue('#587')
	def "should allow easier way of providing dynamic values for [#methodBuilderName]"() {
		given:
		Contract contractDsl = Contract.make {
			label 'trigger_event'
			input {
				triggeredBy('toString()')
			}
			outputMessage {
				sentTo 'topic.rateablequote'
				body([
						alpha: $(anyAlphaUnicode()),
						number: $(anyNumber()),
						aBoolean: $(aBoolean()),
						ip: $(anyIpAddress()),
						hostname: $(anyHostname()),
						email: $(anyEmail()),
						url: $(anyUrl()),
						uuid: $(anyUuid()),
						date: $(anyDate()),
						dateTime: $(anyDateTime()),
						time: $(anyTime()),
						iso8601WithOffset: $(anyIso8601WithOffset()),
						nonBlankString: $(anyNonBlankString()),
						nonEmptyString: $(anyNonEmptyString()),
						anyOf: $(anyOf('foo', 'bar'))
				])
			}
		}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThatJson(parsedJson).field("[\'aBoolean\']").matches("(true|false)")')
			test.contains('assertThatJson(parsedJson).field("[\'alpha\']").matches("[\\\\p{L}]*")')
			test.contains('assertThatJson(parsedJson).field("[\'hostname\']").matches("((http[s]?|ftp):/)/?([^:/\\\\s]+)(:[0-9]{1,5})?")')
			test.contains('assertThatJson(parsedJson).field("[\'url\']").matches("^(?:(?:[A-Za-z][+-.\\\\w^_]*:/{2})?(?:\\\\S+(?::\\\\S*)?@)?(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,})))(?::\\\\d{2,5})?(?:/\\\\S*)?)')
			test.contains('assertThatJson(parsedJson).field("[\'number\']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")')
			test.contains('assertThatJson(parsedJson).field("[\'email\']").matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6}")')
			test.contains('assertThatJson(parsedJson).field("[\'ip\']").matches("([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])\\\\.([01]?\\\\d\\\\d?|2[0-4]\\\\d|25[0-5])")')
			test.contains('assertThatJson(parsedJson).field("[\'uuid\']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")')
			test.contains('assertThatJson(parsedJson).field("[\'date\']").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])')
			test.contains('assertThatJson(parsedJson).field("[\'dateTime\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])')
			test.contains('assertThatJson(parsedJson).field("[\'time\']").matches("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])")')
			test.contains('assertThatJson(parsedJson).field("[\'iso8601WithOffset\']").matches("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\\\.\\\\d{3})?(Z|[+-][01]\\\\d:[0-5]\\\\d)")')
			test.contains('assertThatJson(parsedJson).field("[\'nonBlankString\']").matches("^\\\\s*\\\\S[\\\\S\\\\s]*")')
			test.contains('assertThatJson(parsedJson).field("[\'nonEmptyString\']").matches("[\\\\S\\\\s]+")')
			test.contains('assertThatJson(parsedJson).field("[\'anyOf\']").matches("^foo' + endOfLineRegExSymbol + '|^bar' + endOfLineRegExSymbol + '")')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			String jsonSample = '''\
	String json = "{\\"shouldFail\\":123,\\"duck\\":\\"8\\",\\"allpha\\":\\"YAJEOWYGMFBEWPMEMAZI\\",\\"number\\":-2095030871,\\"aBoolean\\":true,\\"ip\\":\\"129.168.99.100\\",\\"hostname\\":\\"http://foo389886219.com\\",\\"email\\":\\"foo@bar1367573183.com\\",\\"url\\":\\"http://foo-597104692.com\\",\\"uuid\\":\\"e436b817-b764-49a2-908e-967f2f99eb9f\\",\\"date\\":\\"2014-04-14\\",\\"dateTime\\":\\"2011-01-11T12:23:34\\",\\"time\\":\\"12:20:30\\",\\"iso8601WithOffset\\":\\"2015-05-15T12:23:34.123Z\\",\\"nonBlankString\\":\\"EPZWVIRHSUAPBJMMQSFO\\",\\"nonEmptyString\\":\\"RVMFDSEQFHRQFVUVQPIA\\",\\"anyOf\\":\\"foo\\"}";
	DocumentContext parsedJson = JsonPath.parse(json);
	'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine { if (it.contains("assertThatJson")) lines << it else it }
			lines.addFirst(jsonSample)
			lines.addLast('''assertThatJson(parsedJson).field("['shouldFail']").matches("(\\\\d\\\\d\\\\d\\\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");''')
			String assertionsOnly = lines.join("\n")
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, assertionsOnly)
		when:
			SyntaxChecker.tryToRun(methodBuilderName, assertionsOnly)
		then:
			Exception error = thrown(Exception)
			(error.message ? error.message : error.cause.message).contains('''doesn't match the JSON path [$[?(@.['shouldFail'] =~ ''')
		where:
			methodBuilderName                 | methodBuilder                                                            | endOfLineRegExSymbol
			"SpockMessagingMethodBodyBuilder" | { Contract dsl -> new SpockMessagingMethodBodyBuilder(dsl, properties) } | '\\$'
			"JUnitMessagingMethodBodyBuilder" | { Contract dsl -> new JUnitMessagingMethodBodyBuilder(dsl, properties) } | '$'
	}

	@Issue("587")
	def "should generate tests with message headers containing regular expression with escapes for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
				'''
  // when:
  requestIsCalled();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive("topic.rateablequote");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("processId")).isNotNull();
  assertThat(response.getHeader("processId").toString()).matches("[\\S\\s]+");
 // and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
  assertThatJson(parsedJson).field("['eventId']").matches("[\\S\\s]+");
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("336")
	def "should generate tests with message headers containing regular expression for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
				'''
  when:
  requestIsCalled()

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('topic.rateablequote')
  assert response != null
  response.getHeader('processId')?.toString() ==~ java.util.regex.Pattern.compile('[0-9]+')
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("587")
	def "should generate tests with message headers containing regular expression with escapes for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
				'''
  when:
  requestIsCalled()

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('topic.rateablequote')
  assert response != null
  response.getHeader('processId')?.toString() ==~ java.util.regex.Pattern.compile('[\\S\\s]+')
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("['eventId']").matches("[\\S\\s]+")
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("440")
	def "should generate tests with sentTo having a method execution for Spock"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
				'''
  when:
  requestIsCalled()

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive(toString())
  assert response != null
  response.getHeader('processId')?.toString() ==~ java.util.regex.Pattern.compile('[0-9]+')
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+")
'''
		stripped(test) == stripped(expectedMsg)
	}

	@Issue("440")
	def "should generate tests with sentTo having a method execution for JUnit"() {
		given:
			def contractDsl =
					org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			String expectedMsg =
					'''
  // when:
  requestIsCalled();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive(toString());
  assertThat(response).isNotNull();
  assertThat(response.getHeader("processId")).isNotNull();
  assertThat(response.getHeader("processId").toString()).matches("[0-9]+");
 // and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
  assertThatJson(parsedJson).field("['eventId']").matches("[0-9]+");
'''
			stripped(test) == stripped(expectedMsg)
	}

}
