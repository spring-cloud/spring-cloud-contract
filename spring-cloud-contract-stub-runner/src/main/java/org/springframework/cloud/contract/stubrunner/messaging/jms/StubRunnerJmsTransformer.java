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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.util.BodyExtractor;

/**
 * Sends forward a message defined in the DSL.
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerJmsTransformer {

	private final StubRunnerJmsMessageSelector selector;

	StubRunnerJmsTransformer(List<Contract> groovyDsls) {
		this.selector = new StubRunnerJmsMessageSelector(groovyDsls);
	}

	public Message transform(Session session, Contract groovyDsl) {
		Object outputBody = outputBody(groovyDsl);
		Map<String, Object> headers = groovyDsl.getOutputMessage().getHeaders()
				.asStubSideMap();
		Message newMessage = createMessage(session, outputBody);
		setHeaders(newMessage, headers);
		this.selector.updateCache(newMessage, groovyDsl);
		return newMessage;
	}

	private Object outputBody(Contract groovyDsl) {
		Object outputBody = BodyExtractor
				.extractClientValueFromBody(groovyDsl.getOutputMessage().getBody());
		if (outputBody instanceof FromFileProperty) {
			FromFileProperty property = (FromFileProperty) outputBody;
			return property.asBytes();
		}
		return BodyExtractor.extractStubValueFrom(outputBody);
	}

	Contract matchingContract(Message source) {
		return this.selector.matchingContract(source);
	}

	private Message createMessage(Session session, Object payload) {
		try {
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
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
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
