package io.codearte.accurest.messaging;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Core interface that allows you to build, send and receive messages.
 *
 * Destination is relevant to the underlaying implementation. Might be a channel, queue, topic etc.
 *
 * @author Marcin Grzejszczak
 */
public interface AccurestMessaging<PAYLOAD, TYPE_TO_CONVERT_INTO> extends AccurestMessageBuilder<PAYLOAD, TYPE_TO_CONVERT_INTO> {
	/**
	 * Sends the {@link AccurestMessage} to the given destination.
	 */
	void send(AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> message, String destination);

	/**
	 * Sends the given payload with headers, to the given destination.
	 */
	void send(PAYLOAD payload, Map<String, Object> headers, String destination);

	/**
	 * Receives the {@link AccurestMessage} from the given destination. You can provide the timeout
	 * for receiving that message.
	 */
	AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> receiveMessage(String destination, long timeout, TimeUnit timeUnit);

	/**
	 * Receives the {@link AccurestMessage} from the given destination. A default timeout will be applied.
	 */
	AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> receiveMessage(String destination);
}
