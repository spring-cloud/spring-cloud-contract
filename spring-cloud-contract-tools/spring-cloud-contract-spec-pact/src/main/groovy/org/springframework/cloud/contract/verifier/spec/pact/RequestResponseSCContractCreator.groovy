package org.springframework.cloud.contract.verifier.spec.pact

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
	private static final RegexPatterns regexPatterns = new RegexPatterns();

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
					} else {
						url(request.path)
					}
					if (request.headers) {
						headers {
							request.headers.each { String key, String value ->
								header(key, value)
							}
						}
					}
					if (request.body.state == OptionalBody.State.PRESENT) {
						def parsedBody = BodyConverter.toSCCBody(request)
						if (parsedBody instanceof Map) {
							body(parsedBody as Map)
						} else if (parsedBody instanceof List) {
							body(parsedBody as List)
						} else {
							body(parsedBody.toString())
						}
					}
					Category bodyRules = request.matchingRules.rulesForCategory('body')
					if (bodyRules && !bodyRules.matchingRules.isEmpty()) {
						stubMatchers {
							bodyMatchers {
								bodyRules.matchingRules.each { String key, MatchingRuleGroup ruleGroup ->
									if (ruleGroup.ruleLogic != RuleLogic.AND) {
										throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported")
									}

									ruleGroup.rules.each { MatchingRule rule ->
										if (rule instanceof NullMatcher) {
											jsonPath(key, byNull())
										} else if (rule instanceof RegexMatcher) {
											jsonPath(key, byRegex(rule.regex))
										} else if (rule instanceof DateMatcher) {
											jsonPath(key, byDate())
										} else if (rule instanceof TimeMatcher) {
											jsonPath(key, byTime())
										} else if (rule instanceof TimestampMatcher) {
											jsonPath(key, byTimestamp())
										} else if (rule instanceof NumberTypeMatcher) {
											switch (rule.numberType) {
												case NumberTypeMatcher.NumberType.NUMBER:
													jsonPath(key, byRegex(regexPatterns.number()))
													break
												case NumberTypeMatcher.NumberType.INTEGER:
													jsonPath(key, byRegex(regexPatterns.anInteger()))
													break
												case NumberTypeMatcher.NumberType.DECIMAL:
													jsonPath(key, byRegex(regexPatterns.aDouble()))
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
				}
				response {
					Response response = interaction.response
					status(response.status)
					if (response.body.present) {
						def parsedBody = BodyConverter.toSCCBody(response)
						if (parsedBody instanceof Map) {
							body(parsedBody as Map)
						} else if (parsedBody instanceof List) {
							body(parsedBody as List)
						} else {
							body(parsedBody.toString())
						}
					}
					Category bodyRules = response.matchingRules.rulesForCategory('body')
					if (bodyRules && !bodyRules.matchingRules.isEmpty()) {
						testMatchers {
							bodyMatchers {
								bodyRules.matchingRules.each { String key, MatchingRuleGroup ruleGroup ->
									if (ruleGroup.ruleLogic != RuleLogic.AND) {
										throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported")
									}

									if (FULL_BODY.equals(key)) {
										JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(response.body.value)
										jsonPaths.each {
											jsonPath(it.keyBeforeChecking(), byType())
										}
									} else {
										ruleGroup.rules.each { MatchingRule rule ->
											if (rule instanceof NullMatcher) {
												jsonPath(key, byNull())
											} else if (rule instanceof RegexMatcher) {
												jsonPath(key, byRegex(rule.regex))
											} else if (rule instanceof DateMatcher) {
												jsonPath(key, byDate())
											} else if (rule instanceof TimeMatcher) {
												jsonPath(key, byTime())
											} else if (rule instanceof TimestampMatcher) {
												jsonPath(key, byTimestamp())
											} else if (rule instanceof MinTypeMatcher) {
												jsonPath(key, byType() {
													minOccurrence((rule as MinTypeMatcher).min)
												})
											} else if (rule instanceof MinMaxTypeMatcher) {
												jsonPath(key, byType() {
													minOccurrence((rule as MinMaxTypeMatcher).min)
													maxOccurrence((rule as MinMaxTypeMatcher).max)
												})
											} else if (rule instanceof MaxTypeMatcher) {
												jsonPath(key, byType() {
													maxOccurrence((rule as MaxTypeMatcher).max)
												})
											} else if (rule instanceof TypeMatcher) {
												jsonPath(key, byType())
											} else if (rule instanceof NumberTypeMatcher) {
												switch (rule.numberType) {
													case NumberTypeMatcher.NumberType.NUMBER:
														jsonPath(key, byRegex(regexPatterns.number()))
														break
													case NumberTypeMatcher.NumberType.INTEGER:
														jsonPath(key, byRegex(regexPatterns.anInteger()))
														break
													case NumberTypeMatcher.NumberType.DECIMAL:
														jsonPath(key, byRegex(regexPatterns.aDouble()))
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
					}
					response.headers?.each { String key, String value ->
						headers {
							header(key, value)
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