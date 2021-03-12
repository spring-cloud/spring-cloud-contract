/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.kafka

import spock.lang.Specification

import org.springframework.cloud.contract.verifier.messaging.MessageVerifier
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage
import org.springframework.cloud.contract.verifier.messaging.kafka.ContractVerifierKafkaHelper
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

import static org.mockito.Mockito.mock

/**
 * Unit tests for {@link ContractVerifierKafkaHelper}.
 *
 * @author Chris Bono
 */
class ContractVerifierKafkaHelperSpec extends Specification {

	def "should convert message with no headers"() {
		given:
			Message message = MessageBuilder
					.withPayload("some-data")
					.build()
			ContractVerifierKafkaHelper contractVerifierKafkaHelper = new ContractVerifierKafkaHelper(mock(MessageVerifier.class))
		when:
			ContractVerifierMessage contractVerifierMessage = contractVerifierKafkaHelper.convert(message)
		then:
			contractVerifierMessage.payload == "some-data"
	}

	def "should convert message with basic header"() {
		given:
			Message message = MessageBuilder
					.withPayload("some-data")
					.setHeader("some-header", "5150")
					.build()
			ContractVerifierKafkaHelper contractVerifierKafkaHelper = new ContractVerifierKafkaHelper(mock(MessageVerifier.class))
		when:
			ContractVerifierMessage contractVerifierMessage = contractVerifierKafkaHelper.convert(message)
		then:
			contractVerifierMessage.payload == "some-data"
			contractVerifierMessage.headers.containsKey("some-header")
			contractVerifierMessage.headers.get("some-header") == "5150"
	}

	def "should convert message with byte[] header"() {
		given:
			Message message = MessageBuilder
					.withPayload("some-data")
					.setHeader("some-header", "5150".getBytes())
					.build()
			ContractVerifierKafkaHelper contractVerifierKafkaHelper = new ContractVerifierKafkaHelper(mock(MessageVerifier.class))
		when:
			ContractVerifierMessage contractVerifierMessage = contractVerifierKafkaHelper.convert(message)
		then:
			contractVerifierMessage.payload == "some-data"
			contractVerifierMessage.headers.containsKey("some-header")
			new String(contractVerifierMessage.headers.get("some-header")) == "5150"
	}
}
