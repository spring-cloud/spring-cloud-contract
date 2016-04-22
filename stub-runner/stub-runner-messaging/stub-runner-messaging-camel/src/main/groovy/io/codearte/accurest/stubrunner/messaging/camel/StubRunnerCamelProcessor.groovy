package io.codearte.accurest.stubrunner.messaging.camel

import groovy.transform.PackageScope
import io.codearte.accurest.builder.BodyAsString
import io.codearte.accurest.dsl.GroovyDsl
import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor

/**
 * Sends forward a message defined in the DSL. Also removes headers from the
 * input message and provides the headers from the DSL.
 *
 * @author Marcin Grzejszczak
 */
@PackageScope
class StubRunnerCamelProcessor implements Processor {

	private final GroovyDsl groovyDsl

	StubRunnerCamelProcessor(GroovyDsl groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	void process(Exchange exchange) throws Exception {
		Message input = exchange.in
		input.body = BodyAsString.extractClientValueFrom(groovyDsl.outputMessage.body)
		groovyDsl.input.messageHeaders.entries.each {
			input.removeHeader(it.name)
		}
		groovyDsl.outputMessage.headers.entries.each {
			input.setHeader(it.name, it.clientValue)
		}
	}
}
