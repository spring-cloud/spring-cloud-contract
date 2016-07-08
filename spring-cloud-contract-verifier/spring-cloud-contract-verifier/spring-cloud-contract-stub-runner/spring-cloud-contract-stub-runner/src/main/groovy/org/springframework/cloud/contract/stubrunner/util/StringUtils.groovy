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

/**
 * Utils ported from Apache Commons
 *
 * @author Marcin Grzejszczak
 */
class StringUtils {
	public static String EMPTY = ""
	private static int INDEX_NOT_FOUND = -1

	// Empty checks
	//-----------------------------------------------------------------------
	/**
	 * <p>Checks if a String is empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * <p>NOTE: This method changed in Lang version 2.0.
	 * It no longer trims the String.
	 * That functionality is available in isBlank().</p>
	 *
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	static boolean isNotEmpty(String string) {
		return string != null && !string.empty
	}

	static boolean hasText(String string) {
		return isNotEmpty(string) && !string.allWhitespace
	}

	/**
	 * <p>Gets the substring before the last occurrence of a separator.
	 * The separator is not returned.</p>
	 *
	 * <p>A <code>null</code> string input will return <code>null</code>.
	 * An empty ("") string input will return the empty string.
	 * An empty or <code>null</code> separator will return the input string.</p>
	 *
	 * <p>If nothing is found, the string input is returned.</p>
	 *
	 * <pre>
	 * StringUtils.substringBeforeLast(null, *)      = null
	 * StringUtils.substringBeforeLast("", *)        = ""
	 * StringUtils.substringBeforeLast("abcba", "b") = "abc"
	 * StringUtils.substringBeforeLast("abc", "c")   = "ab"
	 * StringUtils.substringBeforeLast("a", "a")     = ""
	 * StringUtils.substringBeforeLast("a", "z")     = "a"
	 * StringUtils.substringBeforeLast("a", null)    = "a"
	 * StringUtils.substringBeforeLast("a", "")      = "a"
	 * </pre>
	 *
	 * @param str the String to get a substring from, may be null
	 * @param separator the String to search for, may be null
	 * @return the substring before the last occurrence of the separator,
	 *  <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String substringBeforeLast(String str, String separator) {
		if (isEmpty(str) || isEmpty(separator)) {
			return str;
		}
		int pos = str.lastIndexOf(separator);
		if (pos == INDEX_NOT_FOUND) {
			return str;
		}
		return str.substring(0, pos);
	}

	/**
	 * <p>Gets the substring after the last occurrence of a separator.
	 * The separator is not returned.</p>
	 *
	 * <p>A <code>null</code> string input will return <code>null</code>.
	 * An empty ("") string input will return the empty string.
	 * An empty or <code>null</code> separator will return the empty string if
	 * the input string is not <code>null</code>.</p>
	 *
	 * <p>If nothing is found, the empty string is returned.</p>
	 *
	 * <pre>
	 * StringUtils.substringAfterLast(null, *)      = null
	 * StringUtils.substringAfterLast("", *)        = ""
	 * StringUtils.substringAfterLast(*, "")        = ""
	 * StringUtils.substringAfterLast(*, null)      = ""
	 * StringUtils.substringAfterLast("abc", "a")   = "bc"
	 * StringUtils.substringAfterLast("abcba", "b") = "a"
	 * StringUtils.substringAfterLast("abc", "c")   = ""
	 * StringUtils.substringAfterLast("a", "a")     = ""
	 * StringUtils.substringAfterLast("a", "z")     = ""
	 * </pre>
	 *
	 * @param str the String to get a substring from, may be null
	 * @param separator the String to search for, may be null
	 * @return the substring after the last occurrence of the separator,
	 *  <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String substringAfterLast(String str, String separator) {
		if (isEmpty(str)) {
			return str;
		}
		if (isEmpty(separator)) {
			return EMPTY;
		}
		int pos = str.lastIndexOf(separator);
		if (pos == INDEX_NOT_FOUND || pos == (str.length() - separator.length())) {
			return EMPTY;
		}
		return str.substring(pos + separator.length());
	}
}
