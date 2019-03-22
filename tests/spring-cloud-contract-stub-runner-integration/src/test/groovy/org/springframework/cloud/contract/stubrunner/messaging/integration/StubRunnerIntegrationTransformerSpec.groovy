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

package org.springframework.cloud.contract.stubrunner.messaging.integration

import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

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
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(noOutputMessageContract) {
				@Override
				Contract matchingContract(Message<?> source) {
					return noOutputMessageContract
				}
			}
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
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(dsl) {
				@Override
				Contract matchingContract(Message<?> source) {
					return dsl
				}
			}
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
			body("""{"id":"${
				value(producer(regex('[0-9]+')), consumer('99'))
			}","temperature":"123.45"}""")
		}
	}

	def 'should convert dsl into message with regex in GString'() {
		given:
			StubRunnerIntegrationTransformer transformer = new StubRunnerIntegrationTransformer(dslWithRegexInGString) {
				@Override
				Contract matchingContract(Message<?> source) {
					return dslWithRegexInGString
				}
			}
		when:
			def result = transformer.transform(message)
		then:
			result.payload == '''{"id":"99","temperature":"123.45"}'''
	}
}
