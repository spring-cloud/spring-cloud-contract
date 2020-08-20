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

import java.util.Iterator;
import java.util.regex.Pattern;

import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.Cookies;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

interface CookieElementProcessor {

	ComparisonBuilder comparisonBuilder();

	default void processCookies(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Cookies cookies = response.getCookies();
		Iterator<Cookie> iterator = cookies.getEntries().iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			String text = processCookieElement(cookie.getKey(),
					cookie.getServerValue() instanceof NotToEscapePattern
							? cookie.getServerValue()
							: MapConverter.getTestSideValues(cookie.getServerValue()));
			if (iterator.hasNext()) {
				blockBuilder().addLine(text).addEndingIfNotPresent();
			}
			else {
				blockBuilder().addIndented(text).addEndingIfNotPresent();
			}
		}
	}

	BlockBuilder blockBuilder();

	default String processCookieElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			verifyCookieNotNull(property);
			return comparisonBuilder().assertThat(cookieValue(property))
					+ comparisonBuilder().matches(((NotToEscapePattern) value)
							.getServerValue().pattern().replace("\\", "\\\\"));
		}
		else if (value instanceof String || value instanceof Pattern) {
			verifyCookieNotNull(property);
			return comparisonBuilder().assertThat(cookieValue(property), value);
		}
		else if (value instanceof Number) {
			verifyCookieNotNull(property);
			return comparisonBuilder().assertThat(cookieValue(property), value);
		}
		else if (value instanceof ExecutionProperty) {
			verifyCookieNotNull(property);
			return ((ExecutionProperty) value).insertValue(cookieValue(property));

		}
		else {
			// fallback
			return processCookieElement(property, value.toString());
		}
	}

	default void verifyCookieNotNull(String key) {
		blockBuilder().addLineWithEnding(
				comparisonBuilder().assertThatIsNotNull(cookieKey(key)));
	}

	String cookieKey(String key);

	default String cookieValue(String key) {
		return cookieKey(key);
	}

}
