package io.codearte.accurest.stubrunner.messaging.stream

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubConfiguration
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.binding.ChannelBindingService
import org.springframework.context.Lifecycle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.FilterEndpointSpec
import org.springframework.integration.dsl.GenericEndpointSpec
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Service
/**
 * Spring Cloud Stream configuration that iterates over the downloaded Groovy DSLs
 * and registers a flow for each DSL.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@EnableBinding
class StubRunnerStreamConfiguration {

	@Bean
	FlowRegistrar service(AutowireCapableBeanFactory beanFactory, BatchStubRunner batchStubRunner, ChannelBindingService channelBindingService) {
		Map<StubConfiguration, Collection<GroovyDsl>> accurestContracts = batchStubRunner.accurestContracts
		accurestContracts.each { StubConfiguration key, Collection<GroovyDsl> value ->
			String name = "${key.groupId}_${key.artifactId}"
			value.findAll { it?.input?.messageFrom && it?.outputMessage?.sentTo }.each { GroovyDsl dsl ->
				String flowName = "${name}_${dsl.label}_${dsl.hashCode()}"
				IntegrationFlow integrationFlow = IntegrationFlows.from(dsl.input.messageFrom)
					.filter(new StubRunnerStreamMessageSelector(dsl), { FilterEndpointSpec e -> e.id("${flowName}.filter") } )
					.transform(new StubRunnerStreamTransformer(dsl), { GenericEndpointSpec e -> e.id("${flowName}.transformer") })
					.channel(dsl.outputMessage.sentTo)
					.get()
				beanFactory.initializeBean(integrationFlow, flowName)
				beanFactory.getBean("${flowName}.filter", Lifecycle.class).start();
				beanFactory.getBean("${flowName}.transformer", Lifecycle.class).start();
				channelBindingService.bindConsumer(beanFactory.getBean(dsl.input.messageFrom, MessageChannel.class), dsl.input.messageFrom)
				channelBindingService.bindProducer(beanFactory.getBean(dsl.outputMessage.sentTo, MessageChannel.class), dsl.outputMessage.sentTo)
			}
		}
		return new FlowRegistrar()
	}

	@Service
	static class FlowRegistrar {}
}
