package io.codearte.accurest.messaging.noop;

import io.codearte.accurest.messaging.AccurestMessage;
import io.codearte.accurest.messaging.AccurestMessageBuilder;

import java.util.Map;

/**
 * @author Marcin Grzejszczak
 */
public class NoOpAccurestMessageBuilder implements AccurestMessageBuilder {
	@Override
	public AccurestMessage create(Object o, Map headers) {
		return null;
	}

	@Override
	public AccurestMessage create(Object o) {
		return null;
	}
}
