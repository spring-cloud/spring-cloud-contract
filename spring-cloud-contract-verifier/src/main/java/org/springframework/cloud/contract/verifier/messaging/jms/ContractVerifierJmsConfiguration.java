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

package org.springframework.cloud.contract.verifier.messaging.jms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Marcin Grzejszczak
 */
@Configuration
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnProperty(name = "stubrunner.jms.enabled", havingValue = "true",
		matchIfMissing = true)
@AutoConfigureBefore(NoOpContractVerifierAutoConfiguration.class)
public class ContractVerifierJmsConfiguration {

	@Bean
	@ConditionalOnMissingBean
	MessageVerifier<Message> contractVerifierJmsMessageExchange(
			ObjectProvider<JmsTemplate> jmsTemplateProvider) {
		JmsTemplate jmsTemplate = jmsTemplateProvider.getIfAvailable(JmsTemplate::new);
		return new JmsStubMessages(jmsTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	public ContractVerifierMessaging<Message> contractVerifierJmsMessaging(
			MessageVerifier<Message> exchange) {
		return new ContractVerifierJmsHelper(exchange);
	}

}

class ContractVerifierJmsHelper extends ContractVerifierMessaging<Message> {

	private static final Log log = LogFactory.getLog(ContractVerifierJmsHelper.class);

	ContractVerifierJmsHelper(MessageVerifier<Message> exchange) {
		super(exchange);
	}

	@Override
	protected ContractVerifierMessage convert(Message message) {
		try {
			Map<String, Object> headers = headers(message);
			return new ContractVerifierMessage(getPayload(message), headers);
		}
		catch (JMSException ex) {
			log.warn("An exception occurred while trying to convert the JMS message", ex);
			throw new IllegalStateException(ex);
		}
	}

	private Map<String, Object> headers(Message message) throws JMSException {
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

	private Object getPayload(Message message) throws JMSException {
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
