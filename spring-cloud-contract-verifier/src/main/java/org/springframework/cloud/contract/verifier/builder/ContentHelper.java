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

import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.verifier.util.MapConverter;

final class ContentHelper {

	/**
	 * Depending on the object type extracts the test side values and combines them into a
	 * String representation. Will not try to guess the type of the value of the header
	 * (e.g. if it's a JSON).
	 */
	static String getTestSideForNonBodyValue(Object object) {
		if (object instanceof ExecutionProperty) {
			return getTestSideValue(object);
		}
		return quotedAndEscaped(
				MapConverter.getTestSideValuesForNonBody(object).toString());
	}

	/**
	 * Depending on the object type extracts the test side values and combines them into a
	 * String representation
	 */
	private static String getTestSideValue(Object object) {
		if (object instanceof ExecutionProperty) {
			return object.toString();
		}
		return '"' + MapConverter.getTestSideValues(object).toString() + '"';
	}

	private static String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"';
	}

}
