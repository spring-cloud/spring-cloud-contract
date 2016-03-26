package io.codearte.accurest.stubrunner.util

import groovy.transform.CompileStatic
import io.codearte.accurest.stubrunner.StubConfiguration

/**
 * Utility to parse string into a list of configuration of stubs
 */
@CompileStatic
class StubsParser {

	/**
	 * The string is expected to be a map with entry called "stubs"
	 * that contains a list of Strings in the format
	 *
	 * <ul>
	 * <li>groupid:artifactid:classifier</li>
	 * <li>groupid:artifactid</li>
	 * </ul>
	 *
	 * In the latter case the provided default stub classifier will be passed.
	 *
	 * Example:
	 *
	 * "a:b,c:d:e"
	 */
	static Set<StubConfiguration> fromString(String list, String defaultClassifier) {
		return list.split(',').findAll { it }.collect { String string ->
			new StubConfiguration(string, defaultClassifier)
		} as Set
	}

	static Set<StubConfiguration> fromString(Collection<String> collection, String defaultClassifier) {
		return collection.findAll { it }.collect { String string ->
			new StubConfiguration(string, defaultClassifier)
		} as Set
	}
}