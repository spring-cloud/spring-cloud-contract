package io.codearte.accurest.stubrunner

interface StubTrigger {

	/**
	 * Triggers an event by a given label for a given {@code groupid:artifactid} notation. You can use only {@code artifactId} too.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger(String ivyNotation, String labelName)

	/**
	 * Triggers an event by a given label.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger(String labelName)

	/**
	 * Triggers all possible events.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger()

	/**
	 * Returns a mapping of ivy notation of a dependency to all the labels it has.
	 *
	 * Feature related to messaging.
	 */
	Map<String, Collection<String>> labels()
}