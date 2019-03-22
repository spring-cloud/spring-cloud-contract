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

package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.model.v3.messaging.MessagePact
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.verifier.util.ContentUtils

/**
 * Creator of {@link MessagePact} instances
 *
 * @author Tim Ysewyn
 * @since 2.0.0
 */
@CompileStatic
@PackageScope
class MessagePactCreator {

	private static final Closure clientValueExtractor = { DslProperty property -> property.clientValue }

	MessagePact createFromContract(List<Contract> contracts) {
		if (contracts.empty) {
			return null
		}
		Names names = NamingUtil.name(contracts.get(0))
		MessagePactBuilder pactBuilder = MessagePactBuilder.consumer(names.consumer)
														   .hasPactWith(names.producer)
		contracts.each { Contract contract ->
			pactBuilder = pactBuilder
					.given(getGiven(contract.input))
					.expectsToReceive(getOutcome(contract))
			if (contract.outputMessage) {
				OutputMessage message = contract.outputMessage
				if (message.body) {
					DslPart pactResponseBody = BodyConverter.
							toPactBody(message.body, clientValueExtractor)
					if (message.bodyMatchers) {
						pactResponseBody.setMatchers(MatchingRulesConverter.
								matchingRulesForBody(message.bodyMatchers))
					}
					pactResponseBody.setGenerators(ValueGeneratorConverter.
							extract(message, { DslProperty dslProperty -> dslProperty.serverValue }))
					pactBuilder = pactBuilder.withContent(pactResponseBody)
				}
				if (message.headers) {
					pactBuilder = pactBuilder.withMetadata(getMetadata(message.headers))
				}
			}
		}
		return pactBuilder.toPact()
	}

	private String getGiven(Input input) {
		if (input.triggeredBy) {
			return input.triggeredBy.executionCommand
		}
		else if (input.messageFrom) {
			return "received message from " + clientValueExtractor.call(input.messageFrom)
		}
		else {
			return ""
		}
	}

	private String getOutcome(Contract contract) {
		if (contract.outputMessage) {
			OutputMessage message = contract.outputMessage
			return "message sent to " + clientValueExtractor.call(message.sentTo)
		}
		else {
			return "assert that " + contract.input.assertThat.executionCommand
		}
	}

	private Map<String, String> getMetadata(Headers headers) {
		return headers.entries.collectEntries({ Header header ->
			return ["$header.name": extractValue(header)]
		})
	}

	private String extractValue(Object value) {
		Object v = value
		if (v instanceof DslProperty) {
			v = clientValueExtractor.call(v)
		}
		if (v instanceof GString) {
			v = ContentUtils.extractValue(v, clientValueExtractor)
		}
		if (v instanceof String) {
			return v
		}
		else {
			return v.toString()
		}
	}
}
