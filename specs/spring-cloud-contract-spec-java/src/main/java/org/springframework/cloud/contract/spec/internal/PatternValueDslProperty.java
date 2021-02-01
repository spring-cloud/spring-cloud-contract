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

import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
abstract class PatternValueDslProperty<T extends DslProperty> implements RegexCreatingProperty<T> {

	private final Random random = new Random();

	protected T createAndValidateProperty(Pattern pattern, Object object) {
		if (object != null) {
			String generatedValue = object.toString();
			boolean matches = pattern.matcher(generatedValue).matches();
			if (!matches) {
				throw new IllegalStateException("The generated value [" + generatedValue
						+ "] doesn\'t match the pattern [" + pattern.pattern() + "]");
			}

			return createProperty(pattern, object);
		}

		return createProperty(pattern, object);
	}

	protected T createAndValidateProperty(Pattern pattern) {
		return createAndValidateProperty(pattern, null);
	}

	/**
	 * Method to generate the PatternValue. The resulting implementation will create
	 * either a Client or a Server side impl.
	 * @param pattern - pattern for which the value will be generated or reused
	 * @param generatedValue - Nullable - potential generated value to be reused
	 * @return {@link DslProperty} wrapping a pattern and generated value
	 */
	protected abstract T createProperty(Pattern pattern, Object generatedValue);

	@Override
	public T anyAlphaUnicode() {
		return createAndValidateProperty(RegexPatterns.ONLY_ALPHA_UNICODE, RandomStringGenerator.randomString(20));
	}

	@Override
	public T anyAlphaNumeric() {
		return createAndValidateProperty(RegexPatterns.ALPHA_NUMERIC, RandomStringUtils.randomAlphanumeric(20));
	}

	@Override
	public T anyNumber() {
		return createAndValidateProperty(RegexPatterns.NUMBER, this.random.nextInt());
	}

	@Override
	public T anyInteger() {
		return createAndValidateProperty(RegexPatterns.INTEGER, this.random.nextInt());
	}

	@Override
	public T anyPositiveInt() {
		return createAndValidateProperty(RegexPatterns.POSITIVE_INT, Math.abs(this.random.nextInt() + 1));
	}

	@Override
	public T anyDouble() {
		return createAndValidateProperty(RegexPatterns.DOUBLE, this.random.nextInt(100) + this.random.nextDouble());
	}

	@Override
	public T anyHex() {
		return createAndValidateProperty(RegexPatterns.HEX, RandomStringUtils.random(10, "0123456789abcdef"));
	}

	@Override
	public T aBoolean() {
		return createAndValidateProperty(RegexPatterns.TRUE_OR_FALSE, this.random.nextBoolean());
	}

	@Override
	public T anyIpAddress() {
		return createAndValidateProperty(RegexPatterns.IP_ADDRESS, "192.168.0." + this.random.nextInt(10));
	}

	@Override
	public T anyHostname() {
		return createAndValidateProperty(RegexPatterns.HOSTNAME_PATTERN,
				"https://foo" + this.random.nextInt() + ".com");
	}

	@Override
	public T anyEmail() {
		return createAndValidateProperty(RegexPatterns.EMAIL, "foo@bar" + this.random.nextInt() + ".com");
	}

	@Override
	public T anyUrl() {
		return createAndValidateProperty(RegexPatterns.URL, "https://foo" + this.random.nextInt() + ".com");
	}

	@Override
	public T anyHttpsUrl() {
		return createAndValidateProperty(RegexPatterns.HTTPS_URL, "https://baz" + this.random.nextInt() + ".com");
	}

	@Override
	public T anyUuid() {
		return createAndValidateProperty(RegexPatterns.UUID, UUID.randomUUID().toString());
	}

	@Override
	public T anyDate() {
		int d = this.random.nextInt(8) + 1;
		return createAndValidateProperty(RegexPatterns.ANY_DATE,
				"201" + String.valueOf(d) + "-0" + String.valueOf(d) + "-1" + String.valueOf(d));
	}

	@Override
	public T anyDateTime() {
		final int d = this.random.nextInt(8) + 1;
		return createAndValidateProperty(RegexPatterns.ANY_DATE_TIME,
				"201" + String.valueOf(d) + "-0" + String.valueOf(d) + "-1" + String.valueOf(d) + "T12:23:34");
	}

	@Override
	public T anyTime() {
		int d = this.random.nextInt(9);
		return createAndValidateProperty(RegexPatterns.ANY_TIME, "12:2" + String.valueOf(d) + ":3" + String.valueOf(d));
	}

	@Override
	public T anyIso8601WithOffset() {
		final int d = this.random.nextInt(8) + 1;
		return createAndValidateProperty(RegexPatterns.ISO8601_WITH_OFFSET,
				"201" + String.valueOf(d) + "-0" + String.valueOf(d) + "-1" + String.valueOf(d) + "T12:23:34.123Z");
	}

	@Override
	public T anyNonBlankString() {
		return createAndValidateProperty(RegexPatterns.NON_BLANK, RandomStringGenerator.randomString(20));
	}

	@Override
	public T anyNonEmptyString() {
		return createAndValidateProperty(RegexPatterns.NON_EMPTY, RandomStringGenerator.randomString(20));
	}

	@Override
	public T anyOf(String... values) {
		return createAndValidateProperty(RegexPatterns.anyOf(values), values[this.random.nextInt(values.length)]);
	}

}
