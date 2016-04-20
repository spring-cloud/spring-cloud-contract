package io.codearte.accurest.messaging;

import java.util.Map;

/**
 * Contract for creation of (@link AccurestMessage}. You can create a message from
 * payload and headers or from some type (e.g. Spring Messaging Message).
 *
 * @author Marcin Grzejszczak
 */
public interface AccurestMessageBuilder<PAYLOAD, TYPE_TO_CONVERT_INTO> {

	/**
	 * Creates a {@link AccurestMessage} from payload and headers
	 */
	AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> create(PAYLOAD payload, Map<String, Object> headers);

	/**
	 * Creates a {@link AccurestMessage} from the {@code TYPE_TO_CONVERT_INTO} type
	 */
	AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> create(TYPE_TO_CONVERT_INTO typeToConvertInto);
}
