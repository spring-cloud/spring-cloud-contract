package org.springframework.cloud.contract.stubrunner.messaging.amqp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON;

import java.util.concurrent.ExecutorService;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@SpringBootApplication
public class AmqpMessagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmqpMessagingApplication.class, args);
	}

	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper) {
		final Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
		jsonMessageConverter.setJsonObjectMapper(objectMapper);
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

	@Bean
	public Exchange testExchange() {
		return new TopicExchange("test-exchange");
	}

	@Bean
	public MessageListenerAdapter messageListenerAdapter(MessageSubscriber messageSubscriber, MessageConverter messageConverter) {
		return new MessageListenerAdapter(messageSubscriber, messageConverter);
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		com.rabbitmq.client.ConnectionFactory mockConnectionFactory = mock(com.rabbitmq.client.ConnectionFactory.class);
		Connection mockConnection = mock(Connection.class);
		Channel mockChannel = mock(Channel.class);
		try {
			when(mockConnectionFactory.newConnection((ExecutorService) null)).thenReturn(mockConnection);
			when(mockConnection.isOpen()).thenReturn(true);
			when(mockConnection.createChannel()).thenReturn(mockChannel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new CachingConnectionFactory(mockConnectionFactory);
	}
}
