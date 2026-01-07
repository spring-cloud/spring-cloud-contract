/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.contract.verifier.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import groovy.json.JsonOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.RegexProperty;

/**
 * Utility class for JSON path matching operations including path cleanup, comparison
 * creation, and body matcher conversions.
 *
 * @author Marcin Grzejszczak
 * @since 5.1.0
 */
public final class JsonPathMatcherUtils {

	private static final Log log = LogFactory.getLog(JsonPathMatcherUtils.class);

	private static final Pattern ANY_ARRAY_NOTATION_IN_JSONPATH = Pattern.compile("\\[(.*?)\\]");

	private JsonPathMatcherUtils() {
	}

	/**
	 * Removes from the parsed json any JSON path matching entries. That way we remain
	 * with values that should be checked in the auto-generated fashion.
	 * @param json - parsed JSON
	 * @param bodyMatchers - the part of request / response that contains matchers
	 * @return json with removed entries
	 */
	public static Object removeMatchingJsonPaths(Object json, BodyMatchers bodyMatchers) {
		Object jsonCopy = cloneBody(json);
		if (bodyMatchers == null || !bodyMatchers.hasMatchers()) {
			return jsonCopy;
		}
		DocumentContext context = JsonPath.parse(jsonCopy);
		List<String> pathsToDelete = deleteMatchingPaths(context, bodyMatchers);
		cleanupEmptyContainers(context, pathsToDelete);
		return jsonCopy;
	}

	/**
	 * Retrieves a value from JSON via json path.
	 * @param json - parsed JSON
	 * @param jsonPath - json path
	 * @return matching part of the json
	 */
	public static Object readElement(Object json, String jsonPath) {
		return JsonPath.parse(json).read(jsonPath);
	}

	/**
	 * For the given matcher converts it into a JSON path that checks the regex pattern or
	 * equality.
	 * @param bodyMatcher the body matcher
	 * @return JSON path that checks the regex for its last element
	 */
	public static String convertJsonPathAndRegexToAJsonPath(BodyMatcher bodyMatcher) {
		return convertJsonPathAndRegexToAJsonPath(bodyMatcher, null);
	}

	/**
	 * For the given matcher converts it into a JSON path that checks the regex pattern or
	 * equality.
	 * @param bodyMatcher the body matcher
	 * @param body the body to read from (required for EQUALITY matching)
	 * @return JSON path that checks the regex for its last element
	 */
	public static String convertJsonPathAndRegexToAJsonPath(BodyMatcher bodyMatcher, Object body) {
		String path = bodyMatcher.path();
		Object value = bodyMatcher.value();
		if (value == null && bodyMatcher.matchingType() != MatchingType.EQUALITY
				&& bodyMatcher.matchingType() != MatchingType.TYPE) {
			return path;
		}
		int lastDotIndex = findLastDotIndex(path);
		String toLastDot = lastDotIndex == -1 ? "$" : path.substring(0, lastDotIndex);
		String fromLastDot = path.substring(lastDotIndex + 1);
		String propertyName = lastDotIndex == -1 ? "@" : "@." + fromLastDot;
		String comparison = createComparison(propertyName, bodyMatcher, value, body);
		return toLastDot + "[?(" + comparison + ")]";
	}

	/**
	 * Returns generated value if the value is a RegexProperty.
	 * @param value the value to check
	 * @return generated value or original value
	 */
	public static Object generatedValueIfNeeded(Object value) {
		if (value instanceof RegexProperty) {
			return ((RegexProperty) value).generateAndEscapeJavaStringIfNeeded();
		}
		return value;
	}

	// ========== Path Deletion and Cleanup ==========

	private static List<String> deleteMatchingPaths(DocumentContext context, BodyMatchers bodyMatchers) {
		List<String> pathsToDelete = new ArrayList<>();
		for (BodyMatcher matcher : bodyMatchers.matchers()) {
			String path = matcher.path();
			try {
				Object entry = readPath(context, path);
				if (entry != null) {
					context.delete(path);
					pathsToDelete.add(path);
				}
			}
			catch (RuntimeException e) {
				if (log.isTraceEnabled()) {
					log.trace("Exception deleting path [" + path + "]", e);
				}
			}
		}
		Collections.sort(pathsToDelete, Collections.reverseOrder());
		return pathsToDelete;
	}

	private static void cleanupEmptyContainers(DocumentContext context, List<String> paths) {
		for (String path : paths) {
			removeTrailingContainers(path, context);
		}
	}

	private static boolean removeTrailingContainers(String matcherPath, DocumentContext context) {
		try {
			Matcher matcher = ANY_ARRAY_NOTATION_IN_JSONPATH.matcher(matcherPath);
			boolean containsArray = matcher.find();
			String pathWithoutArray = containsArray
					? matcherPath.substring(0, matcherPath.lastIndexOf(lastMatch(matcher))) : matcherPath;
			Object object = readPath(context, pathWithoutArray);
			if (isIterable(object) && containsOnlyEmptyElements(object) && !isRootArray(matcherPath)) {
				String pathToDelete = pathWithoutArray.equals("$") ? "$[*]" : pathWithoutArray;
				if (pathToDelete.contains("..")) {
					Object root = context.read("$");
					if (rootContainsOnlyEmpty(root)) {
						context.delete("$[*]");
					}
					return false;
				}
				context.delete(pathToDelete);
				return removeTrailingContainers(pathToDelete, context);
			}
			int lastDot = matcherPath.lastIndexOf(".");
			if (lastDot == -1) {
				return false;
			}
			String parent = matcherPath.substring(0, lastDot);
			Object parentObject = context.read(parent);
			if (isIterable(parentObject) && containsOnlyEmptyElements(parentObject) && !parent.equals("$")) {
				context.delete(parent);
				return removeTrailingContainers(parent, context);
			}
			return false;
		}
		catch (RuntimeException e) {
			if (log.isTraceEnabled()) {
				log.trace("Exception removing trailing containers for [" + matcherPath + "]", e);
			}
			return false;
		}
	}

	private static String lastMatch(Matcher matcher) {
		List<String> matches = new ArrayList<>();
		matches.add(matcher.group());
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches.get(matches.size() - 1);
	}

	private static Object readPath(DocumentContext context, String path) {
		try {
			return context.read(path);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static boolean isIterable(Object object) {
		return object instanceof Iterable || object instanceof Map;
	}

	private static boolean isRootArray(String path) {
		return "$[*]".equals(path);
	}

	@SuppressWarnings("unchecked")
	private static boolean rootContainsOnlyEmpty(Object root) {
		if (!(root instanceof Iterable)) {
			return false;
		}
		for (Object item : (Iterable<?>) root) {
			if (!containsOnlyEmptyElements(item)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static boolean containsOnlyEmptyElements(Object object) {
		if (object instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) object;
			if (map.isEmpty()) {
				return true;
			}
			for (Object item : map.values()) {
				if (item instanceof Map && !((Map<?, ?>) item).isEmpty()) {
					return false;
				}
				if (item instanceof List && !((List<?>) item).isEmpty()) {
					return false;
				}
				if (!(item instanceof Map) && !(item instanceof List)) {
					return false;
				}
			}
			return true;
		}
		if (!(object instanceof Iterable)) {
			return false;
		}
		for (Object item : (Iterable<?>) object) {
			if (item instanceof Map && !((Map<?, ?>) item).isEmpty()) {
				return false;
			}
			if (item instanceof List && !((List<?>) item).isEmpty()) {
				return false;
			}
			if (!(item instanceof Map) && !(item instanceof List)) {
				return false;
			}
		}
		return true;
	}

	static Object cloneBody(Object object) {
		return CloneUtils.clone(object);
	}

	// ========== Comparison Creation ==========

	private static int findLastDotIndex(String path) {
		if (path.contains("['")) {
			int bracketIndex = path.lastIndexOf("['");
			return path.substring(0, bracketIndex).lastIndexOf(".");
		}
		return path.lastIndexOf(".");
	}

	private static String createComparison(String propertyName, BodyMatcher bodyMatcher, Object value, Object body) {
		return switch (bodyMatcher.matchingType()) {
			case EQUALITY -> createEqualityComparison(propertyName, bodyMatcher, body);
			case TYPE -> createTypeComparison(propertyName, bodyMatcher);
			default -> createRegexComparison(propertyName, value);
		};
	}

	private static String createEqualityComparison(String propertyName, BodyMatcher bodyMatcher, Object body) {
		if (body == null) {
			throw new IllegalStateException("Body hasn't been passed");
		}
		try {
			Object convertedBody = MapConverter.transformValues(body, JsonPathMatcherUtils::generatedValueIfNeeded);
			Object retrievedValue = JsonPath.parse(convertedBody).read(bodyMatcher.path());
			String wrappedValue = retrievedValue instanceof Number ? retrievedValue.toString()
					: "'" + retrievedValue.toString() + "'";
			return propertyName + " == " + wrappedValue;
		}
		catch (PathNotFoundException e) {
			throw new IllegalStateException(
					"Value [" + bodyMatcher.path() + "] not found in JSON [" + JsonOutput.toJson(body) + "]", e);
		}
	}

	private static String createTypeComparison(String propertyName, BodyMatcher bodyMatcher) {
		Integer min = bodyMatcher.minTypeOccurrence();
		Integer max = bodyMatcher.maxTypeOccurrence();
		StringBuilder result = new StringBuilder();
		if (min != null) {
			result.append(propertyName).append(".size() >= ").append(min);
		}
		if (max != null) {
			if (!result.isEmpty()) {
				result.append(" && ");
			}
			result.append(propertyName).append(".size() <= ").append(max);
		}
		return result.toString();
	}

	private static String createRegexComparison(String propertyName, Object value) {
		String convertedValue = value.toString();
		if (!convertedValue.contains("\\/")) {
			convertedValue = convertedValue.replace("/", "\\\\/");
		}
		return propertyName + " =~ /(" + convertedValue + ")/";
	}

}
