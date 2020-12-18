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

package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
class Issue178ListenerConfiguration {

	@Bean
	SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
			RabbitTemplate rabbitTemplate) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addQueueNames("rated-item-service.rated-item-event.exchange");
		container.setMessageListener(exampleListener(rabbitTemplate));
		return container;
	}

	@Bean
	MessageListener exampleListener(final RabbitTemplate rabbitTemplate) {
		return new MessageListener() {
			public void onMessage(Message message) {
				System.out.println("received: " + message);
				try {
					String payload = new ObjectMapper()
							.writeValueAsString(new MyPojo("992e46d8-ab05-4a26-a740-6ef7b0daeab3", "CREATED"));
					Message outputMessage = MessageBuilder.withBody(payload.getBytes()).build();
					rabbitTemplate.send(issue178OutputExchange().getName(), "routingkey", outputMessage);
				}
				catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Bean
	Queue issue178InputQueue() {
		return new Queue("rated-item-service.rated-item-event.exchange", false);
	}

	@Bean
	TopicExchange issue178InputExchange() {
		return new TopicExchange("rated-item-service.rated-item-event.exchange");
	}

	@Bean
	TopicExchange issue178OutputExchange() {
		return new TopicExchange("bill-service.rated-item-event.retry-exchange");
	}

	@Bean
	Binding binding() {
		return BindingBuilder.bind(issue178InputQueue()).to(issue178InputExchange())
				.with("rated-item-service.rated-item-event.exchange");
	}

	static class MyPojo {

		public String ratedItemId;

		public String eventType;

		MyPojo(String ratedItemId, String eventType) {
			this.ratedItemId = ratedItemId;
			this.eventType = eventType;
		}

		MyPojo() {
		}

	}

}
