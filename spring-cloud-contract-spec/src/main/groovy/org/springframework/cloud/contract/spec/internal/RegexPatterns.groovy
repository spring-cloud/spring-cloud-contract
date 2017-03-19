/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.util.regex.Pattern

/**
 * Contains most common regular expression patterns
 *
 * @since 1.0.0
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class RegexPatterns {

	// tag::regexps[]
	protected static final Pattern TRUE_OR_FALSE = Pattern.compile(/(true|false)/)
	protected static final Pattern ONLY_ALPHA_UNICODE = Pattern.compile(/[\p{L}]*/)
	protected static final Pattern NUMBER = Pattern.compile('-?\\d*(\\.\\d+)?')
	protected static final Pattern IP_ADDRESS = Pattern.compile('([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])')
	protected static final Pattern HOSTNAME_PATTERN = Pattern.compile('((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?')
	protected static final Pattern EMAIL = Pattern.compile('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');
	protected static final Pattern URL = Pattern.compile('((www\\.|(http|https|ftp|news|file)+\\:\\/\\/)[_.a-z0-9-]+\\.[a-z0-9\\/_:@=.+?,##%&~-]*[^.|\\\'|\\# |!|\\(|?|,| |>|<|;|\\)])')
	protected static final Pattern UUID = Pattern.compile('[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}')
	protected static final Pattern ANY_DATE = Pattern.compile('(\\d\\d\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])')
	protected static final Pattern ANY_DATE_TIME = Pattern.compile('([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])')
	protected static final Pattern ANY_TIME = Pattern.compile('(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])')
	protected static final Pattern NON_EMPTY = Pattern.compile(/.+/)
	protected static final Pattern NON_BLANK = Pattern.compile(/.*(\S+|\R).*|!^\R*$/)
	protected static final Pattern ISO8601_WITH_OFFSET = Pattern.compile(/([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\.\d{3})?(Z|[+-][01]\d:[0-5]\d)/)

	protected static Pattern anyOf(String... values){
		return Pattern.compile(values.collect({"^$it\$"}).join("|"))
	}

	String onlyAlphaUnicode() {
		return ONLY_ALPHA_UNICODE.pattern()
	}

	String number() {
		return NUMBER.pattern()
	}

	String anyBoolean() {
		return TRUE_OR_FALSE.pattern()
	}

	String ipAddress() {
		return IP_ADDRESS.pattern()
	}

	String hostname() {
		return HOSTNAME_PATTERN.pattern()
	}

	String email() {
		return EMAIL.pattern()
	}

	String url() {
		return URL.pattern()
	}

	String uuid(){
		return UUID.pattern()
	}

	String isoDate() {
		return ANY_DATE.pattern()
	}

	String isoDateTime() {
		return ANY_DATE_TIME.pattern()
	}

	String isoTime() {
		return ANY_TIME.pattern()
	}

	String iso8601WithOffset() {
		return ISO8601_WITH_OFFSET.pattern()
	}

	String nonEmpty() {
		return NON_EMPTY.pattern()
	}

	String nonBlank() {
		return NON_BLANK.pattern()
	}

	// end::regexps[]
	
	static String multipartParam(Object name, Object value) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"$name\"\r\n(Content-Type: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n$value\r\n--\\1.*"
	}

	static String multipartFile(Object name, Object filename, Object content) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n(Content-Type: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n$content\r\n--\\1.*";
	}
}
