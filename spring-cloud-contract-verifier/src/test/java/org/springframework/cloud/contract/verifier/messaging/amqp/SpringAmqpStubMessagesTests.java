/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringAmqpStubMessagesTests {

	@Test
	void should_send_message_without_headers_and_contract() {
		final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
		final MessageListenerAccessor messageListenerAccessor = mock(MessageListenerAccessor.class);
		final RabbitProperties rabbitProperties = mock(RabbitProperties.class);
		final SimpleMessageListenerContainer messageListenerContainer = mock(SimpleMessageListenerContainer.class);
		final MessageListener messageListener = mock(MessageListener.class);

		when(messageListenerContainer.getMessageListener()).thenReturn(messageListener);
		when(messageListenerAccessor.getListenerContainersForDestination(any(), any()))
				.thenReturn(Collections.singletonList(messageListenerContainer));

		final SpringAmqpStubMessages springAmqpStubMessages = new SpringAmqpStubMessages(rabbitTemplate,
				messageListenerAccessor, rabbitProperties);

		Assertions.assertThatCode(() -> springAmqpStubMessages.send(anyString(), null, anyString(), null))
				.doesNotThrowAnyException();
	}

}
