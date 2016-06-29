package org.springframework.cloud.contract.stubrunner.messaging.integration

import org.springframework.cloud.contract.spec.Contract
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import spock.lang.Specification

class StubRunnerIntegrationTransformerSpec extends Specification {

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
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(noOutputMessageContract)
		when:
			def result = transformer.transform(message)
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
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(dsl)
		when:
			def result = transformer.transform(message)
		then:
			result.payload == '{"responseId":"123"}'
	}

	def dslWithRegexInGString = Contract.make {
		// Human readable description
		description 'Should produce valid sensor data'
		// Label by means of which the output message can be triggered
		label 'sensor1'
		// input to the contract
		input {
			// the contract will be triggered by a method
			triggeredBy('createSensorData()')
		}
		// output message of the contract
		outputMessage {
			// destination to which the output message will be sent
			sentTo 'sensor-data'
			headers {
				header('contentType': 'application/json')
			}
			// the body of the output message
			body("""{"id":"${value(producer(regex('[0-9]+')), consumer('99'))}","temperature":"123.45"}""")
		}
	}

	def 'should convert dsl into message with regex in GString'() {
		given:
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(dslWithRegexInGString)
		when:
			def result = transformer.transform(message)
		then:
			result.payload == '''{"id":"99","temperature":"123.45"}'''
	}
}
