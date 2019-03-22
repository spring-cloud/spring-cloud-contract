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

package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.DateMatcher
import au.com.dius.pact.model.matchingrules.EqualsMatcher
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinMaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.NullMatcher
import au.com.dius.pact.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.TimeMatcher
import au.com.dius.pact.model.matchingrules.TimestampMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.RegexPatterns

/**
 * @author Tim Ysewyn
 * @Since 2.0.0
 */
@CompileStatic
@PackageScope
class MatchingRulesConverter {

	private static final RegexPatterns regexPatterns = new RegexPatterns()

	static Category matchingRulesForBody(BodyMatchers bodyMatchers) {
		return matchingRulesFor("body", bodyMatchers)
	}

	private static Category matchingRulesFor(String categoryName, BodyMatchers bodyMatchers) {
		Category category = new Category(categoryName)
		bodyMatchers.matchers().forEach({ BodyMatcher it ->
			String key = getMatcherKey(it.path())
			MatchingType matchingType = it.matchingType()
			switch (matchingType) {
			case MatchingType.NULL:
				category.addRule(key, NullMatcher.INSTANCE)
				break
			case MatchingType.EQUALITY:
				category.addRule(key, EqualsMatcher.INSTANCE)
				break
			case MatchingType.TYPE:
				if (it.minTypeOccurrence() && it.maxTypeOccurrence()) {
					category.
							addRule(key, new MinMaxTypeMatcher(it.minTypeOccurrence(), it.
									maxTypeOccurrence()))
				}
				else if (it.minTypeOccurrence()) {
					category.addRule(key, new MinTypeMatcher(it.minTypeOccurrence()))
				}
				else if (it.maxTypeOccurrence()) {
					category.addRule(key, new MaxTypeMatcher(it.maxTypeOccurrence()))
				}
				else {
					category.addRule(key, TypeMatcher.INSTANCE)
				}
				break
			case MatchingType.DATE:
				category.addRule(key, new DateMatcher())
				break
			case MatchingType.TIME:
				category.addRule(key, new TimeMatcher())
				break
			case MatchingType.TIMESTAMP:
				category.addRule(key, new TimestampMatcher())
				break
			case MatchingType.REGEX:
				String pattern = it.value().toString()
				if (pattern == regexPatterns.number().pattern()) {
					category.
							addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER))
				}
				else if (pattern == regexPatterns.anInteger().pattern()) {
					category.
							addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER))
				}
				else if (pattern == regexPatterns.aDouble().pattern()) {
					category.
							addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL))
				}
				else {
					category.addRule(key, new RegexMatcher(pattern))
				}
				break
			default:
				break
			}
		})
		return category
	}

	private static String getMatcherKey(String path) {
		return "${path.startsWith('$') ? path.substring(1) : path}"
	}

}
