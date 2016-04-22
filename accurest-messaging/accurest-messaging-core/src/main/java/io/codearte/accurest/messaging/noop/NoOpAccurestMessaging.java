package io.codearte.accurest.messaging.noop;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessaging;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpAccurestMessaging implements AccurestMessaging {
	@Override
	public void send(AccurestMessage message, String destination) {

	}

	@Override
	public void send(Object payload, Map headers, String destination) {

	}

	@Override
	public AccurestMessage receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		return null;
	}

	@Override
	public AccurestMessage receiveMessage(String destination) {
		return null;
	}

	@Override
	public AccurestMessage create(Object o, Map headers) {
		return null;
	}

	@Override
	public AccurestMessage create(Object o) {
		return null;
	}
}
