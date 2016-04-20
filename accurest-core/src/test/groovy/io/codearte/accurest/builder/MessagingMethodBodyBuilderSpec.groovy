package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class MessagingMethodBodyBuilderSpec extends Specification {

	def "should work for triggered based messaging with Spock"() {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped('''
										when:
											 bookReturnedTriggered()

										then:
											 def response = accurestMessaging.receiveMessage('activemq:output')
											 response.getHeader('BOOK-NAME')  == 'foo'
										and:
											 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.payload))
											 assertThatJson(parsedJson).field("bookName").isEqualTo("foo")
											''')
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
			stripped(test) == stripped('''
										// when:
											 bookReturnedTriggered();

										// then:
											 AccurestMessage response = accurestMessaging.receiveMessage("activemq:output");
											 assertThat(response.getHeader("BOOK-NAME")).isEqualTo("foo");
										// and:
											 DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.getPayload()));
											 assertThatJson(parsedJson).field("bookName").isEqualTo("foo");
											''')
	}

	private String stripped(String text) {
		return text.stripIndent().stripMargin().replace('\t', '').replace('\n', '')
	}

	def "should generate tests triggered by a message for Spock"() {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped('''
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
												''')
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
			stripped(test) == stripped('''
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

												''')
	}

	def "should generate tests without destination, triggered by a message"() {
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
			MethodBodyBuilder builder = new SpockMessagingMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			stripped(test) == stripped('''
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
												''')
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
			stripped(test) == stripped('''
											// given:
											 AccurestMessage inputMessage = accurestMessaging.create(
												"{\\"bookName\\":\\"foo\\"}"
											, headers()
												.header("sample", "header"));

											// when:
											 accurestMessaging.send(inputMessage, "jms:delete");

											// then:
											 bookWasDeleted();
												''')
	}

}
