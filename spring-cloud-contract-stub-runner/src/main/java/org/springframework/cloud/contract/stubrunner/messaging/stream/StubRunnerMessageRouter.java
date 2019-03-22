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
			String channelName = StubRunnerStreamConfiguration.resolvedDestination(
					this.beanFactory,
					dsl.getOutputMessage().getSentTo().getClientValue());
			return Collections
					.singleton((MessageChannel) this.beanFactory.getBean(channelName));
		}
		return Collections
				.singleton((MessageChannel) this.beanFactory.getBean("nullChannel"));
	}

}
