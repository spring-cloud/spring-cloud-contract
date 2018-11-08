package org.springframework.cloud.contract.stubrunner.messaging.stream;

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
class StubRunnerMessageRouter extends AbstractMessageRouter {

	private final StubRunnerStreamMessageSelector selector;
	private final BeanFactory beanFactory;

	StubRunnerMessageRouter(List<Contract> groovyDsls, BeanFactory beanFactory) {
		this.selector = new StubRunnerStreamMessageSelector(groovyDsls);
		this.beanFactory = beanFactory;
	}

	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		Contract dsl = this.selector.matchingContract(message);
		if (dsl != null && dsl.getOutputMessage() != null
				&& dsl.getOutputMessage().getSentTo() != null) {
			String channelName = StubRunnerStreamConfiguration.resolvedDestination(this.beanFactory,
					dsl.getOutputMessage().getSentTo().getClientValue());
			return Collections
					.singleton((MessageChannel) this.beanFactory.getBean(channelName));
		}
		return Collections
				.singleton((MessageChannel)
						this.beanFactory.getBean(StubRunnerStreamConfiguration.DummyMessageHandler.CHANNEL_NAME));
	}
}