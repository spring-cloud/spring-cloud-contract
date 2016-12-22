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

package org.springframework.cloud.contract.stubrunner.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.contract.stubrunner.StubConfiguration;

/**
 * Utility to parse string into a list of configuration of stubs
 */
public class StubsParser {

	/**
	 * The string is expected to be a map with entry called "stubs"
	 * that contains a list of Strings in the format
	 *
	 * <ul>
	 * <li>groupid:artifactid:version:classifier:port</li>
	 * <li>groupid:artifactid:version:classifier</li>
	 * <li>groupid:artifactid:version</li>
	 * <li>groupid:artifactid</li>
	 * </ul>
	 *
	 * In the latter case the provided default stub classifier will be passed.
	 *
	 * Example:
	 *
	 * "a:b,c:d:e"
	 */
	public static Set<StubConfiguration> fromString(Collection<String> collection, String defaultClassifier) {
		Set<StubConfiguration> stubs = new LinkedHashSet<>();
		for (String config : collection) {
			if (StringUtils.hasText(config)) {
				stubs.add(StubSpecification.parse(config, defaultClassifier).stub);
			}
		}
		return stubs;
	}

	public static Map<StubConfiguration, Integer> fromStringWithPort(String notation) {
		StubSpecification stub = StubSpecification.parse(notation, StubConfiguration.DEFAULT_CLASSIFIER);
		if (!stub.hasPort()) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap(stub.stub, stub.port);
	}

	public static String ivyFromStringWithPort(String notation) {
		StubSpecification stub = StubSpecification.parse(notation, StubConfiguration.DEFAULT_CLASSIFIER);
		if (!stub.hasPort()) {
			return "";
		}
		return stub.stub.toColonSeparatedDependencyNotation();
	}

	public static boolean hasPort(String id) {
		String[] splitEntry = id.split(":");
		try {
			Integer.valueOf(splitEntry[splitEntry.length-1]);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private static class StubSpecification {

		private final StubConfiguration stub;
		private final Integer port;

		public StubSpecification(StubConfiguration stub, Integer port) {
			this.stub = stub;
			this.port = port;
		}

		public boolean hasPort() {
			return this.port != null;
		}
		
		private static StubSpecification parse(String id, String defaultClassifier) {
			String[] splitEntry = id.split(":");
			Integer port = null;
			try {
				port = Integer.valueOf(splitEntry[splitEntry.length-1]);
				id = id.substring(0, id.lastIndexOf(":"));
			} catch (NumberFormatException e) {}
			return new StubSpecification(new StubConfiguration(id, defaultClassifier), port);
		}

	}
}