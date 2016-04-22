package io.codearte.accurest.samples.spring.config

import io.codearte.accurest.messaging.AccurestMessage
import io.codearte.accurest.messaging.AccurestMessageBuilder
import io.codearte.accurest.messaging.AccurestMessaging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

import javax.jms.Message
import java.util.concurrent.TimeUnit
/**
 * @author Marcin Grzejszczak
 */
@Component
public class AccurestSpringMessaging<T> implements AccurestMessaging<T, Message> {

	private static final Logger log = LoggerFactory.getLogger(AccurestSpringMessaging.class);

	private final JmsTemplate jmsTemplate;
	private final AccurestMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public AccurestSpringMessaging(AccurestMessageBuilder builder, JmsTemplate jmsTemplate) {
		this.builder = builder
		this.jmsTemplate = jmsTemplate
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	public void send(AccurestMessage<T, Message> message, String destination) {
		try {
			jmsTemplate.send(destination, ((SpringMessage) message).messageCreator)
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			return builder.create(jmsTemplate.receive(destination));
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public AccurestMessage<T, Message> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message> create(Message message) {
		return builder.create(message);
	}
}
