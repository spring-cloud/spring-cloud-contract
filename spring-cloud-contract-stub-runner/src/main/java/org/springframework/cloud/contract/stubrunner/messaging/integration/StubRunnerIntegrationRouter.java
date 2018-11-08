package org.springframework.cloud.contract.stubrunner.messaging.integration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerIntegrationRouter extends AbstractMessageRouter {

	private final StubRunnerIntegrationMessageSelector selector;
	private final BeanFactory beanFactory;

	StubRunnerIntegrationRouter(List<Contract> groovyDsls, BeanFactory beanFactory) {
		this.selector = new StubRunnerIntegrationMessageSelector(groovyDsls);
		this.beanFactory = beanFactory;
	}

	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		Contract dsl = this.selector.matchingContract(message);
		if (dsl != null && dsl.getOutputMessage() != null
				&& dsl.getOutputMessage().getSentTo() != null) {
			String channelName = dsl.getOutputMessage().getSentTo().getClientValue();
			return Collections
					.singleton((MessageChannel) this.beanFactory.getBean(channelName));
		}
		return Collections
				.singleton((MessageChannel)
						this.beanFactory.getBean(StubRunnerIntegrationConfiguration.DummyMessageHandler.CHANNEL_NAME));
	}
}