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

	MessagePact createFromContract(Contract contract) {
		MessagePactBuilder messagePactBuilder = MessagePactBuilder.consumer("Consumer")
				.hasPactWith("Provider")
				.given(getGiven(contract.input))
				.expectsToReceive(getOutcome(contract))
		if (contract.outputMessage) {
			OutputMessage message = contract.outputMessage
			if (message.body) {
				DslPart pactResponseBody = BodyConverter.toPactBody(message.body, clientValueExtractor)
				if (message.matchers?.bodyMatchers) {
					pactResponseBody.setMatchers(MatchingRulesConverter.matchingRulesForBody(message.matchers.bodyMatchers))
				}
				pactResponseBody.setGenerators(ValueGeneratorConverter.extract(message, { DslProperty dslProperty -> dslProperty.serverValue }))
				messagePactBuilder = messagePactBuilder.withContent(pactResponseBody)
			}
			if (message.headers) {
				messagePactBuilder = messagePactBuilder.withMetadata(getMetadata(message.headers))
			}
		}
		return messagePactBuilder.toPact()
	}

	private String getGiven(Input input) {
		if (input.triggeredBy) {
			return input.triggeredBy.executionCommand
		} else if (input.messageFrom) {
			return "received message from " + clientValueExtractor.call(input.messageFrom)
		} else {
			return ""
		}
	}

	private String getOutcome(Contract contract) {
		if (contract.outputMessage) {
			OutputMessage message = contract.outputMessage
			return "message sent to " + clientValueExtractor.call(message.sentTo)
		} else {
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
		} else {
			return v.toString()
		}
	}
}
