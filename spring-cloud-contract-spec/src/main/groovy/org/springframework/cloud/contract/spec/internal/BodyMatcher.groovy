package org.springframework.cloud.contract.spec.internal

/**
 * A jsonPathMatchers for the given path.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
interface BodyMatcher {

	/**
	 * What kind of matching are we dealing with
	 */
	MatchingType matchingType()

	/**
	 * Path to the path. Example for JSON it will be JSON Path
	 */
	String path()

	/**
	 * Optional value that the given path should be checked against.
	 * If there is no value then presence will be checked only together with
	 * type check. Example if we expect a JSON Path path {@code $.a} to be matched
	 * by type, the defined response body contained an integer but the actual one
	 * contained a string then the assertion should fail
	 */
	Object value()

	/**
	 * Min no of occurrence when matching by type. In all other cases it will be ignored
	 */
	Integer minTypeOccurrence()

	/**
	 * Max no of occurrence when matching by type. In all other cases it will be ignored
	 */
	Integer maxTypeOccurrence()
}