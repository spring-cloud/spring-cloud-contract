package io.codearte.accurest.messaging.camel;

import io.codearte.accurest.messaging.AccurestMessage;
import org.apache.camel.Message;

import java.util.Map;

/**
 * @author Marcin Grzejszczak
 */
public class CamelMessage<T> implements AccurestMessage<T, Message> {

	private final Message delegate;

	public CamelMessage(Message delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getPayload() {
		return (T) delegate.getBody();
	}

	@Override
	public Map<String, Object> getHeaders() {
		return delegate.getHeaders();
	}

	@Override
	public Object getHeader(String key) {
		return getHeaders().get(key);
	}

	@Override
	public Message convert() {
		return delegate;
	}

}
