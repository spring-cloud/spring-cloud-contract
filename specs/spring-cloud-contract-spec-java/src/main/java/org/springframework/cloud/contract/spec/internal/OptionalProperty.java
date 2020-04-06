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

package org.springframework.cloud.contract.spec.internal;

import java.io.Serializable;
import java.util.regex.Pattern;

import repackaged.nl.flotsam.xeger.Xeger;

/**
 * Represents a property that may or may not be there.
 *
 * @since 1.0.0
 */
public class OptionalProperty implements Serializable, CanBeDynamic {

	private final Object value;

	public OptionalProperty(Object value) {
		this.value = value;
	}

	/**
	 * String version of a regular expression that wraps the provided value in an optional
	 * function.
	 * @return pattern wrapped in optional regular expresion
	 */
	public String optionalPattern() {
		return "(" + value() + ")?";
	}

	public String value() {
		return valueToCheck(this.value).toString();
	}

	private Object valueToCheck(Object value) {
		if (value == null) {
			return "";
		}
		else if (value instanceof ClientDslProperty) {
			return valueToCheck(((ClientDslProperty) value).getClientValue());
		}
		else if (value instanceof RegexProperty || value instanceof Pattern) {
			return new RegexProperty(value).pattern();
		}
		else {
			return value.toString();
		}
	}

	protected Pattern optionalPatternValue() {
		return Pattern.compile(optionalPattern());
	}

	@Override
	public String toString() {
		return optionalPattern();
	}

	@Override
	public Object generateConcreteValue() {
		return new Xeger(optionalPattern()).generate();
	}

	public final Object getValue() {
		return value;
	}

}
