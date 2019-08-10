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

package org.springframework.cloud.contract.spec.internal

import java.util.regex.Pattern

/**
 * @author Tim Ysewyn
 */
open class BodyMatchersDsl {

	private val jsonPathMatchers = LinkedHashMap<String, MatchingTypeValue>()
	private val xPathMatchers = LinkedHashMap<String, MatchingTypeValue>()

	/**
	 * Adds and configures a JSON path matcher.
	 *
	 * @param configurer The lambda to configure the JSON path matcher.
	 */
	fun jsonPath(configurer: BodyMatcherDsl.() -> Unit) {
		try {
			val bodyMatcher = BodyMatcherDsl().apply(configurer)
			this.jsonPathMatchers[bodyMatcher.path] = bodyMatcher.matcher
		} catch (ex: IllegalStateException) {
			throw IllegalStateException("Body matcher is missing its path or matcher")
		}
	}

	/**
	 * Adds and configures a xPath matcher.
	 *
	 * @param configurer The lambda to configure the xPath matcher.
	 */
	fun xPath(configurer: BodyMatcherDsl.() -> Unit) {
		try {
			val bodyMatcher = BodyMatcherDsl().apply(configurer)
			this.jsonPathMatchers[bodyMatcher.path] = bodyMatcher.matcher
		} catch (ex: IllegalStateException) {
			throw IllegalStateException("Body matcher is missing its path or matcher")
		}
	}

	fun byDate() = MatchingTypeValue(MatchingType.DATE, RegexPatterns.isoDate())

	fun byTime() = MatchingTypeValue(MatchingType.TIME, RegexPatterns.isoTime())

	fun byTimestamp() = MatchingTypeValue(MatchingType.TIMESTAMP, RegexPatterns.isoDateTime())

	fun byRegex(regex: String)= byRegex(Pattern.compile(regex))

	fun byRegex(regex: RegexProperty) = RegexMatchingTypeValue(MatchingType.REGEX, regex)

	fun byRegex(regex: Pattern) = RegexMatchingTypeValue(MatchingType.REGEX, RegexProperty(regex))

	fun byEquality() = MatchingTypeValue(MatchingType.EQUALITY)

	fun byType(configurer: MatchingTypeValueHolder.() -> Unit): MatchingTypeValue = MatchingTypeValueHolder().apply(configurer).matchingTypeValue

	internal open fun get(): BodyMatchers = configureBodyMatchers(BodyMatchers())

	internal fun <T: BodyMatchers> configureBodyMatchers(bodyMatchers: T): T {
		this.jsonPathMatchers.forEach(bodyMatchers::jsonPath)
		this.xPathMatchers.forEach(bodyMatchers::xPath)
		return bodyMatchers
	}

}