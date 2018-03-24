package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.OptionalBody
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
import au.com.dius.pact.model.v3.messaging.Message
import au.com.dius.pact.model.v3.messaging.MessagePact
import groovy.json.JsonException
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
class MessagingSCContractCreator {

	private static final JsonSlurper jsonSlurper = new JsonSlurper()
	private static final String FULL_BODY = '$'

	Collection<Contract> convertFrom(MessagePact pact) {
		return pact.messages.collect({ Message message ->
			Contract.make {
				label("$message.description")
				if (!message.providerStates.isEmpty()) {
					input {
						triggeredBy(getTriggeredBy(message))
					}
				}
				outputMessage {
					if (message.contents.present) {
						body(parseBody(message.contents))
						Category bodyRules = message.matchingRules.rulesForCategory('body')
						if (bodyRules && !bodyRules.matchingRules.isEmpty()) {
							testMatchers {
								bodyRules.matchingRules.each { String key, MatchingRuleGroup ruleGroup ->
									if (ruleGroup.ruleLogic != RuleLogic.AND) {
										throw new UnsupportedOperationException("Currently only the AND combination rule logic is supported")
									}

									if (FULL_BODY.equals(key)) {
										JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(message.contents.value)
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
												switch(rule.numberType) {
													case NumberTypeMatcher.NumberType.NUMBER:
														jsonPath(key, byRegex(number()))
														break
													case NumberTypeMatcher.NumberType.INTEGER:
														jsonPath(key, byRegex(anInteger()))
														break
													case NumberTypeMatcher.NumberType.DECIMAL:
														jsonPath(key, byRegex(aDouble()))
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
					if (!message.metaData.isEmpty()) {
						headers {
							message.metaData.each { String k, String v ->
								if (k.equalsIgnoreCase("contentType")) {
									messagingContentType(v)
								} else {
									header(k, v)
								}
							}
						}
					}
				}
			}
		})
	}

	private String getTriggeredBy(Message message) {
		return message.providerStates.first().name
				.replace(':', ' ')
				.replace(' ', '_')
				.replace('(', '')
				.replace(')', '')
				.uncapitalize() + "()"
	}

	private parseBody(OptionalBody optionalBody) {
		def body = optionalBody.value
		if (body instanceof String) {
			body = body.trim()
			if (body.startsWith("{") && body.endsWith("}")) {
				try {
					body = jsonSlurper.parseText(body)
				} catch (JsonException ex) { /*it wasn't a JSON string after all...*/ }
			}
		}
		return body
	}

}