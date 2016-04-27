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
		def splitList =  list.split(',').findAll { it }
		return fromString(splitList, defaultClassifier)
	}

	static Set<StubConfiguration> fromString(Collection<String> collection, String defaultClassifier) {
		return collection.findAll { it }.collect { String entry ->
			def splitEntry = entry.split(':')
			if (splitEntry.last().isInteger()) {
				String id = entry - ":${splitEntry.last()}"
				new StubConfiguration(id, defaultClassifier)
			}
			new StubConfiguration(entry, defaultClassifier)
		} as Set
	}

	static Map<StubConfiguration, Integer> fromStringWithPort(String notation) {
		def splitEntry = notation.split(':')
		if (!splitEntry.last().isInteger()) {
			return [:]
		}
		Integer port = splitEntry.last().toInteger()
		String id = notation - ":${splitEntry.last()}"
		return [(new StubConfiguration(id)): port]
	}

	static String ivyFromStringWithPort(String notation) {
		def splitEntry = notation.split(':')
		if (!splitEntry.last().isInteger()) {
			return ''
		}
		return notation - ":${splitEntry.last()}"
	}

	static boolean hasPort(String notation) {
		def splitEntry = notation.split(':')
		return splitEntry.last().isInteger()
	}
}