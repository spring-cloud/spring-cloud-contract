package com.example;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class MessagePublisher {

	private final RabbitTemplate rabbitTemplate;

	private final Exchange exchange;

	public MessagePublisher(RabbitTemplate rabbitTemplate, Exchange exchange) {
		this.rabbitTemplate = rabbitTemplate;
		this.exchange = exchange;
	}

	public void sendMessage(Book book) {
		this.rabbitTemplate.convertAndSend(this.exchange.getName(), "routingkey", book);
	}
}
