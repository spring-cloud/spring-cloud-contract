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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * Contains most common regular expression patterns
 *
 * @since 1.0.0
 */
@CompileStatic
class RegexPatterns {

	// tag::regexps[]
	private static final Pattern TRUE_OR_FALSE = Pattern.compile(/(true|false)/)
	private static final Pattern ONLY_ALPHA_UNICODE = Pattern.compile(/[\p{L}]*/)
	private static final Pattern NUMBER = Pattern.compile('-?\\d*(\\.\\d+)?')
	private static final Pattern IP_ADDRESS = Pattern.compile('([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])');
	private static final Pattern HOSTNAME_PATTERN = Pattern.compile('((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?');
	private static final Pattern EMAIL = Pattern.compile('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');
	private static final Pattern URL = Pattern.compile('((www\\.|(http|https|ftp|news|file)+\\:\\/\\/)[_.a-z0-9-]+\\.[a-z0-9\\/_:@=.+?,##%&~-]*[^.|\\\'|\\# |!|\\(|?|,| |>|<|;|\\)])')

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
	// end::regexps[]
	
	static String multipartParam(Object name, Object value) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"$name\"\r\n(Content-Type: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n$value\r\n--\\1.*"
	}

	static String multipartFile(Object name, Object filename, Object content) {
		return ".*--(.*)\r\nContent-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n(Content-Type: .*\r\n)?(Content-Length: \\d+\r\n)?\r\n$content\r\n--\\1.*";
	}
}
