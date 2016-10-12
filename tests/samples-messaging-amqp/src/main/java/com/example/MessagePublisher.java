package com.example;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessagePublisher {

	private final RabbitTemplate rabbitTemplate;

	private final Exchange exchange;
	@Autowired
	public MessagePublisher(RabbitTemplate rabbitTemplate, Exchange exchange) {
		this.rabbitTemplate = rabbitTemplate;
		this.exchange = exchange;
	}

	public void sendMessage(Book book) {
		this.rabbitTemplate.convertAndSend(this.exchange.getName(), "routingkey", book);
	}
}
