package org.springframework.cloud.contract.stubrunner.messaging.stream

import org.springframework.cloud.contract.verifier.dsl.Contract
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import spock.lang.Specification

class StubRunnerStreamTransformerSpec extends Specification {

	Message message = MessageBuilder.withPayload("hello").build()

	def noOutputMessageContract = Contract.make {
		label 'return_book_2'
		input {
			messageFrom('bookStorage')
			messageBody([
					bookId: $(consumer(regex('[0-9]+')), producer('123'))
			])
			messageHeaders {
				header('sample', 'header')
			}
		}
	}

	def 'should not transform the message if there is no output message'() {
		given:
			StubRunnerStreamTransformer streamTransformer = new StubRunnerStreamTransformer(noOutputMessageContract)
		when:
			def result = streamTransformer.transform(message)
		then:
			result.is(message)
	}

	def dsl = Contract.make {
		label 'return_book_2'
		input {
			messageFrom('bookStorage')
			messageBody([
					bookId: $(consumer(regex('[0-9]+')), producer('123'))
			])
			messageHeaders {
				header('sample', 'header')
			}
		}
		outputMessage {
			sentTo('returnBook')
			body([
					responseId: $(producer(regex('[0-9]+')), consumer('123'))
			])
			headers {
				header('BOOK-NAME', 'foo')
			}
		}
	}

	def 'should convert dsl into message'() {
		given:
			StubRunnerStreamTransformer streamTransformer = new StubRunnerStreamTransformer(dsl)
		when:
			def result = streamTransformer.transform(message)
		then:
			result.payload == '{"responseId":"123"}'
	}
}
