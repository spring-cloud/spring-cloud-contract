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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.StreamMessage;
import jakarta.jms.TextMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.integration.ContractVerifierIntegrationConfiguration;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Marcin Grzejszczak
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnProperty(name = "stubrunner.jms.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore({ ContractVerifierIntegrationConfiguration.class, NoOpContractVerifierAutoConfiguration.class })
public class ContractVerifierJmsConfiguration {

	@Bean
	@ConditionalOnMissingBean(MessageVerifierSender.class)
	MessageVerifierSender<Message> contractVerifierJmsMessageSender(ObjectProvider<JmsTemplate> jmsTemplateProvider) {
		JmsTemplate jmsTemplate = jmsTemplateProvider.getIfAvailable(JmsTemplate::new);
		JmsStubMessages jmsStubMessages = new JmsStubMessages(jmsTemplate);
		return new MessageVerifierSender<>() {
			@Override
			public void send(Message message, String destination, @Nullable YamlContract contract) {
				jmsStubMessages.send(message, destination, contract);
			}

			@Override
			public <T> void send(T payload, Map<String, Object> headers, String destination,
					@Nullable YamlContract contract) {
				jmsStubMessages.send(payload, headers, destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(MessageVerifierReceiver.class)
	MessageVerifierReceiver<Message> contractVerifierJmsMessageReceiver(
			ObjectProvider<JmsTemplate> jmsTemplateProvider) {
		JmsTemplate jmsTemplate = jmsTemplateProvider.getIfAvailable(JmsTemplate::new);
		JmsStubMessages jmsStubMessages = new JmsStubMessages(jmsTemplate);
		return new MessageVerifierReceiver<>() {
			@Override
			public Message receive(String destination, long timeout, TimeUnit timeUnit,
					@Nullable YamlContract contract) {
				return jmsStubMessages.receive(destination, timeout, timeUnit, contract);
			}

			@Override
			public Message receive(String destination, YamlContract contract) {
				return jmsStubMessages.receive(destination, contract);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(ContractVerifierMessaging.class)
	ContractVerifierMessaging<Message> contractVerifierJmsMessaging(MessageVerifierSender<Message> sender,
			MessageVerifierReceiver<Message> receiver) {
		return new ContractVerifierJmsHelper(sender, receiver);
	}

}

class ContractVerifierJmsHelper extends ContractVerifierMessaging<Message> {

	private static final Log log = LogFactory.getLog(ContractVerifierJmsHelper.class);

	ContractVerifierJmsHelper(MessageVerifierSender<Message> sender, MessageVerifierReceiver<Message> receiver) {
		super(sender, receiver);
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
