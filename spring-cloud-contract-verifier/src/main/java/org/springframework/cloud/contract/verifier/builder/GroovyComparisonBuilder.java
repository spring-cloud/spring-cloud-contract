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

package org.springframework.cloud.contract.verifier.builder;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

interface GroovyComparisonBuilder extends ComparisonBuilder {

	ComparisonBuilder SPOCK_HTTP_INSTANCE = (GroovyComparisonBuilder) () -> SpockRestAssuredBodyParser.INSTANCE;

	ComparisonBuilder JAXRS_HTTP_INSTANCE = (GroovyComparisonBuilder) () -> JaxRsBodyParser.INSTANCE;

	ComparisonBuilder SPOCK_MESSAGING_INSTANCE = (GroovyComparisonBuilder) () -> SpockMessagingBodyParser.INSTANCE;

	@Override
	default String assertThat(String object) {
		return object;
	}

	@Override
	default String isEqualToUnquoted(String unquoted) {
		return " == " + unquoted;
	}

	@Override
	default String isEqualTo(Number number) {
		String numberString = number instanceof Long ? number.toString() + "L"
				: number.toString();
		return " == " + numberString;
	}

	@Override
	default String matches(String pattern) {
		return matchesEscaped(bodyParser().quotedShortText(pattern));
	}

	@Override
	default String matches(Pattern pattern) {
		String escapedPattern = StringEscapeUtils.escapeJava(pattern.pattern());
		return matchesEscaped(escapedPattern);
	}

	@Override
	default String matchesEscaped(String pattern) {
		return " ==~ java.util.regex.Pattern.compile("
				+ bodyParser().quotedEscapedShortText(pattern) + ")";
	}

	@Override
	default String isNotNull() {
		return " != null";
	}

}
