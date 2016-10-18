package org.springframework.cloud.contract.stubrunner.messaging.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.ExecutorService

import static org.mockito.BDDMockito.then
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@ContextConfiguration(classes = AmqpMessagingApplication, loader = SpringBootContextLoader)
@AutoConfigureStubRunner
class AmqpStubRunnerSpec extends Specification {

    @Autowired
    StubTrigger stubTrigger

    @SpyBean
    MessageSubscriber messageSubscriber

    @Captor
    ArgumentCaptor<Person> personArgumentCaptor
    
    def "should trigger stub amqp message"() {
        when:
        // tag::client_trigger[]
            stubTrigger.trigger("contract-test.person.created.event")
        // end::client_trigger[]
        then:
            then(messageSubscriber).should().handleMessage(personArgumentCaptor.capture())
            personArgumentCaptor.value.name != null
    }

    @Configuration
    static class TestConfig {
        /**
         * Do not connect to a real bus - so we mock the connection factory
         */
        @Bean
        public ConnectionFactory connectionFactory() {
            com.rabbitmq.client.ConnectionFactory mockConnectionFactory = mock(com.rabbitmq.client.ConnectionFactory.class)
            Connection mockConnection = mock(Connection.class)
            Channel mockChannel = mock(Channel.class)
            try {
                when(mockConnectionFactory.newConnection((ExecutorService) null)).thenReturn(mockConnection)
                when(mockConnection.isOpen()).thenReturn(true)
                when(mockConnection.createChannel()).thenReturn(mockChannel)
            } catch (Exception e) {
                throw new RuntimeException(e)
            }
            return new CachingConnectionFactory(mockConnectionFactory)
        }
    }
}
