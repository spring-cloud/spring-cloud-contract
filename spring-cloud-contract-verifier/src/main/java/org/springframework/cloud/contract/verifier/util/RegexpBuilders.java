/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.GStringImpl;

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.util.RegexpUtils;

import static org.apache.commons.text.StringEscapeUtils.escapeJson;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;

/**
 * Useful utility methods to work with regular expressions.
 *
 * @since 1.0.0
 */
public final class RegexpBuilders {

	private final static String WS = "/\\s*/";

	private static final Function<DslProperty<?>, Object> CLIENT_VALUE_EXTRACTOR = DslProperty::getClientValue;

	private RegexpBuilders() {
	}

	/**
	 * Converts the {@link Object} passed values into their stub side String
	 * representations.
	 */
	public static String buildGStringRegexpForStubSide(Object o) {
		if (o instanceof DslProperty) {
			return buildGStringRegexpForStubSide((DslProperty<?>) o);
		}
		else if (o instanceof Pattern) {
			return buildGStringRegexpForStubSide((Pattern) o);
		}
		else if (o instanceof GString) {
			return buildGStringRegexpForStubSide((GString) o);
		}
		return escapeSpecialRegexChars(o.toString());
	}

	/**
	 * Converts the {@link GString} passed values into their stub side String
	 * representations.
	 */
	static String buildGStringRegexpForStubSide(GString gString) {
		return new GStringImpl(
				Stream.of(gString.getValues()).map(RegexpBuilders::buildGStringRegexpForStubSide).map(s -> (Object) s)
						.toArray(),
				Stream.of(gString.getStrings()).map(RegexpBuilders::escapeSpecialRegexChars).toArray(String[]::new))
						.toString();
	}

	/**
	 * Converts the {@link Pattern} passed values into their stub side String
	 * representations.
	 */
	static String buildGStringRegexpForStubSide(Pattern pattern) {
		return pattern.pattern();
	}

	/**
	 * Converts the {@link org.springframework.cloud.contract.spec.internal.DslProperty}
	 * passed values into their stub side String representations.
	 */
	static String buildGStringRegexpForStubSide(DslProperty<?> dslProperty) {
		return buildGStringRegexpForStubSide(dslProperty.getClientValue());
	}

	/**
	 * Converts the {@link GString} passed values into their test side String
	 * representations.
	 */
	public static String buildGStringRegexpForTestSide(GString gString) {
		return new GStringImpl(
				Stream.of(gString.getValues()).map(RegexpBuilders::buildGStringRegexpForTestSide).map(s -> (Object) s)
						.toArray(),
				Stream.of(gString.getStrings()).map(RegexpBuilders::escapeSpecialRegexChars).toArray(String[]::new))
						.toString();
	}

	/**
	 * Converts the {@link Object} passed values into their test side String
	 * representations.
	 */
	public static String buildGStringRegexpForTestSide(Object o) {
		return o.toString().replaceAll("\\\\", "\\\\\\\\");
	}

	public static String escapeSpecialRegexChars(String str) {
		return RegexpUtils.escapeSpecialRegexChars(str);
	}

	public static String buildJSONRegexpMatch(GString gString) {
		return buildJSONRegexpMatch(extractValue(gString, JSON, CLIENT_VALUE_EXTRACTOR));
	}

	public static String buildJSONRegexpMatch(Map<String, Object> jsonMap) {
		return WS + "\\{"
				+ jsonMap.entrySet().stream().map(RegexpBuilders::buildJSONRegexpMatch).collect(Collectors.joining(","))
				+ "\\}" + WS;
	}

	public static String buildJSONRegexpMatch(List<?> jsonList) {
		return WS + "\\[" + jsonList.stream().map(RegexpBuilders::buildJSONRegexpMatch).collect(Collectors.joining(","))
				+ "\\]" + WS;
	}

	/**
	 * Converts the map into String representation of regular expressions.
	 */
	public static String buildJSONRegexpMatch(Map.Entry<String, Object> entry) {
		return buildJSONRegexpMatchString(escapeJson(entry.getKey())) + ":" + buildJSONRegexpMatch(entry.getValue());
	}

	/**
	 * Converts the object into String representation of regular expressions.
	 */
	public static String buildJSONRegexpMatch(Object value) {
		return buildJSONRegexpMatchStringOptionalQuotes(escapeJson(value.toString()));
	}

	/**
	 * Converts the pattern into String representation of regular expressions.
	 */
	public static String buildJSONRegexpMatch(Pattern pattern) {
		return buildJSONRegexpMatchStringOptionalQuotes(pattern.pattern());
	}

	/**
	 * Converts the String into String representation of regular expressions.
	 */
	public static String buildJSONRegexpMatchString(String value) {
		return WS + '"' + value + '"' + WS;
	}

	/**
	 * Converts the String into an optional String representation of regular expressions.
	 */
	public static String buildJSONRegexpMatchStringOptionalQuotes(String value) {
		return WS + "\"?" + value + "\"?" + WS;
	}

}
