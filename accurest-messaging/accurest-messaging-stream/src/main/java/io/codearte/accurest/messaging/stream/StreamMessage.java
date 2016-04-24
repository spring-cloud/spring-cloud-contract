package io.codearte.accurest.messaging.stream;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import io.codearte.accurest.messaging.AccurestMessage;

/**
 * @author Marcin Grzejszczak
 */
public class StreamMessage<T> implements AccurestMessage<T, Message<T>> {

	private final Message<T> delegate;

	public StreamMessage(Message<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T getPayload() {
		return delegate.getPayload();
	}

	@Override
	public MessageHeaders getHeaders() {
		return delegate.getHeaders();
	}

	@Override
	public Object getHeader(String key) {
		return getHeaders().get(key);
	}

	@Override
	public Message<T> convert() {
		return delegate;
	}

}
