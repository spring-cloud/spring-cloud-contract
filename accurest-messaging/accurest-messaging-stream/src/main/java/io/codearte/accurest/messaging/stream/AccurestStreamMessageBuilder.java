package io.codearte.accurest.messaging.stream;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestStreamMessageBuilder<T> implements AccurestMessageBuilder<T, Message<T>> {

	@Override
	public AccurestMessage<T, Message<T>> create(T payload, Map<String, Object> headers) {
		return new StreamMessage<>(MessageBuilder.createMessage(payload, new MessageHeaders(headers)));
	}

	@Override
	public AccurestMessage<T, Message<T>> create(Message<T> message) {
		if (message == null) {
			return null;
		}
		return new StreamMessage<>(message);
	}
}
