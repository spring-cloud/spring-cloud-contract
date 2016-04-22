package io.codearte.accurest.messaging.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.stereotype.Component;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;

/**
 * @author Marcin Grzejszczak
 */
@Component
public class AccurestIntegrationMessaging<T> implements AccurestMessaging<T, Message<T>> {

	private static final Logger log = LoggerFactory.getLogger(AccurestIntegrationMessaging.class);

	private final ApplicationContext context;
	private final AccurestMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public AccurestIntegrationMessaging(ApplicationContext context, AccurestMessageBuilder accurestMessageBuilder) {
		this.context = context;
		this.builder = accurestMessageBuilder;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	public void send(AccurestMessage<T, Message<T>> message, String destination) {
		try {
			MessageChannel messageChannel = context.getBean(destination, MessageChannel.class);
			messageChannel.send(message.convert());
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message<T>> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			PollableChannel messageChannel = context.getBean(destination, PollableChannel.class);
			return builder.create(messageChannel.receive(timeUnit.toMillis(timeout)));
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public AccurestMessage<T, Message<T>> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message<T>> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AccurestMessage<T, Message<T>> create(Message<T> message) {
		return builder.create(message);
	}
}
