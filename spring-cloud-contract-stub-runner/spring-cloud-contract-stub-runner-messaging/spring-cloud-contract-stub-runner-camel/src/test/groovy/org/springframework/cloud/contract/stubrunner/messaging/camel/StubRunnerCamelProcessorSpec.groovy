package org.springframework.cloud.contract.stubrunner.messaging.camel

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.builder.ExchangeBuilder
import org.apache.camel.spring.SpringCamelContext
import org.springframework.cloud.contract.verifier.dsl.Contract
import spock.lang.Specification

class StubRunnerCamelProcessorSpec extends Specification {

	CamelContext camelContext = new SpringCamelContext()
	Exchange message = ExchangeBuilder.anExchange(camelContext).build()

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

	def 'should not process the message if there is no output message'() {
		given:
			StubRunnerCamelProcessor processor = new StubRunnerCamelProcessor(noOutputMessageContract)
		when:
			processor.process(message)
		then:
			noExceptionThrown()
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

	def 'should process message when it has an output message section'() {
		given:
			StubRunnerCamelProcessor processor = new StubRunnerCamelProcessor(dsl)
		when:
			processor.process(message)
		then:
			message.getIn().getBody(String) == '{"responseId":"123"}'
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
			StubRunnerCamelProcessor processor = new StubRunnerCamelProcessor(dslWithRegexInGString)
		when:
			processor.process(message)
		then:
			message.getIn().getBody(String) == '''{"id":"99","temperature":"123.45"}'''
	}
}
