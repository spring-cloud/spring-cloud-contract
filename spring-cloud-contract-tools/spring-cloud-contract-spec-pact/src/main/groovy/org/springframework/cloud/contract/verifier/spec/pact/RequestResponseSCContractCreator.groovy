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
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.TimeMatcher
import au.com.dius.pact.model.matchingrules.TimestampMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract
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
						def parsedBody = parseBody(request.body)
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
							bodyRules.matchingRules.each { String key, MatchingRuleGroup ruleGroup ->
								ruleGroup.rules.each { MatchingRule rule ->
									if (rule instanceof RegexMatcher) {
										jsonPath(key, byRegex(rule.regex))
									} else if (rule instanceof DateMatcher) {
										jsonPath(key, byDate())
									} else if (rule instanceof TimeMatcher) {
										jsonPath(key, byTime())
									} else if (rule instanceof TimestampMatcher) {
										jsonPath(key, byTimestamp())
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
						def parsedBody = parseBody(response.body)
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
							bodyRules.matchingRules.each { String key, MatchingRuleGroup ruleGroup ->
								if (FULL_BODY.equals(key)) {
									JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(response.body.value)
									jsonPaths.each {
										jsonPath(it.keyBeforeChecking(), byType())
									}
								} else {
									ruleGroup.rules.each { MatchingRule rule ->
										if (rule instanceof RegexMatcher) {
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

	private parseBody(OptionalBody optionalBody) {
		if (optionalBody.present) {
			def body = new JsonSlurper().parseText(optionalBody.value)
			if (body instanceof String) {
				return optionalBody.value
			} else {
				return body
			}
		} else {
			return optionalBody.value
		}
	}

}