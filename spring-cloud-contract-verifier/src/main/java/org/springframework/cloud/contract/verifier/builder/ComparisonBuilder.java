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

import static org.apache.commons.text.StringEscapeUtils.escapeJava;

interface ComparisonBuilder {

	ComparisonBuilder JAVA_HTTP_INSTANCE = () -> RestAssuredBodyParser.INSTANCE;

	ComparisonBuilder JAVA_MESSAGING_INSTANCE = () -> JavaMessagingBodyParser.INSTANCE;

	default String createComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return matches((Pattern) headerValue);
		}
		else if (headerValue instanceof Number) {
			return isEqualTo((Number) headerValue);
		}
		String escapedHeader = convertUnicodeEscapesIfRequired(headerValue.toString());
		return isEqualTo(escapedHeader);
	}

	default String createUnescapedComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return createComparison((Pattern) headerValue);
		}
		else if (headerValue instanceof Number) {
			return isEqualTo((Number) headerValue);
		}
		return isEqualTo(headerValue.toString());
	}

	default String assertThat(String object) {
		return "assertThat(" + object + ")";
	}

	default String assertThatIsNotNull(String object) {
		return assertThat(object) + isNotNull();
	}

	default String assertThat(String object, Object valueToCompareAgainst) {
		return assertThat(object) + createComparison(valueToCompareAgainst);
	}

	default String assertThatUnescaped(String object, Object valueToCompareAgainst) {
		return assertThat(object) + createUnescapedComparison(valueToCompareAgainst);
	}

	default String isEqualTo(String escapedHeaderValue) {
		return isEqualToUnquoted(bodyParser().quotedShortText(escapedHeaderValue));
	}

	default String isEqualToUnquoted(String unquoted) {
		return ".isEqualTo(" + unquoted + ")";
	}

	default String isEqualTo(Number number) {
		String numberString = number instanceof Long ? number.toString() + "L" : number.toString();
		return ".isEqualTo(" + numberString + ")";
	}

	default String isNotNull() {
		return ".isNotNull()";
	}

	default String matches(Pattern pattern) {
		String escapedPattern = StringEscapeUtils.escapeJava(pattern.pattern());
		return ".matches(" + bodyParser().quotedEscapedShortText(escapedPattern) + ")";
	}

	default String matches(String pattern) {
		return ".matches(" + bodyParser().quotedShortText(pattern) + ")";
	}

	default String matchesEscaped(String pattern) {
		return ".matches(" + bodyParser().quotedEscapedShortText(pattern) + ")";
	}

	default String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeJson(json);
		return escapeJava(unescapedJson);
	}

	BodyParser bodyParser();

}
