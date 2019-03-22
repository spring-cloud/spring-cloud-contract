/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.util.StringUtils;

/**
 * Utility to parse string into a list of configuration of stubs.
 *
 * @author Marcin Grzejszczak
 */
public final class StubsParser {

	private StubsParser() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	/**
	 * The string is expected to be a map with entry called "stubs" that contains a list
	 * of Strings in the format
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
	 * @param collection collection of ids
	 * @param defaultClassifier default classifier to append if one is missing
	 * @return parsed stub configurations
	 */
	public static List<StubConfiguration> fromString(Collection<String> collection,
			String defaultClassifier) {
		List<StubConfiguration> stubs = new ArrayList<>();
		for (String config : collection) {
			if (StringUtils.hasText(config)) {
				stubs.add(StubSpecification.parse(config, defaultClassifier).stub);
			}
		}
		return stubs;
	}

	/**
	 * @param notation ivy notation of stubs with ports
	 * @return mapping of parsed stub configurations to ports on which the stub is running
	 */
	public static Map<StubConfiguration, Integer> fromStringWithPort(String notation) {
		StubSpecification stub = StubSpecification.parse(notation,
				StubConfiguration.DEFAULT_CLASSIFIER);
		if (!stub.hasPort()) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap(stub.stub, stub.port);
	}

	/**
	 * @param notation ivy notation of stubs with ports
	 * @return colon seprated dependency notation with port or empty string if no port is
	 * present
	 */
	public static String ivyFromStringWithPort(String notation) {
		StubSpecification stub = StubSpecification.parse(notation,
				StubConfiguration.DEFAULT_CLASSIFIER);
		if (!stub.hasPort()) {
			return "";
		}
		return stub.stub.toColonSeparatedDependencyNotation();
	}

	/**
	 * @param id string notation of a stub
	 * @return {@code true} if a port is there in the identifier
	 */
	public static boolean hasPort(String id) {
		String[] splitEntry = id.split(":");
		try {
			Integer.valueOf(splitEntry[splitEntry.length - 1]);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	private static class StubSpecification {

		private final StubConfiguration stub;

		private final Integer port;

		StubSpecification(StubConfiguration stub, Integer port) {
			this.stub = stub;
			this.port = port;
		}

		private static StubSpecification parse(String id, String defaultClassifier) {
			String[] splitEntry = id.split(":");
			Integer port = null;
			try {
				port = Integer.valueOf(splitEntry[splitEntry.length - 1]);
				id = id.substring(0, id.lastIndexOf(":"));
			}
			catch (NumberFormatException e) {
			}
			return new StubSpecification(new StubConfiguration(id, defaultClassifier),
					port);
		}

		public boolean hasPort() {
			return this.port != null;
		}

	}

}
