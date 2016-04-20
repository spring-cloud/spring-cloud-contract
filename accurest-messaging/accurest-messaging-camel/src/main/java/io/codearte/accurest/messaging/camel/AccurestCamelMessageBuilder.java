package io.codearte.accurest.messaging.camel;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;

import java.util.Map;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestCamelMessageBuilder<T> implements AccurestMessageBuilder<T, Message> {

	@Override
	public AccurestMessage<T, Message> create(T payload, Map<String, Object> headers) {
		DefaultMessage message = new DefaultMessage();
		message.setBody(payload);
		message.setHeaders(headers);
		return new CamelMessage<>(message);
	}

	@Override
	public AccurestMessage<T, Message> create(Message message) {
		return new CamelMessage<>(message);
	}
}
