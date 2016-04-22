package io.codearte.accurest.messaging;

/**
 * Contract for filtering out messages that do not match the structure in the Accurest DSL
 *
 * @author Marcin Grzejszczak
 */
public interface AccurestFilter<PAYLOAD, TYPE_TO_CONVERT_INTO> {

	/**
	 * @return @{code true} if the message should be passed through, @{code false} if the message should be filtered out,
	 */
	boolean matches(AccurestMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> message);
}
