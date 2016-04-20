package io.codearte.accurest.messaging.stream;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;
import io.codearte.accurest.messaging.AccurestMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestStreamMessaging<T> implements AccurestMessaging<T, Message<T>> {

	private static final Logger log = LoggerFactory.getLogger(AccurestStreamMessaging.class);

	private final ApplicationContext context;
	private final MessageCollector messageCollector;
	private final AccurestMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public AccurestStreamMessaging(ApplicationContext context, AccurestMessageBuilder builder) {
		this.context = context;
		this.messageCollector = context.getBean(MessageCollector.class);
		this.builder = builder;
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
			MessageChannel messageChannel = context.getBean(destination, MessageChannel.class);
			return builder.create(messageCollector.forChannel(messageChannel).poll(timeout, timeUnit));
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
