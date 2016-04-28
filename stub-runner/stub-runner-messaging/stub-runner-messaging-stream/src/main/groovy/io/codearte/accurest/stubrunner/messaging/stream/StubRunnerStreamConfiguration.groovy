package io.codearte.accurest.stubrunner.messaging.stream

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubConfiguration
import io.codearte.accurest.stubrunner.spring.StubRunnerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.cloud.stream.config.BindingProperties
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties
import org.springframework.context.Lifecycle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.integration.dsl.FilterEndpointSpec
import org.springframework.integration.dsl.GenericEndpointSpec
import org.springframework.integration.dsl.IntegrationFlowBuilder
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.Message
/**
 * Spring Cloud Stream configuration that iterates over the downloaded Groovy DSLs
 * and registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@Import(StubRunnerConfiguration)
@CompileStatic
class StubRunnerStreamConfiguration {

	private static final Logger log = LoggerFactory.getLogger(StubRunnerStreamConfiguration)

	@Bean
	FlowRegistrar flowRegistrar(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner) {
		Map<StubConfiguration, Collection<GroovyDsl>> accurestContracts = batchStubRunner.accurestContracts
		accurestContracts.each { StubConfiguration key, Collection<GroovyDsl> value ->
			String name = "${key.groupId}_${key.artifactId}"
			value.findAll { it?.input?.messageFrom?.clientValue }.each { GroovyDsl dsl ->
				String flowName = "${name}_${dsl.label}_${dsl.hashCode()}"
				String from = resolvedDestination(beanFactory, dsl.input.messageFrom.clientValue)
				IntegrationFlowBuilder builder = IntegrationFlows.from(from)
						.filter(new StubRunnerStreamMessageSelector(dsl), { FilterEndpointSpec e -> e.id("${flowName}.filter") } )
						.transform(new StubRunnerStreamTransformer(dsl), { GenericEndpointSpec e -> e.id("${flowName}.transformer") })
				if (dsl.outputMessage?.sentTo) {
					builder = builder.channel(resolvedDestination(beanFactory, dsl.outputMessage.sentTo.clientValue))
				} else {
					builder = builder.handle(new DummyMessageHandler(), "handle")
				}
				beanFactory.initializeBean(builder.get(), flowName)
				beanFactory.getBean("${flowName}.filter", Lifecycle.class).start();
				beanFactory.getBean("${flowName}.transformer", Lifecycle.class).start();
			}
		}
		return new FlowRegistrar()
	}

	private String resolvedDestination(AutowireCapableBeanFactory context, String destination) {
		ChannelBindingServiceProperties channelBindingServiceProperties = context.getBean(ChannelBindingServiceProperties.class);
		String resolvedDestination = destination;
		for (Map.Entry<String, BindingProperties> entry : channelBindingServiceProperties.getBindings().entrySet()) {
			if (entry.getValue().getDestination().equals(destination)) {
				log.debug("Found a channel named [{}] with destination [{}]", entry.getKey(), destination);
				return entry.getKey();
			}
		}
		log.debug("No destination named [{}] was found. Assuming that the destination equals the channel name", destination);
		return resolvedDestination;
	}

	@CompileStatic
	private static class DummyMessageHandler {
		void handle(Message message) {}
	}

	static class FlowRegistrar {}
}
