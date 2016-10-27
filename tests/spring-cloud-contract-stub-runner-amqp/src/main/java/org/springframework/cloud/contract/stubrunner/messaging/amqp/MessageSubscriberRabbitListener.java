package org.springframework.cloud.contract.stubrunner.messaging.amqp;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class MessageSubscriberRabbitListener {

	private Person person;

	// tag::amqp_annotated_listener[]
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "test.queue"),
			exchange = @Exchange(value = "contract-test.exchange", ignoreDeclarationExceptions = "true")))
	public void handlePerson(Person person) {
		this.person = person;
	}
	// end::amqp_annotated_listener[]
	public Person getPerson() {
		return this.person;
	}
}
