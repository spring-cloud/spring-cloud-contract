package com.example;

import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	MessagePublisher messagePublisher(RabbitTemplate rabbitTemplate) {
		return new MessagePublisher(rabbitTemplate, testExchange());
	}

}
