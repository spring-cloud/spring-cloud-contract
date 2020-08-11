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

package org.springframework.cloud.contract.verifier.messaging.jms;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

class JmsStubMessages implements MessageVerifier<Message> {

	private final JmsTemplate jmsTemplate;

	JmsStubMessages(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public void send(Message message, String destination, YamlContract contract) {
		jmsTemplate.convertAndSend(destination, message, new ReplyToProcessor());
	}

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit,
			YamlContract contract) {
		jmsTemplate.setReceiveTimeout(timeUnit.toMillis(timeout));
		return jmsTemplate.receive(destination);
	}

	@Override
	public Message receive(String destination, YamlContract contract) {
		return receive(destination, 5, TimeUnit.SECONDS, contract);
	}

	@Override
	public void send(Object payload, Map headers, String destination,
			YamlContract contract) {
		jmsTemplate.send(destination, session -> {
			Message message = createMessage(session, payload);
			setHeaders(message, headers);
			return message;
		});
	}

	private Message createMessage(Session session, Object payload) throws JMSException {
		if (payload instanceof String) {
			return session.createTextMessage((String) payload);
		}
		else if (payload instanceof byte[]) {
			BytesMessage bytesMessage = session.createBytesMessage();
			bytesMessage.writeBytes((byte[]) payload);
			return bytesMessage;
		}
		else if (payload instanceof Serializable) {
			return session.createObjectMessage((Serializable) payload);
		}
		return session.createMessage();
	}

	private void setHeaders(Message message, Map<String, Object> headers) {
		for (Map.Entry<String, Object> entry : headers.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			try {
				if (value instanceof String) {
					message.setStringProperty(key, (String) value);
				}
				else if (value instanceof Boolean) {
					message.setBooleanProperty(key, (Boolean) value);
				}
				else {
					message.setObjectProperty(key, value);
				}
			}
			catch (JMSException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

}

class ReplyToProcessor implements MessagePostProcessor {

	@Override
	public Message postProcessMessage(Message message) throws JMSException {
		message.setStringProperty("requiresReply", "no");
		return message;
	}

}
