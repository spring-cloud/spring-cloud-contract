/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.contract.wiremock.restdocs;

import com.jayway.jsonpath.JsonPath;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

class JsonPathValue {

	private final JsonPath jsonPath;
	private final String expression;
	private final CharSequence actual;

	JsonPathValue(JsonPath jsonPath, CharSequence actual) {
		this.jsonPath = jsonPath;
		this.actual = actual;
		this.expression = jsonPath.getPath();
	}

	public void assertHasValue(Class<?> type, String expectedDescription) {
		Object value = getValue(true);
		if (value == null || isIndefiniteAndEmpty()) {
			throw new AssertionError(getNoValueMessage());
		}
		if (type != null && !type.isInstance(value)) {
			throw new AssertionError(getExpectedValueMessage(expectedDescription));
		}
	}

	private boolean isIndefiniteAndEmpty() {
		return !isDefinite() && isEmpty();
	}

	private boolean isDefinite() {
		return this.jsonPath.isDefinite();
	}

	private boolean isEmpty() {
		return ObjectUtils.isEmpty(getValue(false));
	}

	public Object getValue(boolean required) {
		try {
			CharSequence json = this.actual;
			return this.jsonPath.read(json == null ? null : json.toString());
		}
		catch (Exception ex) {
			if (!required) {
				return null;
			}
			throw new AssertionError(getNoValueMessage() + ". " + ex.getMessage());
		}
	}

	private String getNoValueMessage() {
		return "No value at JSON path \"" + this.expression + "\"";
	}

	private String getExpectedValueMessage(String expectedDescription) {
		return String.format("Expected %s at JSON path \"%s\" but found: %s",
				expectedDescription, this.expression,
				ObjectUtils.nullSafeToString(StringUtils.quoteIfString(getValue(false))));
	}

}