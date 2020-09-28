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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import au.com.dius.pact.core.model.matchingrules.Category;
import au.com.dius.pact.core.model.matchingrules.DateMatcher;
import au.com.dius.pact.core.model.matchingrules.MatchingRule;
import au.com.dius.pact.core.model.matchingrules.MaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinMaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.NullMatcher;
import au.com.dius.pact.core.model.matchingrules.NumberTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.RegexMatcher;
import au.com.dius.pact.core.model.matchingrules.RuleLogic;
import au.com.dius.pact.core.model.matchingrules.TimeMatcher;
import au.com.dius.pact.core.model.matchingrules.TimestampMatcher;
import au.com.dius.pact.core.model.matchingrules.TypeMatcher;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.RegexPatterns;
import org.springframework.cloud.contract.spec.internal.ResponseBodyMatchers;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;

/**
 * Creator of {@link Contract} instances.
 *
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
class MessagingSCContractCreator {

	private static final String FULL_BODY = "$";

	private static final String DESTINATION_KEY = "sentTo";

	private static final List<String> NON_HEADER_META_DATA = Collections.singletonList(DESTINATION_KEY);

	Collection<Contract> convertFrom(MessagePact pact) {
		return pact.getMessages().stream().map(message -> Contract.make(contract -> {
			contract.label(message.getDescription());
			if (CollectionUtils.isNotEmpty(message.getProviderStates())) {
				contract.input(i -> i.triggeredBy(this.getTriggeredBy(message)));
			}

			contract.outputMessage((outputMessage) -> {
				if (message.getContents().isPresent()) {
					outputMessage.body(BodyConverter.toSCCBody(message));
					Category bodyRules = message.getMatchingRules().rulesForCategory("body");
					if (bodyRules != null && MapUtils.isNotEmpty(bodyRules.getMatchingRules())) {
						outputMessage.bodyMatchers((responseBodyMatchers) -> outputMessageBodyMatchers(message,
								bodyRules, responseBodyMatchers));
					}
				}
				if (MapUtils.isNotEmpty(message.getMetaData())) {
					outputMessage.headers((headers) -> outputMessageHeaders(message, headers));
				}
				String dest = findDestination(message);
				if (StringUtils.isNotBlank(dest)) {
					outputMessage.sentTo(dest);
				}
			});
		})).collect(Collectors.toList());
	}

	private void outputMessageHeaders(Message message, Headers headers) {
		message.getMetaData().forEach((key, value) -> {
			String matchingRuleGroup = value.toString();
			if (key.equalsIgnoreCase("contentType")) {
				headers.messagingContentType(matchingRuleGroup);
			}
			else if (!NON_HEADER_META_DATA.contains(key)) {
				headers.header(key, matchingRuleGroup);
			}
		});
	}

	private void outputMessageBodyMatchers(Message message, Category bodyRules,
			ResponseBodyMatchers responseBodyMatchers) {
		bodyRules.getMatchingRules().forEach((matchingRuleKey, matchingRuleGroup) -> {
			if (matchingRuleGroup.getRuleLogic() != RuleLogic.AND) {
				throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported");
			}
			if (FULL_BODY.equals(matchingRuleKey)) {
				JsonPaths jsonPaths = JsonToJsonPathsConverter
						.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(message.getContents().getValue());
				jsonPaths.forEach((j) -> {
					responseBodyMatchers.jsonPath(j.keyBeforeChecking(), responseBodyMatchers.byType());
				});
			}
			else {
				matchingRuleGroup.getRules().forEach((rule) -> {
					applyJsonPathToResponseBodyMatchers(rule, matchingRuleKey, responseBodyMatchers);
				});
			}
		});
	}

	private String getTriggeredBy(Message message) {
		String triggeredBy = message.getProviderStates().get(0).getName().replaceAll(":", " ").replaceAll(" ", "_")
				.replaceAll("\\(", "").replaceAll("\\)", "");
		return StringUtils.uncapitalize(triggeredBy) + "()";
	}

	private String findDestination(Message message) {
		return message.getMetaData().get(DESTINATION_KEY) != null
				? message.getMetaData().get(DESTINATION_KEY).toString() : "";
	}

	void applyJsonPathToResponseBodyMatchers(MatchingRule matchingRule, String key,
			ResponseBodyMatchers responseBodyMatchers) {
		if (matchingRule instanceof NullMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byNull());
		}
		else if (matchingRule instanceof RegexMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byRegex(((RegexMatcher) matchingRule).getRegex()));
		}
		else if (matchingRule instanceof DateMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byDate());
		}
		else if (matchingRule instanceof TimeMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byTime());
		}
		else if (matchingRule instanceof TimestampMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byTimestamp());
		}
		else if (matchingRule instanceof MinTypeMatcher) {
			responseBodyMatchers.jsonPath(key,
					responseBodyMatchers.byType((b) -> b.minOccurrence(((MinTypeMatcher) matchingRule).getMin())));
		}
		else if (matchingRule instanceof MinMaxTypeMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byType((c) -> {
				c.minOccurrence(((MinMaxTypeMatcher) matchingRule).getMin());
				c.maxOccurrence(((MinMaxTypeMatcher) matchingRule).getMax());
			}));
		}
		else if (matchingRule instanceof MaxTypeMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byType((c) -> {
				c.maxOccurrence(((MaxTypeMatcher) matchingRule).getMax());
			}));
		}
		else if (matchingRule instanceof TypeMatcher) {
			responseBodyMatchers.jsonPath(key, responseBodyMatchers.byType());
		}
		else if (matchingRule instanceof NumberTypeMatcher) {
			switch (((NumberTypeMatcher) matchingRule).getNumberType()) {
			case NUMBER:
				responseBodyMatchers.jsonPath(key, responseBodyMatchers.byRegex(RegexPatterns.number()));
				break;
			case INTEGER:
				responseBodyMatchers.jsonPath(key, responseBodyMatchers.byRegex(RegexPatterns.anInteger()));
				break;
			case DECIMAL:
				responseBodyMatchers.jsonPath(key, responseBodyMatchers.byRegex(RegexPatterns.aDouble()));
				break;
			default:
				throw new RuntimeException("Unsupported number type!");
			}
		}
	}

}
