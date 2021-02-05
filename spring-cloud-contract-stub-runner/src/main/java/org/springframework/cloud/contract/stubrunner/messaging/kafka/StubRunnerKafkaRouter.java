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

package org.springframework.cloud.contract.stubrunner.messaging.kafka;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.messaging.Message;

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerKafkaRouter implements MessageListener<Object, Object> {

	private static final Log log = LogFactory.getLog(StubRunnerKafkaRouter.class);

	private final MessagingMessageConverter messageConverter = new MessagingMessageConverter();

	private final StubRunnerKafkaMessageSelector selector;

	private final BeanFactory beanFactory;

	private final List<Contract> contracts;

	private KafkaTemplate kafkaTemplate;

	StubRunnerKafkaRouter(List<Contract> groovyDsls, BeanFactory beanFactory) {
		this.selector = new StubRunnerKafkaMessageSelector(groovyDsls);
		this.beanFactory = beanFactory;
		this.contracts = groovyDsls;
	}

	private KafkaTemplate kafkaTemplate() {
		if (this.kafkaTemplate == null) {
			this.kafkaTemplate = this.beanFactory.getBean(KafkaTemplate.class);
		}
		return this.kafkaTemplate;
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data) {
		if (log.isDebugEnabled()) {
			log.debug("Received message [" + data + "]");
		}
		Message<?> message = messageConverter.toMessage(data, null, null, null);
		Contract dsl = this.selector.matchingContract(message);
		if (dsl != null && dsl.getOutputMessage() != null && dsl.getOutputMessage().getSentTo() != null) {
			String destination = dsl.getOutputMessage().getSentTo().getClientValue();
			if (log.isDebugEnabled()) {
				log.debug("Found a matching contract with an output message. Will send it to the [" + destination
						+ "] destination");
			}
			Message<?> transform = new StubRunnerKafkaTransformer(this.contracts).transform(dsl);
			String defaultTopic = kafkaTemplate().getDefaultTopic();
			try {
				kafkaTemplate().setDefaultTopic(destination);
				kafkaTemplate().send(transform);
			}
			finally {
				kafkaTemplate().setDefaultTopic(defaultTopic);
			}
		}
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment) {
		onMessage(data);
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data, Consumer<?, ?> consumer) {
		onMessage(data);
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
		onMessage(data);
	}

}
