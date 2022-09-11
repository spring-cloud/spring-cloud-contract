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

package org.springframework.cloud.contract.verifier.spec.pact;

import au.com.dius.pact.core.model.matchingrules.DateMatcher;
import au.com.dius.pact.core.model.matchingrules.EqualsMatcher;
import au.com.dius.pact.core.model.matchingrules.MatchingRuleCategory;
import au.com.dius.pact.core.model.matchingrules.MaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinMaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.NullMatcher;
import au.com.dius.pact.core.model.matchingrules.NumberTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.RegexMatcher;
import au.com.dius.pact.core.model.matchingrules.TimeMatcher;
import au.com.dius.pact.core.model.matchingrules.TimestampMatcher;
import au.com.dius.pact.core.model.matchingrules.TypeMatcher;

import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.RegexPatterns;

/**
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
final class MatchingRulesConverter {

	private MatchingRulesConverter() {
	}

	static MatchingRuleCategory matchingRulesForBody(BodyMatchers bodyMatchers) {
		return matchingRulesFor("body", bodyMatchers);
	}

	private static MatchingRuleCategory matchingRulesFor(String categoryName, BodyMatchers bodyMatchers) {
		MatchingRuleCategory category = new MatchingRuleCategory(categoryName);
		bodyMatchers.matchers().forEach((b) -> {
			String key = getMatcherKey(b.path());
			MatchingType matchingType = b.matchingType();
			switch (matchingType) {
				case NULL:
					category.addRule(key, NullMatcher.INSTANCE);
					break;
				case EQUALITY:
					category.addRule(key, EqualsMatcher.INSTANCE);
					break;
				case TYPE:
					if (b.minTypeOccurrence() != null && b.maxTypeOccurrence() != null) {
						category.addRule(key, new MinMaxTypeMatcher(b.minTypeOccurrence(), b.maxTypeOccurrence()));
					}
					else if (b.minTypeOccurrence() != null) {
						category.addRule(key, new MinTypeMatcher(b.minTypeOccurrence()));
					}
					else if (b.maxTypeOccurrence() != null) {
						category.addRule(key, new MaxTypeMatcher(b.maxTypeOccurrence()));
					}
					else {
						category.addRule(key, TypeMatcher.INSTANCE);
					}
					break;
				case DATE:
					category.addRule(key, new DateMatcher());
					break;
				case TIME:
					category.addRule(key, new TimeMatcher());
					break;
				case TIMESTAMP:
					category.addRule(key, new TimestampMatcher());
					break;
				case REGEX:
					String pattern = b.value().toString();
					if (pattern.equals(RegexPatterns.number().pattern())) {
						category.addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER));
					}
					else if (pattern.equals(RegexPatterns.anInteger().pattern())) {
						category.addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER));
					}
					else if (pattern.equals(RegexPatterns.aDouble().pattern())) {
						category.addRule(key, new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL));
					}
					else {
						category.addRule(key, new RegexMatcher(pattern));
					}
					break;
				default:
					break;
			}
		});
		return category;
	}

	private static String getMatcherKey(String path) {
		return path.startsWith("$") ? path.substring(1) : path;
	}

}
