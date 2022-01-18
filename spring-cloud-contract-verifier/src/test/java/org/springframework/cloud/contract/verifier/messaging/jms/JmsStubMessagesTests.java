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

package org.springframework.cloud.contract.verifier.messaging.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JmsStubMessagesTests {

	@Test
	void should_send_message_without_headers_and_contract() throws JMSException {
		final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
		final Session session = mock(Session.class);
		final TextMessage message = mock(TextMessage.class);

		when(session.createTextMessage(anyString())).thenReturn(message);

		final JmsStubMessages springJmsStubMessages = new JmsStubMessages(jmsTemplate);

		springJmsStubMessages.send(anyString(), null, anyString(), null);

		final ArgumentCaptor<MessageCreator> messageCreatorArgumentCaptor = ArgumentCaptor
				.forClass(MessageCreator.class);

		verify(jmsTemplate, times(1)).send(anyString(), messageCreatorArgumentCaptor.capture());

		final MessageCreator creator = messageCreatorArgumentCaptor.getValue();

		Assertions.assertThatCode(() -> creator.createMessage(session)).doesNotThrowAnyException();
	}

}
