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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import repackaged.nl.flotsam.xeger.Xeger;

/**
 * Represents a regular expression property.
 *
 * @since 2.1.0
 */
public class RegexProperty extends DslProperty implements CanBeDynamic {

	final Pattern pattern;

	String charset = StandardCharsets.UTF_8.name();

	private final Class clazz;

	public RegexProperty(Object value) {
		this(value, value, null);
	}

	public RegexProperty(Object client, Object server) {
		this(client, server, null);
	}

	public RegexProperty(Object client, Object server, Class clazz) {
		super(client, server);
		boolean clientDynamic = client instanceof Pattern
				|| client instanceof RegexProperty;
		boolean serverDynamic = server instanceof Pattern
				|| server instanceof RegexProperty;
		if (!clientDynamic && !serverDynamic) {
			throw new IllegalStateException("Neither client not server side is dynamic");
		}
		Object dynamicValue = clientDynamic ? client : server;
		if (dynamicValue instanceof Pattern) {
			this.pattern = (Pattern) dynamicValue;
			this.clazz = clazz != null ? clazz : String.class;
		}
		else if (dynamicValue instanceof RegexProperty) {
			RegexProperty regexProperty = ((RegexProperty) dynamicValue);
			this.pattern = regexProperty.pattern;
			this.clazz = clazz != null ? clazz : regexProperty.clazz;
		}
		else {
			this.clazz = clazz;
			this.pattern = null;
		}
	}

	public Matcher matcher(CharSequence input) {
		return this.pattern.matcher(input);
	}

	public String pattern() {
		return this.pattern.pattern();
	}

	public Class clazz() {
		return this.getClass();
	}

	public RegexProperty asInteger() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Integer.class);
	}

	public RegexProperty asDouble() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Double.class);
	}

	public RegexProperty asFloat() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Float.class);
	}

	public RegexProperty asLong() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Long.class);
	}

	public RegexProperty asShort() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Short.class);
	}

	public RegexProperty asString() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				String.class);
	}

	public RegexProperty asString(Charset charset) {
		RegexProperty regexProperty = asString();
		regexProperty.charset = charset.name();
		return regexProperty;
	}

	public RegexProperty asString(String charset) {
		RegexProperty regexProperty = asString();
		regexProperty.charset = charset;
		return regexProperty;
	}

	public RegexProperty asBooleanType() {
		return new RegexProperty(this.getClientValue(), this.getServerValue(),
				Boolean.class);
	}

	public Object generate() {
		return doGenerate(3);
	}

	private Object doGenerate(int retries) {
		try {
			String generatedValue = new Xeger(this.pattern.pattern()).generate();
			if (Integer.class.equals(this.clazz)) {
				return Integer.parseInt(generatedValue);
			}
			else if (Double.class.equals(this.clazz)) {
				return Double.parseDouble(generatedValue);
			}
			else if (Float.class.equals(this.clazz)) {
				return Float.parseFloat(generatedValue);
			}
			else if (Long.class.equals(this.clazz)) {
				return Long.parseLong(generatedValue);
			}
			else if (Short.class.equals(this.clazz)) {
				return Short.parseShort(generatedValue);
			}
			else if (Boolean.class.equals(this.clazz)) {
				return Boolean.parseBoolean(generatedValue);
			}
			return new String(generatedValue.getBytes(Charset.forName(this.charset)),
					this.charset);
		}
		catch (NumberFormatException ex) {
			if (retries > 0) {
				retries = retries - 1;
				return doGenerate(retries);
			}
			throw ex;
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public Object generateAndEscapeJavaStringIfNeeded() {
		Object generated = generate();
		if (isNumber()) {
			return generated;
		}
		return StringEscapeUtils.escapeJava(String.valueOf(generated));
	}

	private boolean isNumber() {
		return Number.class.isAssignableFrom(this.clazz);
	}

	public RegexProperty dynamicClientConcreteProducer() {
		return new RegexProperty(this.pattern, generate(), this.clazz);
	}

	public RegexProperty concreteClientDynamicProducer() {
		return new RegexProperty(generate(), this.pattern, this.clazz);
	}

	public RegexProperty concreteClientEscapedDynamicProducer() {
		return new RegexProperty(generateAndEscapeJavaStringIfNeeded(), this.pattern,
				this.clazz);
	}

	public RegexProperty dynamicClientEscapedConcreteProducer() {
		return new RegexProperty(this.pattern, generateAndEscapeJavaStringIfNeeded(),
				this.clazz);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RegexProperty that = (RegexProperty) o;
		return Objects.equals(stringPatternIfPresent(pattern),
				stringPatternIfPresent(that.pattern))
				&& Objects.equals(clazz, that.clazz);
	}

	private Object stringPatternIfPresent(Pattern value) {
		return value != null ? value.pattern() : null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(stringPatternIfPresent(pattern), clazz);
	}

	@Override
	public String toString() {
		return this.pattern();
	}

	@Override
	public Object generateConcreteValue() {
		return generate();
	}

	public Pattern getPattern() {
		return pattern;
	}

	public Class getClazz() {
		return clazz;
	}

}
