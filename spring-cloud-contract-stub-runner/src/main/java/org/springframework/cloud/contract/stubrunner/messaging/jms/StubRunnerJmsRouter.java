/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.jms;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerJmsRouter implements MessageListener {

	private final StubRunnerJmsMessageSelector selector;

	private final BeanFactory beanFactory;

	private final List<Contract> contracts;

	private JmsTemplate jmsTemplate;

	StubRunnerJmsRouter(List<Contract> groovyDsls, BeanFactory beanFactory) {
		this.selector = new StubRunnerJmsMessageSelector(groovyDsls);
		this.beanFactory = beanFactory;
		this.contracts = groovyDsls;
	}

	@Override
	public void onMessage(javax.jms.Message message) {
		Contract dsl = this.selector.matchingContract(message);
		if (dsl != null && dsl.getOutputMessage() != null && dsl.getOutputMessage().getSentTo() != null) {
			String destination = dsl.getOutputMessage().getSentTo().getClientValue();
			jmsTemplate().send(destination,
					session -> new StubRunnerJmsTransformer(this.contracts).transform(session, dsl));
		}
	}

	private JmsTemplate jmsTemplate() {
		if (this.jmsTemplate == null) {
			this.jmsTemplate = this.beanFactory.getBean(JmsTemplate.class);
		}
		return this.jmsTemplate;
	}

}

class ReplyToProcessor implements MessagePostProcessor {

	@Override
	public javax.jms.Message postProcessMessage(Message message) throws JMSException {
		message.setStringProperty("requiresReply", "no");
		return message;
	}

}
