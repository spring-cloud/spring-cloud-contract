package io.codearte.accurest.samples.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessaging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

// Context configuration would end up in base class
@ContextConfiguration(classes = [IntegrationMessagingApplication], loader = SpringApplicationContextLoader)
public class IntegrationMessagingApplicationSpec extends Specification {

	// ALL CASES
	@Inject AccurestMessaging accurestMessaging
	ObjectMapper accurestObjectMapper = new ObjectMapper()

	def "should work for triggered based messaging"() {
		given:
		// tag::method_trigger[]
			def dsl = GroovyDsl.make {
				// Human readable description
				description 'Some description'
				// Label by means of which the output message can be triggered
				label 'some_label'
				// input to the contract
				input {
					// the contract will be triggered by a method
					triggeredBy('bookReturnedTriggered()')
				}
				// output message of the contract
				outputMessage {
					// destination to which the output message will be sent
					sentTo('output')
					// the body of the output message
					body('''{ "bookName" : "foo" }''')
					// the headers of the output message
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
		// end::method_trigger[]
		// generated test should look like this:
		when:
			bookReturnedTriggered()
		then:
			def response = accurestMessaging.receiveMessage('output')
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests triggered by a message"() {
		given:
		// tag::message_trigger[]
			def dsl = GroovyDsl.make {
				description 'Some Description'
				label 'some_label'
				// input is a message
				input {
					// the message was received from this destination
					messageFrom('input')
					// has the following body
					messageBody([
					        bookName: 'foo'
					])
					// and the following headers
					messageHeaders {
						header('sample', 'header')
					}
				}
				outputMessage {
					sentTo('output')
					body([
					        bookName: 'foo'
					])
					headers {
						header('BOOK-NAME', 'foo')
					}
				}
			}
		// end::message_trigger[]

		// generated test should look like this:

		//given:
		AccurestMessage inputMessage = accurestMessaging.create(
				accurestObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header']
		)
		when:
			accurestMessaging.send(inputMessage, 'input')
		then:
			def response = accurestMessaging.receiveMessage('output')
			response.headers.get('BOOK-NAME')  == 'foo'
		and:
			DocumentContext parsedJson = JsonPath.parse(accurestObjectMapper.writeValueAsString(response.payload))
			JsonAssertion.assertThat(parsedJson).field('bookName').isEqualTo('foo')
	}

	def "should generate tests without destination, triggered by a message"() {
		given:
			def dsl = GroovyDsl.make {
				label 'some_label'
				input {
					messageFrom('delete')
					messageBody([
					        bookName: 'foo'
					])
					messageHeaders {
						header('sample', 'header')
					}
					assertThat('bookWasDeleted()')
				}
			}

		// generated test should look like this:

		//given:
		AccurestMessage inputMessage = accurestMessaging.create(
				accurestObjectMapper.writeValueAsString([bookName: 'foo']),
				[sample: 'header']
		)
		when:
			accurestMessaging.send(inputMessage, 'delete')
		then:
			noExceptionThrown()
			bookWasDeleted()
	}

	// BASE CLASS WOULD HAVE THIS:

	@Autowired BookService bookService
	@Autowired BookListener bookListener

	void bookReturnedTriggered() {
		bookService.returnBook(new BookReturned("foo"))
	}

	void bookWasDeleted() {
		assert bookListener.bookSuccessfulyDeleted.get()
	}

}