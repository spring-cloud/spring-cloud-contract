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

package org.springframework.cloud.contract.spec.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.RegexProperty;

/**
 * Checks the validity of DSL entries.
 *
 * @since 1.0.0
 */
public final class ValidateUtils {

	private ValidateUtils() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

	/**
	 * Allowed matching types on the server side.
	 */
	public static List<MatchingStrategy.Type> ALLOWED_MATCHING_TYPES_ON_SERVER_SIDE = new ArrayList<>(
			Arrays.asList(MatchingStrategy.Type.EQUAL_TO, MatchingStrategy.Type.ABSENT));

	/**
	 * Validates if for given object the server value is present.
	 * @param value value to check
	 * @param msg potential exception message
	 * @return object if everything is fine
	 */
	public static Object validateServerValueIsAvailable(Object value, String msg) {
		if (value instanceof Pattern) {
			validateServerValue((Pattern) value, msg);
		}
		else if (value instanceof RegexProperty) {
			RegexProperty property = (RegexProperty) value;
			validateServerValue(property.getPattern(), msg);
		}
		else if (value instanceof MatchingStrategy) {
			validateServerValue((MatchingStrategy) value, msg);
		}
		else if (value instanceof DslProperty) {
			validateServerValue((DslProperty) value, msg);
		}
		else {
			validateServerValue(value, msg);
		}

		return value;
	}

	public static void validateServerValue(Pattern pattern, String msg) {
		throw new IllegalStateException(msg + " can\'t be a pattern for the server side");
	}

	public static void validateServerValue(MatchingStrategy matchingStrategy,
			String msg) {
		if (!ALLOWED_MATCHING_TYPES_ON_SERVER_SIDE.contains(matchingStrategy.getType())) {
			throw new IllegalStateException(msg + " can\'t be of a matching type: "
					+ String.valueOf(matchingStrategy.getType())
					+ " for the server side");
		}

		validateServerValue(matchingStrategy.getServerValue(), msg);
	}

	public static void validateServerValue(DslProperty value, String msg) {
		validateServerValue(value.getServerValue(), msg);
	}

	public static void validateServerValue(Object value, String msg) {
		// OK
	}

}
