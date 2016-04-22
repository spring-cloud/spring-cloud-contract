package io.codearte.accurest.stubrunner.messaging.integration

import io.codearte.accurest.builder.BodyAsString
import io.codearte.accurest.dsl.GroovyDsl
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder

/**
 * Sends forward a message defined in the DSL.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerIntegrationTransformer implements GenericTransformer<Message<?>, Message<?>> {

	private final GroovyDsl groovyDsl

	StubRunnerIntegrationTransformer(GroovyDsl groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	Message<?> transform(Message<?> source) {
		String payload = BodyAsString.extractClientValueFrom(groovyDsl.outputMessage.body)
		Map<String, Object> headers = groovyDsl.outputMessage.headers.asStubSideMap()
		return MessageBuilder.createMessage(payload, new MessageHeaders(headers))
	}
}
