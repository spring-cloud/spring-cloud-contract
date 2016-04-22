package io.codearte.accurest.stubrunner.messaging.camel

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubConfiguration
import org.apache.camel.RoutesBuilder
import org.apache.camel.spring.SpringRouteBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Camel configuration that iterates over the downloaded Groovy DSLs
 * and registers a route for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
class StubRunnerCamelConfiguration {

	@Bean
	RoutesBuilder myRouter(BatchStubRunner batchStubRunner) {
		return new SpringRouteBuilder() {
			@Override
			public void configure() throws Exception {
				Map<StubConfiguration, Collection<GroovyDsl>> accurestContracts = batchStubRunner.accurestContracts
				(accurestContracts.values().flatten() as Collection<GroovyDsl>).findAll { it?.input?.messageFrom && it?.outputMessage?.sentTo }.each {
					from(it.input.messageFrom)
							.filter(new StubRunnerCamelPredicate(it))
							.process(new StubRunnerCamelProcessor(it))
							.to(it.outputMessage.sentTo)
				}
			}
		};
	}
}
