package io.codearte.accurest.messaging;

import java.util.Map;

/**
 * Describes a message. Contains payload and headers. A message can be converted
 * to another type (e.g. Spring Messaging Message)
 *
 * @author Marcin Grzejszczak
 */
public interface AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> {

	/**
	 * Returns a payload of type {@code PAYLOAD}
	 */
	PAYLOAD getPayload();

	/**
	 * Returns a map of headers
	 */
	Map<String, Object> getHeaders();

	/**
	 * Returns a header for a given key
	 */
	Object getHeader(String key);

	/**
	 * Converts the message to {@code TYPE_TO_CONVERT_INTO} type
	 */
	TYPE_TO_CONVERT_INTO convert();
}
