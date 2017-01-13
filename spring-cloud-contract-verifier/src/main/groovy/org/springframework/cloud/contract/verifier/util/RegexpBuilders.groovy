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

package org.springframework.cloud.contract.verifier.util

import groovy.transform.TypeChecked
import org.codehaus.groovy.runtime.GStringImpl
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.util.RegexpUtils

import java.util.regex.Pattern

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson
import static ContentType.*
import static ContentUtils.extractValue

/**
 * Useful utility methods to work with regular expresisons
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @since 1.0.0
 */
@TypeChecked
class RegexpBuilders {

	/**
	 * Converts the {@link GString} passed values into their stub side String representations
	 */
	static String buildGStringRegexpForStubSide(GString gString) {
		new GStringImpl(
				gString.values.collect(this.&buildGStringRegexpForStubSide) as Object[],
				gString.strings.collect(this.&escapeSpecialRegexChars) as String[]
		)
	}

	/**
	 * Converts the {@link Pattern} passed values into their stub side String representations
	 */
	static String buildGStringRegexpForStubSide(Pattern pattern) {
		return pattern.pattern()
	}

	/**
	 * Converts the {@link org.springframework.cloud.contract.spec.internal.DslProperty} passed values into their stub side String representations
	 */
	static String buildGStringRegexpForStubSide(DslProperty dslProperty) {
		return buildGStringRegexpForStubSide(dslProperty.clientValue)
	}

	/**
	 * Converts the {@link Object} passed values into their stub side String representations
	 */
	static String buildGStringRegexpForStubSide(Object o) {
		return escapeSpecialRegexChars(o.toString())
	}

	/**
	 * Converts the {@link GString} passed values into their test side String representations
	 */
	static String buildGStringRegexpForTestSide(GString gString) {
		new GStringImpl(
				gString.values.collect(this.&buildGStringRegexpForTestSide) as Object[],
				gString.strings.collect(this.&escapeSpecialRegexChars) as String[]
		)
	}

	/**
	 * Converts the {@link Pattern} passed values into their test side String representations
	 */
	static String buildGStringRegexpForTestSide(Pattern pattern) {
		return pattern.pattern()
	}

	/**
	 * Converts the {@link DslProperty} passed values into their test side String representations
	 */
	static String buildGStringRegexpForTestSide(DslProperty dslProperty) {
		return buildGStringRegexpForTestSide(dslProperty.clientValue)
	}

	/**
	 * Converts the {@link Object} passed values into their test side String representations
	 */
	static String buildGStringRegexpForTestSide(Object o) {
		return o.toString().replaceAll('\\\\', '\\\\\\\\')
	}

	static String escapeSpecialRegexChars(String str) {
		return RegexpUtils.escapeSpecialRegexChars(str)
	}

	private final static String WS = /\s*/

	static String buildJSONRegexpMatch(GString gString) {
		return buildJSONRegexpMatch(extractValue(gString, JSON, { DslProperty dslProperty -> dslProperty.clientValue }))
	}

	static String buildJSONRegexpMatch(Map jsonMap) {
		return WS + "\\{" + jsonMap.collect(this.&buildJSONRegexpMatch).join(",") + "\\}" + WS
	}

	static String buildJSONRegexpMatch(List jsonList) {
		return WS + "\\[" + jsonList.collect(this.&buildJSONRegexpMatch).join(",") + "\\]" + WS
	}

	/**
	 * Converts the map into String representation of regular expressions
	 */
	static String buildJSONRegexpMatch(Map.Entry<String, Object> entry) {
		return buildJSONRegexpMatchString(escapeJson(entry.key)) + ":" + buildJSONRegexpMatch(entry.value)
	}

	/**
	 * Converts the object into String representation of regular expressions
	 */
	static String buildJSONRegexpMatch(Object value) {
		return buildJSONRegexpMatchStringOptionalQuotes(escapeJson(value.toString()))
	}

	/**
	 * Converts the pattern into String representation of regular expressions
	 */
	static String buildJSONRegexpMatch(Pattern pattern) {
		return buildJSONRegexpMatchStringOptionalQuotes(pattern.pattern())
	}

	/**
	 * Converts the String into String representation of regular expressions
	 */
	static String buildJSONRegexpMatchString(String value) {
		return WS + '"' + value + '"' + WS
	}

	/**
	 * Converts the String into an optional String representation of regular expressions
	 */
	static String buildJSONRegexpMatchStringOptionalQuotes(String value) {
		return WS + '"?' + value + '"?' + WS
	}

}
