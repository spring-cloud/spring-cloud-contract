package io.codearte.accurest.messaging.noop;

import io.codearte.accurest.messaging.AccurestMessage;

import java.util.Map;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpAccurestMessage implements AccurestMessage {
	@Override
	public Object getPayload() {
		return null;
	}

	@Override
	public Map<String, Object> getHeaders() {
		return null;
	}

	@Override
	public Object getHeader(String key) {
		return null;
	}

	@Override
	public Object convert() {
		return null;
	}
}
