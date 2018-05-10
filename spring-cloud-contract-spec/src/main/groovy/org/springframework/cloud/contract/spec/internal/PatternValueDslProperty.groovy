/*
 *  Copyright 2013-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.lang3.RandomStringUtils

import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
@PackageScope
@CompileStatic
abstract class PatternValueDslProperty<T extends DslProperty> {

	private final Random random = new Random()

	protected T createAndValidateProperty(Pattern pattern, Object object = null) {
		if (object) {
			String generatedValue = object as String
			boolean matches = pattern.matcher(generatedValue).matches()
			if (!matches) {
				throw new IllegalStateException("The generated value [${generatedValue}] doesn't match the pattern [${pattern.pattern()}]")
			}
			return createProperty(pattern, object)
		}
		return createProperty(pattern, object)
	}

	/**
	 * Method to generate the PatternValue. The resulting implementation
	 * will create either a Client or a Server side impl.
	 *
	 * @param pattern - pattern for which the value will be generated or reused
	 * @param generatedValue - Nullable - potential generated value to be reused
	 * @return {@link DslProperty} wrapping a pattern and generated value
	 */
	protected abstract T createProperty(Pattern pattern, Object generatedValue)
	
	T anyAlphaUnicode() {
		return createAndValidateProperty(RegexPatterns.ONLY_ALPHA_UNICODE,
				RandomStringGenerator.randomString(20))
	}

	T anyAlphaNumeric() {
		return createAndValidateProperty(RegexPatterns.ALPHA_NUMERIC,
				RandomStringUtils.randomAlphanumeric(20))
	}

	T anyNumber() {
		return createAndValidateProperty(RegexPatterns.NUMBER, this.random.nextInt())
	}

	T anyInteger() {
		return createAndValidateProperty(RegexPatterns.INTEGER, this.random.nextInt())
	}

	T anyPositiveInt() {
		return createAndValidateProperty(RegexPatterns.POSITIVE_INT, Math.abs(this.random.nextInt() + 1))
	}

	T anyDouble() {
		return createAndValidateProperty(RegexPatterns.DOUBLE, this.random.nextInt(100) + this.random.nextDouble())
	}

	T anyHex() {
		return createAndValidateProperty(RegexPatterns.HEX,
				RandomStringUtils.random(10, "0123456789abcdef"))
	}

	T aBoolean() {
		return createAndValidateProperty(RegexPatterns.TRUE_OR_FALSE)
	}

	T anyIpAddress() {
		return createAndValidateProperty(RegexPatterns.IP_ADDRESS, "192.168.0." + this.random.nextInt(10))
	}

	T anyHostname() {
		return createAndValidateProperty(RegexPatterns.HOSTNAME_PATTERN, "http://foo" + this.random.nextInt() + ".com")
	}

	T anyEmail() {
		return createAndValidateProperty(RegexPatterns.EMAIL, "foo@bar" + this.random.nextInt() + ".com")
	}

	T anyUrl() {
		return createAndValidateProperty(RegexPatterns.URL, "http://foo" + this.random.nextInt() + ".com")
	}

	T anyHttpsUrl() {
		return createAndValidateProperty(RegexPatterns.HTTPS_URL, "https://baz" + this.random.nextInt() + ".com")
	}

	T anyUuid() {
		return createAndValidateProperty(RegexPatterns.UUID, UUID.randomUUID().toString())
	}

	T anyDate() {
		int d = this.random.nextInt(8) + 1
		return createAndValidateProperty(RegexPatterns.ANY_DATE, "201$d-0$d-1$d")
	}

	T anyDateTime() {
		int d = this.random.nextInt(8) + 1
		return createAndValidateProperty(RegexPatterns.ANY_DATE_TIME, "201$d-0$d-1${d}T12:23:34")
	}

	T anyTime() {
		int d = this.random.nextInt(9)
		return createAndValidateProperty(RegexPatterns.ANY_TIME, "12:2$d:3$d")
	}

	T anyIso8601WithOffset() {
		int d = this.random.nextInt(8) + 1
		return createAndValidateProperty(RegexPatterns.ISO8601_WITH_OFFSET, "201$d-0$d-1${d}T12:23:34.123Z")
	}

	T anyNonBlankString() {
		return createAndValidateProperty(RegexPatterns.NON_BLANK,
				RandomStringGenerator.randomString(20))
	}

	T anyNonEmptyString() {
		return createAndValidateProperty(RegexPatterns.NON_EMPTY,
				RandomStringGenerator.randomString(20))
	}

	T anyOf(String... values){
		return createAndValidateProperty(RegexPatterns.anyOf(values), values[0])
	}
}
