package org.springframework.cloud.contract.stubrunner.messaging.amqp;

import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class AmqpMessagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmqpMessagingApplication.class, args);
	}

	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper) {
		final Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter(objectMapper);
		jsonMessageConverter.setCreateMessageIds(true);
		final ContentTypeDelegatingMessageConverter messageConverter = new ContentTypeDelegatingMessageConverter(jsonMessageConverter);
		messageConverter.addDelegate(CONTENT_TYPE_JSON, jsonMessageConverter);
		return messageConverter;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

	@Configuration
	@Profile("!listener")
	static class MessageListenerAdapterConfig {

		@Bean
		public MessageListenerAdapter messageListenerAdapter(MessageSubscriber messageSubscriber, MessageConverter messageConverter) {
			return new MessageListenerAdapter(messageSubscriber, messageConverter);
		}

		// tag::amqp_binding[]

		@Bean
		public Binding binding() {
			return BindingBuilder.bind(new Queue("test.queue")).to(new DirectExchange("contract-test.exchange")).with("#");
		}
		// end::amqp_binding[]

		// tag::amqp_listener[]
		@Bean
		public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
																				MessageListenerAdapter listenerAdapter) {
			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setQueueNames("test.queue");
			container.setMessageListener(listenerAdapter);

			return container;
		}
		// end::amqp_listener[]

		@Bean
		public MessageSubscriber messageSubscriber() {
			return new MessageSubscriber();
		}
	}

	@Configuration
	@EnableRabbit
	@Profile("listener")
	static class RabbitListenerConfig {

		@Bean
		public MessageSubscriberRabbitListener messageSubscriberRabbitLister() {
			return new MessageSubscriberRabbitListener();
		}

		@Bean
		public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
																					MessageConverter messageConverter) {
			SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
			factory.setConnectionFactory(connectionFactory);
			factory.setConcurrentConsumers(3);
			factory.setMaxConcurrentConsumers(10);
			factory.setMessageConverter(messageConverter);
			return factory;
		}
	}
}
