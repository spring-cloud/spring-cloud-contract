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

/**
 * Matching type with corresponding values.
 */
public class RegexMatchingTypeValue extends MatchingTypeValue {

	RegexMatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence,
			Integer maxTypeOccurrence) {
		super(type, value, minTypeOccurrence, maxTypeOccurrence);
	}

	RegexMatchingTypeValue(MatchingType type, Object value) {
		super(type, value);
	}

	public RegexMatchingTypeValue asInteger() {
		return typed(Integer.class);
	}

	private RegexMatchingTypeValue typed(Class clazz) {
		if (!(this.getValue() instanceof RegexProperty)) {
			throw new IllegalStateException("Value has to be a regex");
		}
		RegexProperty regexProperty = (RegexProperty) this.getValue();
		return new RegexMatchingTypeValue(this.getType(),
				new RegexProperty(regexProperty.getClientValue(),
						regexProperty.getServerValue(), clazz),
				this.getMinTypeOccurrence(), this.getMaxTypeOccurrence());
	}

	public RegexMatchingTypeValue asDouble() {
		return typed(Double.class);
	}

	public RegexMatchingTypeValue asFloat() {
		return typed(Float.class);
	}

	public RegexMatchingTypeValue asLong() {
		return typed(Long.class);
	}

	public RegexMatchingTypeValue asShort() {
		return typed(Short.class);
	}

	public RegexMatchingTypeValue asString() {
		return typed(String.class);
	}

	public RegexMatchingTypeValue asBooleanType() {
		return typed(Boolean.class);
	}

}
