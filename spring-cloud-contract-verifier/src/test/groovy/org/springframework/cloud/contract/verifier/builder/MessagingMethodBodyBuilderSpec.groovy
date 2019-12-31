/*
 * Copyright 2013-2019 the original author or authors.
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
import org.springframework.cloud.contract.verifier.util.SyntaxChecker

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
class MessagingMethodBodyBuilderSpec extends Specification {

	@Shared
	ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(assertJsonSize: true, generatedTestSourcesDir: new File("."),
			generatedTestResourcesDir: new File("."))
	@Shared
	GeneratedClassDataForMethod generatedClassDataForMethod = new GeneratedClassDataForMethod(
			new SingleTestGenerator.GeneratedClassData("foo", "bar", new File("target/test.java").toPath()), "method")

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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('assertThat(response.getHeader("processId").toString()).matches("\\\\d+");')
	}


	@Issue('#587')
	def "should allow easier way of providing dynamic values for [#methodBuilderName]"() {
		given:
			//tag::regex_creating_props[]
			Contract contractDsl = Contract.make {
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
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
			String jsonSample = '''\
	String json = "{\\"shouldFail\\":123,\\"duck\\":\\"8\\",\\"alpha\\":\\"YAJEOWYGMFBEWPMEMAZI\\",\\"number\\":-2095030871,\\"anInteger\\":1780305902,\\"positiveInt\\":345,\\"aDouble\\":42.345,\\"aBoolean\\":true,\\"ip\\":\\"129.168.99.100\\",\\"hostname\\":\\"https://foo389886219.com\\",\\"email\\":\\"foo@bar1367573183.com\\",\\"url\\":\\"https://foo-597104692.com\\",\\"httpsUrl\\":\\"https://baz-486093581.com\\",\\"uuid\\":\\"e436b817-b764-49a2-908e-967f2f99eb9f\\",\\"date\\":\\"2014-04-14\\",\\"dateTime\\":\\"2011-01-11T12:23:34\\",\\"time\\":\\"12:20:30\\",\\"iso8601WithOffset\\":\\"2015-05-15T12:23:34.123Z\\",\\"nonBlankString\\":\\"EPZWVIRHSUAPBJMMQSFO\\",\\"nonEmptyString\\":\\"RVMFDSEQFHRQFVUVQPIA\\",\\"anyOf\\":\\"foo\\"}";
	DocumentContext parsedJson = JsonPath.parse(json);
	'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine {
				if (it.contains("assertThatJson")) {
					lines << it
				}
				else {
					it
				}
			}
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
			methodBuilderName                 | methodBuilder                                                                                         | endOfLineRegExSymbol
			"SpockMessagingMethodBodyBuilder" | { Contract dsl -> new SpockMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '\\$'
			"JUnitMessagingMethodBodyBuilder" | { Contract dsl -> new JUnitMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | '$'
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl, properties, generatedClassDataForMethod)
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

	@Issue('#620')
	def "should generate tests with message headers containing regular expression which compile for [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
				label 'shouldPublishMessage'
				// input to the contract
				input {
					// the contract will be triggered by a method
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test == expectedTest
		where:
			methodBuilderName                 | methodBuilder                                                                                         | expectedTest
			"SpockMessagingMethodBodyBuilder" | { Contract dsl -> new SpockMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' when:
  foo()

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('messageExchange')
  assert response != null
  response.getHeader('Authorization')?.toString() ==~ java.util.regex.Pattern.compile('Bearer [A-Za-z0-9-._~+/]+=*')
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("['field']").isEqualTo("value")
'''
			"JUnitMessagingMethodBodyBuilder" | { Contract dsl -> new JUnitMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' // when:
  foo();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("Authorization")).isNotNull();
  assertThat(response.getHeader("Authorization").toString()).matches("Bearer [A-Za-z0-9\\\\-\\\\._~\\\\+\\\\/]+=*");
 // and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
  assertThatJson(parsedJson).field("['field']").isEqualTo("value");
'''
	}

	@Issue('#664')
	def "should generate tests for messages having binary payloads [#methodBuilderName]"() {
		given:
			Contract contractDsl = Contract.make {
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test == expectedTest
		where:
			methodBuilderName                 | methodBuilder                                                                                         | expectedTest
			"SpockMessagingMethodBodyBuilder" | { Contract dsl -> new SpockMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' given:
  ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
      fileToBytes(this, "method_request_request.pdf")

    ,[
      "contentType": "application/octet-stream"
    ])

 when:
  contractVerifierMessaging.send(inputMessage, 'foo')

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('messageExchange')
  assert response != null
  response.getHeader('contentType')?.toString()  == 'application/octet-stream\'
 and:
  response.payloadAsByteArray == fileToBytes(this, "method_response_response.pdf")
'''
			"JUnitMessagingMethodBodyBuilder" | { Contract dsl -> new JUnitMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' // given:
  ContractVerifierMessage inputMessage = contractVerifierMessaging.create(
\t\t\t\tfileToBytes(this, "method_request_request.pdf")
\t\t\t\t, headers()
\t\t\t\t\t\t.header("contentType", "application/octet-stream")
\t\t\t);

 // when:
  contractVerifierMessaging.send(inputMessage, "foo");

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive("messageExchange");
  assertThat(response).isNotNull();
  assertThat(response.getHeader("contentType")).isNotNull();
  assertThat(response.getHeader("contentType").toString()).isEqualTo("application/octet-stream");
 // and:
  assertThat(response.getPayloadAsByteArray()).isEqualTo(fileToBytes(this, "method_response_response.pdf"));
'''
	}

	@Issue('#650')
	def "should generate output for message [#methodBuilderName]"() {
		given:
			Contract contractDsl = org.springframework.cloud.contract.spec.Contract.make {
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
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			!test.contains('cursor')
			!test.contains('REGEXP>>')
			test == expectedTest
		where:
			methodBuilderName                 | methodBuilder                                                                                         | expectedTest
			"SpockMessagingMethodBodyBuilder" | { Contract dsl -> new SpockMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' when:
  createNewPerson()

 then:
  ContractVerifierMessage response = contractVerifierMessaging.receive('personEventsTopic')
  assert response != null
  response.getHeader('contentType')?.toString()  == 'application/json'
  response.getHeader('type')?.toString()  == 'person'
  response.getHeader('eventType')?.toString()  == 'PersonChangedEvent'
  response.getHeader('customerId')?.toString() ==~ java.util.regex.Pattern.compile('[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')
 and:
  DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.payload))
  assertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+")
  assertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?")
  assertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?")
  assertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)")
  assertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
  assertThatJson(parsedJson).field("['type']").isEqualTo("CREATED")
  assertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
  assertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+")
'''
			"JUnitMessagingMethodBodyBuilder" | { Contract dsl -> new JUnitMessagingMethodBodyBuilder(dsl, properties, generatedClassDataForMethod) } | ''' // when:
  createNewPerson();

 // then:
  ContractVerifierMessage response = contractVerifierMessaging.receive("personEventsTopic");
  assertThat(response).isNotNull();
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
  assertThatJson(parsedJson).field("['lastName']").matches("[\\\\S\\\\s]+");
  assertThatJson(parsedJson).field("['userId']").matches("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})?");
  assertThatJson(parsedJson).field("['middleName']").matches("([\\\\S\\\\s]+)?");
  assertThatJson(parsedJson).field("['version']").matches("-?(\\\\d*\\\\.\\\\d+|\\\\d+)");
  assertThatJson(parsedJson).field("['uid']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
  assertThatJson(parsedJson).field("['type']").isEqualTo("CREATED");
  assertThatJson(parsedJson).field("['personId']").matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
  assertThatJson(parsedJson).field("['firstName']").matches("[\\\\S\\\\s]+");
'''
	}
}
