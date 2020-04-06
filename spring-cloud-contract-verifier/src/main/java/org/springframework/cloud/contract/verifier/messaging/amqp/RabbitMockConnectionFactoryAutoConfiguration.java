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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Spring rabbit test utility that provides a mock ConnectionFactory to avoid having to
 * connect against a running broker.
 *
 * Set verifier.amqp.mockConnection=true to enable the mocked ConnectionFactory
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ContractVerifierAmqpAutoConfiguration.class)
@ConditionalOnProperty(value = "stubrunner.amqp.mockConnection", havingValue = "true",
		matchIfMissing = true)
public class RabbitMockConnectionFactoryAutoConfiguration {

	@Bean
	public ConnectionFactory connectionFactory() {
		final Connection mockConnection = mock(Connection.class);
		final AMQP.Queue.DeclareOk mockDeclareOk = mock(AMQP.Queue.DeclareOk.class);
		com.rabbitmq.client.ConnectionFactory mockConnectionFactory = mock(
				com.rabbitmq.client.ConnectionFactory.class, new Answer() {
					@Override
					public Object answer(InvocationOnMock invocationOnMock)
							throws Throwable {
						// hack for keeping backward compatibility with #303
						if ("newConnection"
								.equals(invocationOnMock.getMethod().getName())) {
							return mockConnection;
						}
						return Mockito.RETURNS_DEFAULTS.answer(invocationOnMock);
					}
				});
		try {
			final Channel mockChannel = mock(Channel.class, invocationOnMock -> {
				if ("queueDeclare".equals(invocationOnMock.getMethod().getName())) {
					return mockDeclareOk;
				}
				return Mockito.RETURNS_DEFAULTS.answer(invocationOnMock);
			});
			when(mockConnection.isOpen()).thenReturn(true);
			when(mockConnection.createChannel()).thenReturn(mockChannel);
			when(mockConnection.createChannel(Mockito.anyInt())).thenReturn(mockChannel);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new CachingConnectionFactory(mockConnectionFactory) {

		};
	}

}
