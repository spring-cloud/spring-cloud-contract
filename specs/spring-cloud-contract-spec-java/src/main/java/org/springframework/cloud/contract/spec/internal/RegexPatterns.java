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

package org.springframework.cloud.contract.spec.internal;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.util.RegexpUtils;

/**
 * Contains most common regular expression patterns.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public final class RegexPatterns {

	private static final Log log = LogFactory.getLog(RegexPatterns.class);

	public RegexPatterns() {
		log.warn("WARNING: RegexPatterns shouldn't be instantiated. Use its static methods instead.");
	}

	protected static final Pattern TRUE_OR_FALSE = Pattern.compile("(true|false)");

	protected static final Pattern ALPHA_NUMERIC = Pattern.compile("[a-zA-Z0-9]+");

	protected static final Pattern ONLY_ALPHA_UNICODE = Pattern.compile("[\\p{L}]*");

	protected static final Pattern NUMBER = Pattern.compile("-?(\\d*\\.\\d+|\\d+)");

	protected static final Pattern INTEGER = Pattern.compile("-?(\\d+)");

	protected static final Pattern POSITIVE_INT = Pattern.compile("([1-9]\\d*)");

	protected static final Pattern DOUBLE = Pattern.compile("-?(\\d*\\.\\d+)");

	protected static final Pattern HEX = Pattern.compile("[a-fA-F0-9]+");

	protected static final Pattern IP_ADDRESS = Pattern.compile(
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

	protected static final Pattern HOSTNAME_PATTERN = Pattern.compile("((http[s]?|ftp):/)/?([^:/\\s]+)(:[0-9]{1,5})?");

	protected static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

	protected static final Pattern URL = UrlHelper.URL;

	protected static final Pattern HTTPS_URL = UrlHelper.HTTPS_URL;

	protected static final Pattern UUID = Pattern
			.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", Pattern.CASE_INSENSITIVE);

	protected static final Pattern UUID4 = Pattern
			.compile("[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}", Pattern.CASE_INSENSITIVE);

	protected static final Pattern ANY_DATE = Pattern
			.compile("(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");

	protected static final Pattern ANY_DATE_TIME = Pattern.compile(
			"([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])");

	protected static final Pattern ANY_TIME = Pattern.compile("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])");

	protected static final Pattern NON_EMPTY = Pattern.compile("[\\S\\s]+");

	protected static final Pattern NON_BLANK = Pattern.compile("^\\s*\\S[\\S\\s]*");

	protected static final Pattern ISO8601_WITH_OFFSET = Pattern.compile(
			"([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.\\d+)?(Z|[+-][01]\\d:[0-5]\\d)");

	protected static Pattern anyOf(String... values) {
		return Pattern
				.compile(Arrays.stream(values).map(it -> '^' + RegexpUtils.escapeSpecialRegexWithSingleEscape(it) + '$')
						.collect(Collectors.joining("|")));
	}

	// tag::regexps[]

	public static RegexProperty onlyAlphaUnicode() {
		return new RegexProperty(ONLY_ALPHA_UNICODE).asString();
	}

	public static RegexProperty alphaNumeric() {
		return new RegexProperty(ALPHA_NUMERIC).asString();
	}

	public static RegexProperty number() {
		return new RegexProperty(NUMBER).asDouble();
	}

	public static RegexProperty positiveInt() {
		return new RegexProperty(POSITIVE_INT).asInteger();
	}

	public static RegexProperty anyBoolean() {
		return new RegexProperty(TRUE_OR_FALSE).asBooleanType();
	}

	public static RegexProperty anInteger() {
		return new RegexProperty(INTEGER).asInteger();
	}

	public static RegexProperty aDouble() {
		return new RegexProperty(DOUBLE).asDouble();
	}

	public static RegexProperty ipAddress() {
		return new RegexProperty(IP_ADDRESS).asString();
	}

	public static RegexProperty hostname() {
		return new RegexProperty(HOSTNAME_PATTERN).asString();
	}

	public static RegexProperty email() {
		return new RegexProperty(EMAIL).asString();
	}

	public static RegexProperty url() {
		return new RegexProperty(URL).asString();
	}

	public static RegexProperty httpsUrl() {
		return new RegexProperty(HTTPS_URL).asString();
	}

	public static RegexProperty uuid() {
		return new RegexProperty(UUID).asString();
	}

	public static RegexProperty uuid4() {
		return new RegexProperty(UUID4).asString();
	}

	public static RegexProperty isoDate() {
		return new RegexProperty(ANY_DATE).asString();
	}

	public static RegexProperty isoDateTime() {
		return new RegexProperty(ANY_DATE_TIME).asString();
	}

	public static RegexProperty isoTime() {
		return new RegexProperty(ANY_TIME).asString();
	}

	public static RegexProperty iso8601WithOffset() {
		return new RegexProperty(ISO8601_WITH_OFFSET).asString();
	}

	public static RegexProperty nonEmpty() {
		return new RegexProperty(NON_EMPTY).asString();
	}

	public static RegexProperty nonBlank() {
		return new RegexProperty(NON_BLANK).asString();
	}

	// end::regexps[]

}
