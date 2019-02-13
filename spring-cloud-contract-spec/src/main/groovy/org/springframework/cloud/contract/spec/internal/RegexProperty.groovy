/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal

import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.apache.commons.text.StringEscapeUtils
import repackaged.nl.flotsam.xeger.Xeger

/**
 * Represents a regular expression property
 *
 * @since 2.1.0
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class RegexProperty extends DslProperty implements CanBeDynamic {
	final Pattern pattern
	private final Class clazz

	RegexProperty(Object value) {
		this(value, value, null)
	}

	RegexProperty(Object client, Object server) {
		this(client, server, null)
	}

	RegexProperty(Object client, Object server, Class clazz) {
		super(client, server)
		boolean clientDynamic = client instanceof Pattern ||
				client instanceof RegexProperty
		boolean serverDynamic = server instanceof Pattern ||
				server instanceof RegexProperty
		if (!clientDynamic && !serverDynamic) {
			throw new IllegalStateException("Neither client not server side is dynamic")
		}
		Object dynamicValue = clientDynamic ? client : server
		if (dynamicValue instanceof Pattern) {
			this.pattern = dynamicValue
			this.clazz = clazz ?: String
		}
		else if (dynamicValue instanceof RegexProperty) {
			RegexProperty regexProperty = ((RegexProperty) dynamicValue)
			this.pattern = regexProperty.pattern
			this.clazz = clazz ?: regexProperty.clazz
		}
		else {
			this.clazz = clazz
		}
	}

	Matcher matcher(CharSequence input) {
		return this.pattern.matcher(input)
	}

	String pattern() {
		return this.pattern.pattern()
	}

	Class clazz() {
		return this.class
	}

	RegexProperty asInteger() {
		return new RegexProperty(this.clientValue, this.serverValue, Integer)
	}

	RegexProperty asDouble() {
		return new RegexProperty(this.clientValue, this.serverValue, Double)
	}

	RegexProperty asFloat() {
		return new RegexProperty(this.clientValue, this.serverValue, Float)
	}

	RegexProperty asLong() {
		return new RegexProperty(this.clientValue, this.serverValue, Long)
	}

	RegexProperty asShort() {
		return new RegexProperty(this.clientValue, this.serverValue, Short)
	}

	RegexProperty asString() {
		return new RegexProperty(this.clientValue, this.serverValue, String)
	}

	RegexProperty asBooleanType() {
		return new RegexProperty(this.clientValue, this.serverValue, Boolean)
	}

	Object generate() {
		String generatedValue = new Xeger(this.pattern.pattern()).generate()
		switch (this.clazz) {
		case Integer: return Integer.parseInt(generatedValue)
		case Double: return Double.parseDouble(generatedValue)
		case Float: return Float.parseFloat(generatedValue)
		case Long: return Long.parseLong(generatedValue)
		case Short: return Short.parseShort(generatedValue)
		case Boolean: return Boolean.parseBoolean(generatedValue)
		default: return generatedValue
		}
	}

	Object generateAndEscapeJavaStringIfNeeded() {
		Object generated = generate()
		if (isNumber()) {
			return generated
		}
		return StringEscapeUtils.escapeJava(generated as String)
	}

	private boolean isNumber() {
		return Number.isAssignableFrom(this.clazz)
	}

	RegexProperty dynamicClientConcreteProducer() {
		return new RegexProperty(this.pattern, generate(), this.clazz)
	}

	RegexProperty concreteClientDynamicProducer() {
		return new RegexProperty(generate(), this.pattern, this.clazz)
	}

	RegexProperty concreteClientEscapedDynamicProducer() {
		return new RegexProperty(
				generateAndEscapeJavaStringIfNeeded(), this.pattern, this.clazz)
	}

	RegexProperty dynamicClientEscapedConcreteProducer() {
		return new RegexProperty(this.pattern,
				generateAndEscapeJavaStringIfNeeded(), this.clazz)
	}

	boolean equals(o) {
		if (this.is(o)) {
			return true
		}
		if (getClass() != o.class) {
			return false
		}
		if (!super.equals(o)) {
			return false
		}
		RegexProperty that = (RegexProperty) o
		if (this.clazz != that.clazz) {
			return false
		}
		if (this.pattern != that.pattern) {
			return false
		}
		return true
	}

	int hashCode() {
		int result = super.hashCode()
		result = 31 * result + (this.pattern != null ? pattern.hashCode() : 0)
		result = 31 * result + (this.clazz != null ? clazz.hashCode() : 0)
		return result
	}

	@Override
	String toString() {
		return this.pattern()
	}

	@Override
	Object generateConcreteValue() {
		return generate()
	}
}
