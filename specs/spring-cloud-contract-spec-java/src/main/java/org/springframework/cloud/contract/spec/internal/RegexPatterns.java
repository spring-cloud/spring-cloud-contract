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

package org.springframework.cloud.contract.spec.internal;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains most common regular expression patterns.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
class RegexPatterns {

	// tag::regexps[]
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

	protected static final Pattern HOSTNAME_PATTERN = Pattern
			.compile("((http[s]?|ftp):/)/?([^:/\\s]+)(:[0-9]{1,5})?");

	protected static final Pattern EMAIL = Pattern
			.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

	protected static final Pattern URL = UrlHelper.URL;

	protected static final Pattern HTTPS_URL = UrlHelper.HTTPS_URL;

	protected static final Pattern UUID = Pattern
			.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

	protected static final Pattern ANY_DATE = Pattern
			.compile("(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");

	protected static final Pattern ANY_DATE_TIME = Pattern.compile(
			"([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])");

	protected static final Pattern ANY_TIME = Pattern
			.compile("(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])");

	protected static final Pattern NON_EMPTY = Pattern.compile("[\\S\\s]+");

	protected static final Pattern NON_BLANK = Pattern.compile("^\\s*\\S[\\S\\s]*");

	protected static final Pattern ISO8601_WITH_OFFSET = Pattern.compile(
			"([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.\\d{3})?(Z|[+-][01]\\d:[0-5]\\d)");

	protected static Pattern anyOf(String... values) {
		return Pattern.compile(Arrays.stream(values).map(it -> '^' + it + '$')
				.collect(Collectors.joining("|")));
	}

	public static String multipartParam(Object name, Object value) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"" + name
				+ "\"\r\n(Content-Type: .*\r\n)?(Content-Transfer-Encoding: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n"
				+ value + "\r\n--\\1.*";
	}

	public static String multipartFile(Object name, Object filename, Object content,
			Object contentType) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"" + name
				+ "\"; filename=\"" + filename + "\"\r\n(Content-Type: "
				+ toContentType(contentType)
				+ "\r\n)?(Content-Transfer-Encoding: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n"
				+ content + "\r\n--\\1.*";
	}

	private static String toContentType(Object contentType) {
		if (contentType == null) {
			return ".*";
		}
		if (contentType instanceof RegexProperty) {
			return ((RegexProperty) contentType).pattern();
		}
		return contentType.toString();
	}

	public RegexProperty onlyAlphaUnicode() {
		return new RegexProperty(ONLY_ALPHA_UNICODE).asString();
	}

	public RegexProperty alphaNumeric() {
		return new RegexProperty(ALPHA_NUMERIC).asString();
	}

	public RegexProperty number() {
		return new RegexProperty(NUMBER).asDouble();
	}

	public RegexProperty positiveInt() {
		return new RegexProperty(POSITIVE_INT).asInteger();
	}

	public RegexProperty anyBoolean() {
		return new RegexProperty(TRUE_OR_FALSE).asBooleanType();
	}

	public RegexProperty anInteger() {
		return new RegexProperty(INTEGER).asInteger();
	}

	public RegexProperty aDouble() {
		return new RegexProperty(DOUBLE).asDouble();
	}

	public RegexProperty ipAddress() {
		return new RegexProperty(IP_ADDRESS).asString();
	}

	public RegexProperty hostname() {
		return new RegexProperty(HOSTNAME_PATTERN).asString();
	}

	public RegexProperty email() {
		return new RegexProperty(EMAIL).asString();
	}

	public RegexProperty url() {
		return new RegexProperty(URL).asString();
	}

	public RegexProperty httpsUrl() {
		return new RegexProperty(HTTPS_URL).asString();
	}

	public RegexProperty uuid() {
		return new RegexProperty(UUID).asString();
	}

	public RegexProperty isoDate() {
		return new RegexProperty(ANY_DATE).asString();
	}

	public RegexProperty isoDateTime() {
		return new RegexProperty(ANY_DATE_TIME).asString();
	}

	public RegexProperty isoTime() {
		return new RegexProperty(ANY_TIME).asString();
	}

	// end::regexps[]

	public RegexProperty iso8601WithOffset() {
		return new RegexProperty(ISO8601_WITH_OFFSET).asString();
	}

	public RegexProperty nonEmpty() {
		return new RegexProperty(NON_EMPTY).asString();
	}

	public RegexProperty nonBlank() {
		return new RegexProperty(NON_BLANK).asString();
	}

}
