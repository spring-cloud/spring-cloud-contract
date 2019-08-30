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
 * Matching strategy of dynamic parts of the body.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
open class BodyMatchersDsl {

    private val jsonPathMatchers = LinkedHashMap<String, MatchingTypeValue>()
    private val xPathMatchers = LinkedHashMap<String, MatchingTypeValue>()

    /**
     * Adds a JSON path matcher.
     *
     * @param path The path.
     * @param matcher The matcher.
     */
    fun jsonPath(path: String, matcher: MatchingTypeValue) {
        this.jsonPathMatchers[path] = matcher
    }

    /**
     * Adds an xPath matcher.
     *
     * @param path The path.
     * @param matcher The matcher.
     */
    fun xPath(path: String, matcher: MatchingTypeValue) {
        this.xPathMatchers[path] = matcher
    }

    /* HELPER VARIABLES */

    val byDate
        get() = MatchingTypeValue(MatchingType.DATE, RegexPatterns.isoDate())

    val byTime
        get() = MatchingTypeValue(MatchingType.TIME, RegexPatterns.isoTime())

    val byTimestamp
        get() = MatchingTypeValue(MatchingType.TIMESTAMP, RegexPatterns.isoDateTime())

    val byEquality
        get() = MatchingTypeValue(MatchingType.EQUALITY)

    /* HELPER FUNCTIONS */

    fun byRegex(regex: String) = byRegex(Pattern.compile(regex))

    fun byRegex(regex: RegexProperty) = RegexMatchingTypeValue(MatchingType.REGEX, regex)

    fun byRegex(regex: Pattern) = RegexMatchingTypeValue(MatchingType.REGEX, RegexProperty(regex))

    fun byType(configurer: MatchingTypeValueHolder.() -> Unit): MatchingTypeValue = MatchingTypeValueHolder().apply(configurer).matchingTypeValue

    internal open fun get(): BodyMatchers = configureBodyMatchers(BodyMatchers())

    internal fun <T : BodyMatchers> configureBodyMatchers(bodyMatchers: T): T {
        this.jsonPathMatchers.forEach(bodyMatchers::jsonPath)
        this.xPathMatchers.forEach(bodyMatchers::xPath)
        return bodyMatchers
    }

}