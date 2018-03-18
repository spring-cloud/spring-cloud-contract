package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.matchingrules.MatchingRulesImpl
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter

/**
 * Converter of JSON PACT file
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.1.0
 */
@CompileStatic
class PactContractConverter implements ContractConverter<Collection<Pact>> {

	private static final String MATCH_KEY = "match"
	private static final String REGEX_KEY = "regex"
	private static final String MAX_KEY = "max"
	private static final String MIN_KEY = "min"
	private static final String FULL_BODY = '$.body'

	private RequestResponsePactCreator requestResponsePactCreator = new RequestResponsePactCreator()
	private MessagePactCreator messagePactCreator = new MessagePactCreator()

	@Override
	boolean isAccepted(File file) {
		try {
			PactReader.loadPact(file)
			return true
		} catch (Exception e) {
			return false
		}
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		Pact pact = PactReader.loadPact(file)
		List<Interaction> interactions = pact.interactions
		return interactions.collect { Interaction interaction ->
			Contract.make {
				if (interaction instanceof RequestResponseInteraction) {
					RequestResponseInteraction requestResponseInteraction = (RequestResponseInteraction) interaction
					description("$requestResponseInteraction.description${providerState(interaction)}")
					request {
						method(requestResponseInteraction.request.method)
						if (requestResponseInteraction.request.query) {
							url(requestResponseInteraction.request.path) {
								queryParameters {
									requestResponseInteraction.request.query.each { String key, List<String> value ->
										value.each { String singleValue ->
											parameter(key, singleValue)
										}
									}
								}
							}
						} else {
							url(requestResponseInteraction.request.path)
						}
						if (requestResponseInteraction.request.headers) {
							headers {
								requestResponseInteraction.request.headers.each { String key, String value ->
									header(key, value)
								}
							}
						}
						if (requestResponseInteraction.request.body.state == OptionalBody.State.PRESENT) {
							def parsedBody = parseBody(requestResponseInteraction.request.body)
							if (parsedBody instanceof Map) {
								body(parsedBody as Map)
							} else if (parsedBody instanceof List) {
								body(parsedBody as List)
							} else {
								body(parsedBody.toString())
							}
						}
						if (requestResponseInteraction.request?.matchingRules) {
							Map<String, Object> rules = ((MatchingRulesImpl) requestResponseInteraction.request.matchingRules).toMap(PactSpecVersion.V2)
							if (!rules.isEmpty()) {
								stubMatchers {
									rules.each { String key, Object value ->
										String keyFromBody = toKeyStartingFromBody(key)
										Map<String, Object> valueAsMap = (Map<String, Object>) value

										if (valueAsMap.containsKey(MATCH_KEY)) {
											MatchingType matchingType = MatchingType.valueOf((valueAsMap.get(MATCH_KEY) as String).toUpperCase())
											switch (matchingType) {
												case MatchingType.EQUALITY:
													// equality is checked by default in the standard way
													break
												case MatchingType.DATE:
													jsonPath(keyFromBody, byDate())
													break
												case MatchingType.TIME:
													jsonPath(keyFromBody, byTime())
													break
												case MatchingType.TIMESTAMP:
													jsonPath(keyFromBody, byTimestamp())
													break
												case MatchingType.REGEX:
													jsonPath(keyFromBody, byRegex(valueAsMap.get(REGEX_KEY) as String))
													break
											}
										} else if (valueAsMap.containsKey(REGEX_KEY)) {
											jsonPath(keyFromBody, byRegex(valueAsMap.get(REGEX_KEY) as String))
										}
									}
								}
							}
						}
					}
					response {
						status(requestResponseInteraction.response.status)
						if (requestResponseInteraction.response.body.state == OptionalBody.State.PRESENT) {
							def parsedBody = parseBody(requestResponseInteraction.response.body)
							if (parsedBody instanceof Map) {
								body(parsedBody as Map)
							} else if (parsedBody instanceof List) {
								body(parsedBody as List)
							} else {
								body(parsedBody.toString())
							}
						}
						if (requestResponseInteraction.response?.matchingRules) {
							testMatchers {
								Map<String, Object> rules = ((MatchingRulesImpl) requestResponseInteraction.response.matchingRules).toMap(PactSpecVersion.V2)
								Object fullBodyCheck = rules.get(FULL_BODY)
								if (fullBodyCheck != null) {
									JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(requestResponseInteraction.response?.body?.value)
									jsonPaths.each {
										jsonPath(it.keyBeforeChecking(), byType())
									}
								}
								rules.each { String key, Object value ->
									String keyFromBody = toKeyStartingFromBody(key)
									Map<String, Object> valueAsMap = (Map<String, Object>) value

									if (!keyFromBody) {
										return
									}
									if (valueAsMap.containsKey(MATCH_KEY)) {
										MatchingType matchingType = MatchingType.valueOf((valueAsMap.get(MATCH_KEY) as String).toUpperCase())
										switch (matchingType) {
											case MatchingType.EQUALITY:
												// equality is checked by default in the standard way
												break
											case MatchingType.DATE:
												jsonPath(keyFromBody, byDate())
												break
											case MatchingType.TIME:
												jsonPath(keyFromBody, byTime())
												break
											case MatchingType.TIMESTAMP:
												jsonPath(keyFromBody, byTimestamp())
												break
											case MatchingType.REGEX:
												jsonPath(keyFromBody, byRegex(valueAsMap.get(REGEX_KEY) as String))
												break
											case MatchingType.TYPE:
												jsonPath(keyFromBody, byType() {
													if (valueAsMap.containsKey(MIN_KEY)) {
														minOccurrence(valueAsMap.get(MIN_KEY) as Integer)
													}
													if (valueAsMap.containsKey(MAX_KEY)) {
														maxOccurrence(valueAsMap.get(MAX_KEY) as Integer)
													}
												})
												break
										}
									} else if (valueAsMap.containsKey(REGEX_KEY)) {
										jsonPath(keyFromBody, byRegex(valueAsMap.get(REGEX_KEY) as String))
									}
								}
							}
						}
						requestResponseInteraction.response.headers?.each { String key, String value ->
							headers {
								header(key, value)
							}
						}
					}
				}
			}
		}
	}

	protected String providerState(Interaction interaction) {
		return interaction.providerState ? " ${interaction.providerState}" : ""
	}

	protected String toKeyStartingFromBody(String key) {
		if (key == FULL_BODY) {
			return ""
		}
		return key.replace(FULL_BODY, '$')
	}

	protected parseBody(OptionalBody optionalBody) {
		if (optionalBody.present) {
			def body = new JsonSlurper().parseText(optionalBody.value)
			if (body instanceof String) {
				optionalBody.value
			} else {
				body
			}
		} else {
			optionalBody.value
		}
	}

	protected Map<String, String> headers(Headers headers, Closure closure) {
		return headers.entries.collectEntries {
			String name = it.name
			String value = closure(it)
			return [(name) : value]
		}
	}

	protected String url(Contract dsl) {
		if (dsl.request.urlPath) {
			return dsl.request.urlPath.serverValue.toString()
		} else if (dsl.request.url) {
			return dsl.request.url.serverValue.toString()
		}
		throw new IllegalStateException("No url provided")
	}

	@Override
	Collection<Pact> convertTo(Collection<Contract> contracts) {
		List<Pact> pactContracts = new ArrayList<>()

		contracts.collect({ Contract contract ->
			if (contract.request) {
				pactContracts.add(requestResponsePactCreator.createFromContract(contract))
			}

			if (contract.input) {
				pactContracts.add(messagePactCreator.createFromContract(contract))
			}
		})

		pactContracts
	}
}
