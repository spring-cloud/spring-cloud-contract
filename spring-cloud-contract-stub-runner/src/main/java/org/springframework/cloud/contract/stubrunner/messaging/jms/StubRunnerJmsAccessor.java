/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.jms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

final class StubRunnerJmsAccessor {

	private StubRunnerJmsAccessor() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

	static Object getBody(Message message) {
		try {
			return getPayload(message);
		}
		catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static Map<String, Object> getHeaders(Message message) {
		try {
			return headers(message);
		}
		catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Map<String, Object> headers(Message message) throws JMSException {
		Map<String, Object> headers = new HashMap<>();
		if (message == null) {
			return headers;
		}
		Enumeration enumeration = message.getPropertyNames();
		while (enumeration.hasMoreElements()) {
			Object element = enumeration.nextElement();
			String asString = element.toString();
			Object property = message.getObjectProperty(asString);
			headers.put(asString, property);
		}
		return headers;
	}

	private static Object getPayload(Message message) throws JMSException {
		if (message == null) {
			return null;
		}
		else if (message instanceof TextMessage) {
			return ((TextMessage) message).getText();
		}
		else if (message instanceof StreamMessage) {
			return ((StreamMessage) message).readObject();
		}
		else if (message instanceof ObjectMessage) {
			return ((ObjectMessage) message).getObject();
		}
		return message.getBody(Object.class);
	}

}
