package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class MessagingMethodBodyBuilderSpec extends Specification {

	def "should work for triggered based messaging with Spock"() {
		given:
		// tag::trigger_method_dsl[]
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_method_test[]
					'''
										when:
											 bookReturnedTriggered()

										then:
											 def response = accurestMessaging.receiveMessage('activemq:output')
											 response.getHeader('BOOK-NAME')  == 'foo'
										and:
											 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.payload))
											 assertThatJson(parsedJson).field("bookName").isEqualTo("foo")
											'''
					// end::trigger_method_test[]
			)
	}

	def "should work for triggered based messaging with JUnit"() {
		given:
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_method_junit_test[]
					'''
										// when:
											 bookReturnedTriggered();

										// then:
											 AccurestMessage response = accurestMessaging.receiveMessage("activemq:output");
											 assertThat(response.getHeader("BOOK-NAME")).isEqualTo("foo");
										// and:
											 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.getPayload()));
											 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
											'''
					// end::trigger_method_junit_test[]
			)
	}

	private String stripped(String text) {
		return text.stripIndent().stripMargin().replace('\t', '').replace('\n', '')
	}

	def "should generate tests triggered by a message for Spock"() {
		given:
		// tag::trigger_message_dsl[]
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_message_spock[]
					'''
											given:
												 def inputMessage = accurestMessaging.create(
													\'\'\'{"bookName":"foo"}\'\'\',
													['sample': 'header']
												)

											when:
												 accurestMessaging.send(inputMessage, 'jms:input')

											then:
												 def response = accurestMessaging.receiveMessage('jms:output')
												 response.getHeader('BOOK-NAME')  == 'foo'
											and:
												 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.payload))
												 assertThatJson(parsedJson).field("bookName").isEqualTo("foo")
												'''
					// end::trigger_message_spock[]
			)
	}

	def "should generate tests triggered by a message for JUnit"() {
		given:
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_message_junit[]
					'''
											// given:
												 AccurestMessage inputMessage = accurestMessaging.create(
													"{\\"bookName\\":\\"foo\\"}"
												, headers()
													.header("sample", "header"));

												// when:
												 accurestMessaging.send(inputMessage, "jms:input");

												// then:
												 AccurestMessage response = accurestMessaging.receiveMessage("jms:output");
												 assertThat(response.getHeader("BOOK-NAME")).isEqualTo("foo");
												// and:
												 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.getPayload()));
												 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");

												'''
					// end::trigger_message_junit[]
			)
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
		// tag::trigger_no_output_dsl[]
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_no_output_spock[]
					'''
											given:
												 def inputMessage = accurestMessaging.create(
													\'\'\'{"bookName":"foo"}\'\'\',
													['sample': 'header']
												)

											when:
												 accurestMessaging.send(inputMessage, 'jms:delete')

											then:
												 noExceptionThrown()
												 bookWasDeleted()
												'''
					// end::trigger_no_output_spock[]
			)
	}

	def "should generate tests without destination, triggered by a message for JUnit"() {
		given:
			def contractDsl = GroovyDsl.make {
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
			MethodBodyBuilder builder = new JUnitMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped(
					// tag::trigger_no_output_junit[]
					'''
											// given:
											 AccurestMessage inputMessage = accurestMessaging.create(
												"{\\"bookName\\":\\"foo\\"}"
											, headers()
												.header("sample", "header"));

											// when:
											 accurestMessaging.send(inputMessage, "jms:delete");

											// then:
											 bookWasDeleted();
												'''
					// end::trigger_no_output_junit[]
			)
	}

}
