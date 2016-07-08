/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.util

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.stubrunner.StubConfiguration

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