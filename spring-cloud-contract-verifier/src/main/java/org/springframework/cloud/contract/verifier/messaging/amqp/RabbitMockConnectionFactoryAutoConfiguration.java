package org.springframework.cloud.contract.verifier.messaging.amqp;

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
 * Spring rabbit test utility that provides a mock ConnectionFactory to avoid having to connect against a running broker.
 *
 * Set verifier.amqp.mockConnection=true to enable the mocked ConnectionFactory
 *
 * @author Mathias Düsterhöft
 * @since 1.0.2
 */
@Configuration
@ConditionalOnBean(ContractVerifierAmqpAutoConfiguration.class)
@ConditionalOnProperty(value = "stubrunner.amqp.mockConnection", havingValue = "true", matchIfMissing = true)
public class RabbitMockConnectionFactoryAutoConfiguration {

	@Bean
	public ConnectionFactory connectionFactory() {
		final Connection mockConnection = mock(Connection.class);
		final Channel mockChannel = mock(Channel.class);
		com.rabbitmq.client.ConnectionFactory mockConnectionFactory = mock(com.rabbitmq.client.ConnectionFactory.class, new Answer() {
			@Override public Object answer(InvocationOnMock invocationOnMock)
					throws Throwable {
				// hack for keeping backward compatibility with #303
				if ("newConnection".equals(invocationOnMock.getMethod().getName())) {
					return mockConnection;
				}
				return Mockito.RETURNS_DEFAULTS.answer(invocationOnMock);
			}
		});
		try {
			when(mockConnection.isOpen()).thenReturn(true);
			when(mockConnection.createChannel()).thenReturn(mockChannel);
			when(mockConnection.createChannel(Mockito.anyInt())).thenReturn(mockChannel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new CachingConnectionFactory(mockConnectionFactory);
	}
}
