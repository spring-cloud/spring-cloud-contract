package io.codearte.accurest.stubrunner.messaging.integration

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubConfiguration
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.Lifecycle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.FilterEndpointSpec
import org.springframework.integration.dsl.GenericEndpointSpec
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.stereotype.Service
/**
 * Spring Integration configuration that iterates over the downloaded Groovy DSLs
 * and registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
class StubRunnerIntegrationConfiguration {

	@Bean
	FlowRegistrar service(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<GroovyDsl>> accurestContracts = batchStubRunner.accurestContracts
		accurestContracts.each { StubConfiguration key, Collection<GroovyDsl> value ->
			String name = "${key.groupId}_${key.artifactId}"
			value.findAll { it?.input?.messageFrom && it?.outputMessage?.sentTo }.each { GroovyDsl dsl ->
				String flowName = "${name}_${dsl.label}_${dsl.hashCode()}"
				IntegrationFlow integrationFlow = IntegrationFlows.from(dsl.input.messageFrom)
					.filter(new StubRunnerIntegrationMessageSelector(dsl), { FilterEndpointSpec e -> e.id("${flowName}.filter") } )
					.transform(new StubRunnerIntegrationTransformer(dsl), { GenericEndpointSpec e -> e.id("${flowName}.transformer") })
					.channel(dsl.outputMessage.sentTo)
					.get()
				beanFactory.initializeBean(integrationFlow, flowName)
				beanFactory.getBean("${flowName}.filter", Lifecycle.class).start();
				beanFactory.getBean("${flowName}.transformer", Lifecycle.class).start();
			}
		}
		return new FlowRegistrar()
	}

	@Service
	static class FlowRegistrar {}
}
