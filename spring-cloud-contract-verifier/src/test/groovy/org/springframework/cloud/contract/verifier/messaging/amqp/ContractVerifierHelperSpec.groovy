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

package org.springframework.cloud.contract.verifier.messaging.amqp

import spock.lang.Specification

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessagePropertiesBuilder
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage

import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON

/**
 * @author Mathias Düsterhöft
 */
class ContractVerifierHelperSpec extends Specification {

	def "should convert message"() {
		given:
			String payload = '''{"name":"some"}'''
			Message message = MessageBuilder
					.withBody(payload.bytes)
					.andProperties(MessagePropertiesBuilder.newInstance()
														   .setHeader("my-header", "some")
														   .setContentType(CONTENT_TYPE_JSON)
														   .build()).build()
			ContractVerifierHelper contractVerifierHelper = new ContractVerifierHelper(null, new Jackson2JsonMessageConverter())
		when:
			ContractVerifierMessage contractVerifierMessage = contractVerifierHelper.convert(message)
		then:
			((Map) contractVerifierMessage.payload).containsKey("name")
			contractVerifierMessage.headers.containsKey("contentType")
			contractVerifierMessage.headers.containsKey("my-header")
	}
}
