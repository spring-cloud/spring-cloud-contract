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

package org.springframework.cloud.contract.stubrunner.messaging.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Marcin Grzejszczak
 */
class StubRunnerKafkaRouter implements MessageListener<Object, Object> {

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
		Message<?> message = MessageBuilder.createMessage(data.value(),
				headers(data.headers()));
		Contract dsl = this.selector.matchingContract(message);
		if (dsl != null && dsl.getOutputMessage() != null
				&& dsl.getOutputMessage().getSentTo() != null) {
			String destination = dsl.getOutputMessage().getSentTo().getClientValue();
			kafkaTemplate().send(destination,
					new StubRunnerKafkaTransformer(this.contracts).transform(dsl));
		}
	}

	private MessageHeaders headers(Headers headers) {
		Map<String, Object> map = new HashMap<>();
		for (Header header : headers) {
			map.put(header.key(), header.value());
		}
		return new MessageHeaders(map);
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data,
			Acknowledgment acknowledgment) {
		onMessage(data);
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data, Consumer<?, ?> consumer) {
		onMessage(data);
	}

	@Override
	public void onMessage(ConsumerRecord<Object, Object> data,
			Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
		onMessage(data);
	}

}
