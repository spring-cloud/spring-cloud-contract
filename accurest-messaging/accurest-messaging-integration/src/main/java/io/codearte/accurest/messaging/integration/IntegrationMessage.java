package io.codearte.accurest.messaging.integration;

import io.codearte.accurest.messaging.AccurestMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * @author Marcin Grzejszczak
 */
public class IntegrationMessage<T> implements AccurestMessage<T, Message<T>> {

	private final Message<T> delegate;

	public IntegrationMessage(Message<T> delegate) {
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
