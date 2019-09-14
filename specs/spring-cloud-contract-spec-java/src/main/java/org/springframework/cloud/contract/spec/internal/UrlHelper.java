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

import java.util.regex.Pattern;

/**
 * Taken from https://gist.github.com/skeller88/5eb73dc0090d4ff1249a.
 */
final class UrlHelper {

	/**
	 * Example: "http". Also called 'protocol'. Scheme component is optional, even though
	 * the RFC doesn't make it optional. Since ((RegexProperty) this regex is validating a
	 * submitted callback url, which determines where the browser will navigate to after a
	 * successful authentication, the browser will use http or https for the scheme by
	 * default. Not borrowed from dperini in order to allow any scheme type.
	 */
	private static final String REGEX_SCHEME = "[A-Za-z][+-.\\w^_]*:";

	private static final String HTTPS_REGEX_SCHEME = "https:";

	private static final String REGEX_AUTHORATIVE_DECLARATION = "/{2}";

	private static final String REGEX_USERINFO = "(?:\\S+(?::\\S*)?@)?";

	private static final String REGEX_HOST = "(?:"
			+ "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
			+ "|" + "(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)"
			+ "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*"
			+ "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))|(?:localhost))";

	private static final String REGEX_PORT = "(?::\\d{2,5})?";

	private static final String REGEX_RESOURCE_PATH = "(?:/\\S*)?";

	protected static final Pattern HTTPS_URL = Pattern.compile(
			"^(?:" + HTTPS_REGEX_SCHEME + REGEX_AUTHORATIVE_DECLARATION + REGEX_USERINFO
					+ REGEX_HOST + REGEX_PORT + REGEX_RESOURCE_PATH + ")$");

	protected static final Pattern URL = Pattern.compile("^(?:(?:" + REGEX_SCHEME
			+ REGEX_AUTHORATIVE_DECLARATION + ")?" + REGEX_USERINFO + REGEX_HOST
			+ REGEX_PORT + REGEX_RESOURCE_PATH + ")$");

	private UrlHelper() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

}
