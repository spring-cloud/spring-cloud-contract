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

import java.util.regex.Pattern

import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.Response
import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.DateMatcher
import au.com.dius.pact.model.matchingrules.MatchingRule
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinMaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.NullMatcher
import au.com.dius.pact.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.RuleLogic
import au.com.dius.pact.model.matchingrules.TimeMatcher
import au.com.dius.pact.model.matchingrules.TimestampMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
/**
 * Creator of {@link Contract} instances
 *
 * @author Tim Ysewyn
 * @since 2.0.0
 */
@CompileStatic
@PackageScope
class RequestResponseSCContractCreator {

	private static final String FULL_BODY = '$'

	Collection<Contract> convertFrom(RequestResponsePact pact) {
		return pact.interactions.collect { RequestResponseInteraction interaction ->
			Contract.make {
				description(buildDescription(interaction))
				request {
					Request request = interaction.request
					method(request.method)
					if (request.query) {
						url(request.path) {
							queryParameters {
								request.query.each { String key, List<String> value ->
									value.each { String singleValue ->
										parameter(key, singleValue)
									}
								}
							}
						}
					}
					else {
						url(request.path)
					}
					if (request.headers) {
						Category headerRules = request.matchingRules.
								rulesForCategory('header')
						headers {
							request.headers.each { String k, List<String> v ->
								if (k.equalsIgnoreCase("Cookie")) {
									return
								}
								if (headerRules.matchingRules.containsKey(k)) {
									MatchingRuleGroup ruleGroup = headerRules.matchingRules.
											get(k)
									if (ruleGroup.rules.size() > 1) {
										throw new UnsupportedOperationException("Currently only 1 rule at a time for a header is supported")
									}
									MatchingRule rule = ruleGroup.rules[0]
									if (rule instanceof RegexMatcher) {
										v.each({
											header(k, $(c(regex(((RegexMatcher) rule).getRegex())),
													p(it)))
										})
									}
									else {
										throw new UnsupportedOperationException("Currently only the header matcher of type regex is supported")
									}
								}
								else {
									v.each({
										header(k, it)
									})
								}
							}
						}
					}
					if (request.headers.containsKey("Cookie")) {
						Category headerRules = request.matchingRules.
								rulesForCategory('header')
						String[] splitHeader = request.headers.get("Cookie").first().split(";")
						Map<String, String> foundCookies = splitHeader.collectEntries {
							String[] keyValue = it.split("=")
							return [(keyValue[0]): keyValue[1]]
						}
						cookies {
							foundCookies.each { k, v ->
								if (headerRules.matchingRules.containsKey("Cookie")) {
									MatchingRuleGroup ruleGroup = headerRules.matchingRules.
											get("Cookie")
									if (ruleGroup.rules.size() > 1) {
										throw new UnsupportedOperationException("Currently only 1 rule at a time for a header is supported")
									}
									MatchingRule rule = ruleGroup.rules[0]
									if (rule instanceof RegexMatcher) {
										v.each({
											cookie(k, $(c(regex(((RegexMatcher) rule).getRegex())),
													p(it)))
										})
									}
									else {
										throw new UnsupportedOperationException("Currently only the header matcher of type regex is supported")
									}
								}
								else {
									cookie(k, v)
								}
							}
						}
					}
					if (request.body.state == OptionalBody.State.PRESENT) {
						def parsedBody = BodyConverter.toSCCBody(request)
						if (parsedBody instanceof Map) {
							body(parsedBody as Map)
						}
						else if (parsedBody instanceof List) {
							body(parsedBody as List)
						}
						else {
							body(parsedBody.toString())
						}
					}
					Category bodyRules = request.matchingRules.rulesForCategory('body')
					if (bodyRules && !bodyRules.matchingRules.isEmpty()) {
						bodyMatchers {
							bodyRules.matchingRules.
									each { String key, MatchingRuleGroup ruleGroup ->
										if (ruleGroup.ruleLogic != RuleLogic.AND) {
											throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported")
										}

										ruleGroup.rules.each { MatchingRule rule ->
											if (rule instanceof RegexMatcher) {
												jsonPath(key, byRegex(rule.regex))
											}
											else if (rule instanceof DateMatcher) {
												jsonPath(key, byDate())
											}
											else if (rule instanceof TimeMatcher) {
												jsonPath(key, byTime())
											}
											else if (rule instanceof TimestampMatcher) {
												jsonPath(key, byTimestamp())
											}
											else if (rule instanceof NumberTypeMatcher) {
												switch (rule.numberType) {
												case NumberTypeMatcher.NumberType.NUMBER:
													jsonPath(key,
															byRegex(RegexPatterns.number()))
													break
												case NumberTypeMatcher.NumberType.INTEGER:
													jsonPath(key, byRegex(RegexPatterns.
															anInteger()))
													break
												case NumberTypeMatcher.NumberType.DECIMAL:
													jsonPath(key,
															byRegex(RegexPatterns.aDouble()))
													break
												default:
													throw new RuntimeException("Unsupported number type!")
												}
											}
										}
									}
						}
					}
				}
				response {
					Response response = interaction.response
					status(response.status)
					if (response.body.present) {
						def parsedBody = BodyConverter.toSCCBody(response)
						if (parsedBody instanceof Map) {
							body(parsedBody as Map)
						}
						else if (parsedBody instanceof List) {
							body(parsedBody as List)
						}
						else {
							body(parsedBody.toString())
						}
					}
					Category bodyRules = response.matchingRules.rulesForCategory('body')
					if (bodyRules && !bodyRules.matchingRules.isEmpty()) {
						bodyMatchers {
							bodyRules.matchingRules.
									each { String key, MatchingRuleGroup ruleGroup ->
										if (ruleGroup.ruleLogic != RuleLogic.AND) {
											throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported")
										}

										if (FULL_BODY == key) {
											JsonPaths jsonPaths = JsonToJsonPathsConverter.
													transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(response.body.value instanceof byte[] ? new String(response.body.value) : response.body.value)
											jsonPaths.each {
												jsonPath(it.keyBeforeChecking(), byType())
											}
										}
										else {
											ruleGroup.rules.each { MatchingRule rule ->
												if (rule instanceof NullMatcher) {
													jsonPath(key, byNull())
												}
												else if (rule instanceof RegexMatcher) {
													jsonPath(key, byRegex(rule.regex))
												}
												else if (rule instanceof DateMatcher) {
													jsonPath(key, byDate())
												}
												else if (rule instanceof TimeMatcher) {
													jsonPath(key, byTime())
												}
												else if (rule instanceof TimestampMatcher) {
													jsonPath(key, byTimestamp())
												}
												else if (rule instanceof MinTypeMatcher) {
													jsonPath(key, byType() {
														minOccurrence((rule as MinTypeMatcher).min)
													})
												}
												else if (rule instanceof MinMaxTypeMatcher) {
													jsonPath(key, byType() {
														minOccurrence((rule as MinMaxTypeMatcher).min)
														maxOccurrence((rule as MinMaxTypeMatcher).max)
													})
												}
												else if (rule instanceof MaxTypeMatcher) {
													jsonPath(key, byType() {
														maxOccurrence((rule as MaxTypeMatcher).max)
													})
												}
												else if (rule instanceof TypeMatcher) {
													jsonPath(key, byType())
												}
												else if (rule instanceof NumberTypeMatcher) {
													switch (rule.numberType) {
													case NumberTypeMatcher.NumberType.NUMBER:
														jsonPath(key,
																byRegex(RegexPatterns.
																		number()))
														break
													case NumberTypeMatcher.NumberType.INTEGER:
														jsonPath(key,
																byRegex(RegexPatterns.
																		anInteger()))
														break
													case NumberTypeMatcher.NumberType.DECIMAL:
														jsonPath(key,
																byRegex(RegexPatterns.
																		aDouble()))
														break
													default:
														throw new UnsupportedOperationException("Unsupported number type!")
													}
												}
											}
										}
									}
						}
					}
					if (response.headers) {
						Category headerRules = response.matchingRules.
								rulesForCategory('header')
						headers {
							response.headers.forEach({ String k, List<String> v ->
								if (k.equalsIgnoreCase("Cookie")) {
									return
								}
								if (headerRules.matchingRules.containsKey(k)) {
									MatchingRuleGroup ruleGroup = headerRules.matchingRules.
											get(k)
									if (ruleGroup.rules.size() > 1) {
										throw new UnsupportedOperationException("Currently only 1 rule at a time for a header is supported")
									}
									MatchingRule rule = ruleGroup.rules[0]
									if (rule instanceof RegexMatcher) {
										v.each({
											header(k, $(p(regex(Pattern.compile(
													((RegexMatcher) rule).getRegex()))),
													c(it)))
										})

									}
									else {
										throw new UnsupportedOperationException("Currently only the header matcher of type regex is supported")
									}
								}
								else {
									v.each({
										header(k, it)
									})
								}
							})
						}
					}
					if (response.headers.containsKey("Cookie")) {
						Category headerRules = response.matchingRules.
								rulesForCategory('header')
						String[] splitHeader = response.headers.get("Cookie").first().split(";")
						Map<String, String> foundCookies = splitHeader.collectEntries {
							String[] keyValue = it.split("=")
							return [(keyValue[0]): keyValue[1]]
						}
						cookies {
							foundCookies.each { k, v ->
								if (headerRules.matchingRules.containsKey("Cookie")) {
									MatchingRuleGroup ruleGroup = headerRules.matchingRules.
											get("Cookie")
									if (ruleGroup.rules.size() > 1) {
										throw new UnsupportedOperationException("Currently only 1 rule at a time for a header is supported")
									}
									MatchingRule rule = ruleGroup.rules[0]
									if (rule instanceof RegexMatcher) {
										v.each({
											cookie(k, $(p(regex(Pattern.compile(
													((RegexMatcher) rule).getRegex()))),
													c(it)))
										})

									}
									else {
										throw new UnsupportedOperationException("Currently only the header matcher of type regex is supported")
									}
								}
								else {
									cookie(k, v)
								}
							}
						}
					}
				}
			}
		}
	}

	private String buildDescription(RequestResponseInteraction interaction) {
		String description = "$interaction.description"
		interaction.providerStates.forEach({ ProviderState it ->
			description += " $it.name"
			if (!it.params.isEmpty()) {
				Map<String, Object> params = it.params
				description += "("
				params.forEach({ String k, Object v ->
					description += k + ": " + v.toString()
					if (params.keySet().last() != k) {
						description += ", "
					}
				})
				description += ")"
			}
		})
		return description
	}

}
