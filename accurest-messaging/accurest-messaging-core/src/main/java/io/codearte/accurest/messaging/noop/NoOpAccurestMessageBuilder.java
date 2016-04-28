package io.codearte.accurest.messaging.noop;

import java.util.Map;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpAccurestMessageBuilder implements AccurestMessageBuilder {
	@Override
	public AccurestMessage create(Object o, Map headers) {
		return new NoOpAccurestMessage();
	}

	@Override
	public AccurestMessage create(Object o) {
		return new NoOpAccurestMessage();
	}
}
