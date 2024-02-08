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

package org.springframework.cloud.contract.stubrunner.messaging.amqp;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * Rabbit listener.
 *
 * @author Mathias Düsterhöft
 */
public class MessageSubscriberRabbitListener {

	private Person person;

	// tag::amqp_annotated_listener[]
	@RabbitListener(bindings = @QueueBinding(value = @Queue("test.queue"),
			exchange = @Exchange(value = "contract-test.exchange", ignoreDeclarationExceptions = "true")))
	public void handlePerson(Person person) {
		this.person = person;
	}

	// end::amqp_annotated_listener[]
	public Person getPerson() {
		return this.person;
	}

}
