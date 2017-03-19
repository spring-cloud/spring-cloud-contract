package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
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
			return createProperty(pattern, generatedValue)
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
		return createAndValidateProperty(RegexPatterns.ONLY_ALPHA_UNICODE, randomString(20))
	}

	T anyNumber() {
		return createAndValidateProperty(RegexPatterns.NUMBER, this.random.nextInt())
	}

	T aBoolean() {
		return createAndValidateProperty(RegexPatterns.TRUE_OR_FALSE)
	}

	T anyIpAddress() {
		return createAndValidateProperty(RegexPatterns.IP_ADDRESS)
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

	T anyIso8601DateTimeWithTimeZone() {
		int d = this.random.nextInt(8) + 1
		return createAndValidateProperty(RegexPatterns.ISO8601_WITH_OFFSET, "201$d-0$d-1${d}T12:23:34.123Z")
	}

	T anyNonBlankString() {
		return createAndValidateProperty(RegexPatterns.NON_BLANK, randomString(20))
	}

	T anyNonEmptyString() {
		return createAndValidateProperty(RegexPatterns.NON_EMPTY, randomString(20))
	}

	T enumOf(String... values){
		return createAndValidateProperty(RegexPatterns.anyOf(values), values[0])
	}

	private static String randomString(int length) {
		char[] characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
		Random random = new Random()
		char[] result = new char[length]
		for (int i = 0; i < result.length; i++) {
			// picks a random index out of character set > random character
			int randomCharIndex = random.nextInt(characterSet.length)
			result[i] = characterSet[randomCharIndex]
		}
		return new String(result)
	}
}
