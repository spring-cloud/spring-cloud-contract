package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class RegexPatterns {

	private static final Pattern IP_ADDRESS = Pattern.compile('([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])');
	private static final Pattern HOSTNAME_PATTERN = Pattern.compile('((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?');
	private static final Pattern EMAIL = Pattern.compile('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');
	private static final Pattern URL = Pattern.compile('((www\\.|(http|https|ftp|news|file)+\\:\\/\\/)[_.a-z0-9-]+\\.[a-z0-9\\/_:@=.+?,##%&~-]*[^.|\\\'|\\# |!|\\(|?|,| |>|<|;|\\)])');

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
}
